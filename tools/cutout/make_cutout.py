#!/usr/bin/env python3
"""
KOMPASS cut-out generator for the Nearby front card (Lamplight design).

Turns a place's main photo into a transparent "cut-out" sized for the card's
image slot, checks the result against quality gates, and writes:
  <out>/<id>@2x.webp, <out>/<id>@3x.webp   transparent WebP, slot-sized
  <out>/<id>.preview.png                    the cut-out drawn on the card (for review)
  <out>/<id>.report.json                    metrics + verdict

Verdicts:
  pass    -> safe to publish as `cutout_image`
  review  -> a person must look at the preview before publishing
  fail    -> do not publish; the app uses the arch-mask fallback

Background removal uses `rembg` (pip install "rembg[cpu]") unless the input
already has transparency or you pass --matte with a precomputed mask.

Usage:
  python make_cutout.py photo.jpg --id cathedral-st-tryphon --out build/cutouts
  python make_cutout.py photo.jpg --id x --matte mask.png   # skip rembg
"""
from __future__ import annotations

import argparse
import io
import json
import sys
from dataclasses import dataclass, asdict, field
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageOps

try:
    from scipy import ndimage as ndi
except ImportError:  # optional; island cleanup is skipped without it
    ndi = None


# ---------------------------------------------------------------- geometry ---
@dataclass(frozen=True)
class CardSpec:
    """All values in dp, for the 390dp reference phone. Mirrors the design."""
    card_w: int = 338          # screen width - 2 * 26
    card_h: int = 410
    card_radius: int = 26
    slot_w: int = 196          # 58% of card width
    slot_h: int = 250
    button_zone_h: int = 74    # bottom row (18 margin + 56 button) covers the subject
    title_zone: tuple = (22, 22, 338 - 120, 132)  # x0, y0, x1, y1 in card coords
    shadow_dx: int = -10
    shadow_dy: int = -6

    @property
    def slot_origin(self) -> tuple[int, int]:
        return self.card_w - self.slot_w, self.card_h - self.slot_h


# ------------------------------------------------------------ thresholds -----
@dataclass(frozen=True)
class Gates:
    coverage_min: float = 0.25        # share of slot covered by the subject
    coverage_max: float = 0.85        # above this the matte probably kept the background
    bbox_fill_max: float = 0.93       # subject ~ a solid rectangle -> removal failed
    bottom_touch_min: float = 0.15    # share of bottom row that must be solid
    visible_mass_min: float = 0.45    # share of subject above the button zone
    side_clip_max: float = 0.45       # subject cut by the slot's left edge (tall wall)
    islands_max: int = 3              # separate blobs after cleanup
    upscale_review: float = 1.25      # source pixels stretched more than this -> review
    upscale_fail: float = 2.0
    webp_max_kb_3x: int = 150


@dataclass
class Report:
    id: str
    verdict: str = "pass"
    reasons: list = field(default_factory=list)
    metrics: dict = field(default_factory=dict)
    outputs: dict = field(default_factory=dict)

    def flag(self, level: str, reason: str) -> None:
        order = {"pass": 0, "review": 1, "fail": 2}
        if order[level] > order[self.verdict]:
            self.verdict = level
        self.reasons.append(f"{level}: {reason}")


# ---------------------------------------------------------------- matte ------
def load_rgb(path: Path) -> Image.Image:
    img = Image.open(path)
    img = ImageOps.exif_transpose(img)  # phone photos are often rotated via EXIF
    return img


def get_alpha(img: Image.Image, matte_path: Path | None, model: str) -> np.ndarray:
    """Return an 8-bit alpha matte (H x W) for the subject."""
    if img.mode in ("RGBA", "LA") and np.asarray(img.getchannel("A")).min() < 250:
        return np.asarray(img.getchannel("A"), dtype=np.uint8)
    if matte_path:
        m = Image.open(matte_path).convert("L").resize(img.size, Image.LANCZOS)
        return np.asarray(m, dtype=np.uint8)
    try:
        from rembg import new_session, remove
    except ImportError:
        sys.exit("rembg not installed: pip install 'rembg[cpu]'  (or pass --matte)")
    session = new_session(model)
    out = remove(img.convert("RGB"), session=session, only_mask=True,
                 post_process_mask=True)
    return np.asarray(out.convert("L"), dtype=np.uint8)


def clean_alpha(alpha: np.ndarray) -> tuple[np.ndarray, int]:
    """Drop speckles, keep the main subject (+ big attached parts), tidy the edge."""
    a = alpha.copy()
    a[a < 12] = 0                                   # kill faint haze
    solid = a >= 128
    islands = 1
    if ndi is not None and solid.any():
        labels, n = ndi.label(solid)
        if n > 1:
            sizes = ndi.sum(solid, labels, range(1, n + 1))
            keep_ids = [i + 1 for i, s in enumerate(sizes) if s >= sizes.max() * 0.08]
            keep = np.isin(labels, keep_ids)
            keep = ndi.binary_dilation(keep, iterations=3)  # keep soft edges nearby
            a[~keep] = 0
            islands = len(keep_ids)
    # 1px choke + 1px feather: removes the halo of old background colour
    m = Image.fromarray(a).filter(ImageFilter.MinFilter(3)).filter(
        ImageFilter.GaussianBlur(0.8))
    return np.asarray(m, dtype=np.uint8), islands


def decontaminate(rgb: np.ndarray, alpha: np.ndarray) -> np.ndarray:
    """Pull edge pixels toward nearby solid colour so no sky/wall fringe shows on yellow."""
    if ndi is None:
        return rgb
    solid = alpha >= 230
    if not solid.any():
        return rgb
    _, (iy, ix) = ndi.distance_transform_edt(~solid, return_indices=True)
    nearest = rgb[iy, ix]
    t = (alpha.astype(np.float32) / 255.0)[..., None]
    edge = (alpha > 0) & (alpha < 230)
    out = rgb.copy()
    out[edge] = (rgb[edge] * t[edge] + nearest[edge] * (1 - t[edge])).astype(np.uint8)
    return out


# ----------------------------------------------------------------- fit -------
def fit_to_slot(rgba: Image.Image, spec: CardSpec, scale: int) -> tuple[Image.Image, float, tuple]:
    """Trim to subject, scale to slot height (or width if too wide), align bottom-right."""
    a = np.asarray(rgba.getchannel("A"))
    ys, xs = np.nonzero(a > 16)
    if len(xs) == 0:
        raise ValueError("matte is empty")
    box = (xs.min(), ys.min(), xs.max() + 1, ys.max() + 1)
    subject = rgba.crop(box)

    slot_w, slot_h = spec.slot_w * scale, spec.slot_h * scale
    k = min(slot_h / subject.height, slot_w / subject.width)
    new = (max(1, round(subject.width * k)), max(1, round(subject.height * k)))
    subject = subject.resize(new, Image.LANCZOS)

    canvas = Image.new("RGBA", (slot_w, slot_h), (0, 0, 0, 0))
    canvas.alpha_composite(subject, (slot_w - new[0], slot_h - new[1]))
    return canvas, k, box


# --------------------------------------------------------------- gates -------
def run_gates(slot: Image.Image, k: float, islands: int, spec: CardSpec,
              gates: Gates, scale: int, rep: Report) -> None:
    a = np.asarray(slot.getchannel("A")) >= 128
    h, w = a.shape
    coverage = float(a.mean())
    ys, xs = np.nonzero(a)
    bbox_area = (xs.max() - xs.min() + 1) * (ys.max() - ys.min() + 1)
    bbox_fill = float(a.sum() / bbox_area)
    bottom_touch = float(a[-2:, :].any(axis=0).mean())
    visible_h = h - spec.button_zone_h * scale
    visible_mass = float(a[:visible_h].sum() / max(1, a.sum()))
    side_clip = float(a[:, 0].mean())

    rep.metrics.update(coverage=round(coverage, 3), bbox_fill=round(bbox_fill, 3),
                       bottom_touch=round(bottom_touch, 3),
                       visible_mass=round(visible_mass, 3),
                       side_clip=round(side_clip, 3), islands=islands,
                       upscale=round(k, 3))

    if coverage < gates.coverage_min:
        rep.flag("fail", f"subject too small in slot ({coverage:.0%})")
    if coverage > gates.coverage_max:
        rep.flag("fail", f"subject fills the slot ({coverage:.0%}); background likely kept")
    if bbox_fill > gates.bbox_fill_max:
        rep.flag("fail", "subject is a near-solid rectangle; removal likely failed")
    if bottom_touch < gates.bottom_touch_min:
        rep.flag("review", "subject barely touches the bottom; may look like it floats")
    if visible_mass < gates.visible_mass_min:
        rep.flag("review", "most of the subject sits behind the button row")
    if side_clip > gates.side_clip_max:
        rep.flag("review", "subject is cut by the left edge of the slot")
    if islands > gates.islands_max:
        rep.flag("review", f"{islands} separate pieces; check for stray background")
    if k > gates.upscale_fail:
        rep.flag("fail", f"source too small (stretched {k:.2f}x)")
    elif k > gates.upscale_review:
        rep.flag("review", f"source upscaled {k:.2f}x; may look soft")


# -------------------------------------------------------------- export -------
def save_webp(img: Image.Image, path: Path, max_kb: int | None) -> int:
    for q in (90, 85, 80, 75, 70, 60):
        buf = io.BytesIO()
        img.save(buf, "WEBP", quality=q, alpha_quality=90, method=6)
        if max_kb is None or buf.tell() <= max_kb * 1024 or q == 60:
            path.write_bytes(buf.getvalue())
            return buf.tell()
    return 0


def render_preview(slot3x: Image.Image, spec: CardSpec, title: str) -> Image.Image:
    """Draw the card as the app will: lamp panel, night shadow, cut-out, button row."""
    s = 3
    lamp, night, chalk = (255, 194, 71, 255), (18, 22, 26, 255), (244, 244, 239, 255)
    card = Image.new("RGBA", (spec.card_w * s, spec.card_h * s), lamp)
    ox, oy = (v * s for v in spec.slot_origin)
    shadow = Image.new("RGBA", slot3x.size, night)
    shadow.putalpha(slot3x.getchannel("A"))
    # Two shadow stamps: lifted (dx, dy) and grounded (dx, 0), so the shadow
    # reaches the card's bottom edge instead of leaving a yellow gap under it.
    card.alpha_composite(shadow, (ox + spec.shadow_dx * s, oy + spec.shadow_dy * s))
    card.alpha_composite(shadow, (ox + spec.shadow_dx * s, oy))
    card.alpha_composite(slot3x, (ox, oy))
    d = ImageDraw.Draw(card)
    x0, y0, x1, y1 = (v * s for v in spec.title_zone)
    d.rectangle((x0, y0, x1, y1), outline=(18, 22, 26, 90), width=2)
    d.text((x0 + 6, y0 + 6), title, fill=night)
    by = (spec.card_h - 18 - 56) * s
    d.rounded_rectangle((18 * s, by, (spec.card_w - 18 - 56 - 10) * s, by + 56 * s),
                        radius=28 * s, fill=chalk)
    d.ellipse(((spec.card_w - 18 - 56) * s, by, (spec.card_w - 18) * s, by + 56 * s), fill=chalk)
    mask = Image.new("L", card.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, *card.size), radius=spec.card_radius * s, fill=255)
    out = Image.new("RGBA", card.size, (0, 0, 0, 0))
    out.paste(card, (0, 0), mask)
    return out


# ---------------------------------------------------------------- main -------
def main(argv=None) -> int:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("input", type=Path)
    p.add_argument("--id", required=True, help="place id / slug used for file names")
    p.add_argument("--out", type=Path, default=Path("cutouts"))
    p.add_argument("--matte", type=Path, help="precomputed mask (white = subject)")
    p.add_argument("--model", default="birefnet-general", help="rembg model name")
    p.add_argument("--title", default="", help="title drawn in the preview")
    args = p.parse_args(argv)

    spec, gates = CardSpec(), Gates()
    rep = Report(id=args.id)
    args.out.mkdir(parents=True, exist_ok=True)

    src = load_rgb(args.input)
    rep.metrics["source_px"] = list(src.size)
    alpha_raw = get_alpha(src, args.matte, args.model)
    alpha, islands = clean_alpha(alpha_raw)
    rgb = decontaminate(np.asarray(src.convert("RGB")), alpha)
    rgba = Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")

    try:
        slot3, k3, box = fit_to_slot(rgba, spec, 3)
    except ValueError as e:
        rep.flag("fail", str(e))
        (args.out / f"{args.id}.report.json").write_text(json.dumps(asdict(rep), indent=2))
        return 1
    rep.metrics["subject_box_px"] = [int(v) for v in box]
    run_gates(slot3, k3, islands, spec, gates, 3, rep)

    slot2, _, _ = fit_to_slot(rgba, spec, 2)
    if rep.verdict != "fail":
        p3, p2 = args.out / f"{args.id}@3x.webp", args.out / f"{args.id}@2x.webp"
        kb3 = save_webp(slot3, p3, gates.webp_max_kb_3x) / 1024
        save_webp(slot2, p2, None)
        rep.outputs.update({"3x": str(p3), "2x": str(p2)})
        rep.metrics["webp_3x_kb"] = round(kb3, 1)
        if kb3 > gates.webp_max_kb_3x:
            rep.flag("review", f"3x file is {kb3:.0f} KB")

    prev = args.out / f"{args.id}.preview.png"
    render_preview(slot3, spec, args.title or args.id).save(prev)
    rep.outputs["preview"] = str(prev)

    (args.out / f"{args.id}.report.json").write_text(json.dumps(asdict(rep), indent=2))
    print(json.dumps({"id": rep.id, "verdict": rep.verdict, "reasons": rep.reasons}))
    return {"pass": 0, "review": 2, "fail": 1}[rep.verdict]


if __name__ == "__main__":
    sys.exit(main())
