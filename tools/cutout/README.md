# Nearby cut-out generator

Offline batch tool that turns a place's main photo into a transparent cut-out for the Lamplight
Nearby front card. It is **not** part of the Gradle build and never runs on device. The spec
(Must See + cut-out pipeline, §B6) rules out on-device segmentation: it is slow, costs battery,
and nobody reviews the results.

The app currently renders **arch mode only** (`CutoutSlot.kt`). Cut-out mode needs the
`cutout_image_url` / `cutout_source_photo_id` / `cutout_status` place fields (spec §B7, still
[NEEDS APPROVAL]). Until they exist, this script is for evaluating which places would cut out
well.

## Setup

```bash
pip install pillow numpy scipy "rembg[cpu]"   # rembg downloads its model on first run (200–900 MB)
```

## Run

```bash
python tools/cutout/make_cutout.py originals/cathedral.jpg --id cathedral-st-tryphon \
    --title "Cathedral of Saint Tryphon" --out build/cutouts

# hand-made mask, skips rembg
python tools/cutout/make_cutout.py originals/cathedral.jpg --id cathedral-st-tryphon --matte masks/cathedral.png
```

Outputs: `<id>@3x.webp`, `<id>@2x.webp`, `<id>.preview.png`, `<id>.report.json`.
Exit code `0` pass, `2` review, `1` fail. Never publish `review` or `fail` without a person
looking at the preview.

## Keep in sync with the app

`CardSpec` mirrors the card geometry in
`composeApp/.../presentation/screens/home/components/NearbyCard.kt` (card 338×410, title zone,
slot 58% × 250dp, button row 18 + 56) and the shadow offsets in `CutoutSlot.kt` (−10, −6) and
(−10, 0). If one changes, change the other.

Cut-outs are derivatives of the CMS photos: confirm the photo licences allow modification, and
check the licence of the `rembg` model you use before commercial use.
