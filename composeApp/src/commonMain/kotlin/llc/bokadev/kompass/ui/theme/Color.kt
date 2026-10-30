package llc.bokadev.kompass.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val lampYellow = Color(0xFFFFC247) // accent: buttons, highlights, selected tab, active audio
val lampShadow = Color(0xFF2A2516) // loaded-audio row, saved-card remove/save buttons
val nightBlack = Color(0xFF12161A) // screen backgrounds
val charcoal = Color(0xFF1E2327) // raised surfaces, cards
val charcoalLight = Color(0xFF22262A) // saved card fill
val graphite = Color(0xFF262B2F) // bottom nav bar
val slateDeep = Color(0xFF232A2E) // expanded essentials module
val slateDark = Color(0xFF2A3034) // muted surfaces
val slateDivider = Color(0xFF2C3236) // dividers
val slateGray = Color(0xFF3A4146) // strong borders
val ashGray = Color(0xFF4A5055) // inactive indicators
val mossGray = Color(0xFF7A7F78) // landmark glyph on limestone
val pebbleGray = Color(0xFF9EA39C) // tertiary text
val stoneGray = Color(0xFFB7BBB3) // secondary text
val sageGray = Color(0xFFC3C7BF) // explore hero subtitle
val limestoneDark = Color(0xFFC9CAC3) // image placeholder on limestone cards
val limestone = Color(0xFFD9DAD3) // limestone cards
val boneWhite = Color(0xFFE6E7E0) // primary text
val inkGray = Color(0xFF3C4246) // text on limestone cards
val chalkWhite = Color(0xFFF4F4EF) // text on dark, chalk accents
val warmOffWhite = Color(0xFFF8F3EA) // Material surface/background, favorite button fill
val warmStone = Color(0xFFE8DCC8) // Material surface variant
val white = Color(0xFFFFFFFF) // page surfaces on the gradient
val white85 = Color(0xD9FFFFFF) // secondary lettering on the page gradient
val black = Color(0xFF000000) // Material primary/outline, favorite button icon
val inkBlack = Color(0xFF1E1B20) // page gradient bottom, window background
val inkBlack60 = Color(0x991E1B20) // muted text on light surfaces
val periwinkle = Color(0xFF777DB6) // language sheet accent
val lavender = Color(0xFFAD9DBE) // selection
val lavenderLight = Color(0xFFC9BFD5) // language sheet accent text
val mutedViolet = Color(0xFF6A6489) // page gradient top
val duskViolet = Color(0xFF55517A) // page gradient middle
val coralRed = Color(0xFFFF6B57) // alerts
val tomatoRed = Color(0xFFE53935) // favorited heart
val emberBrown = Color(0xFF2A1C1B) // emergency utility tile
val rustBrown = Color(0xFF5A2E29) // emergency utility tile border

@Immutable
data class KompassColors(
    val lampYellow: Color,
    val lampShadow: Color,
    val nightBlack: Color,
    val charcoal: Color,
    val charcoalLight: Color,
    val graphite: Color,
    val slateDeep: Color,
    val slateDark: Color,
    val slateDivider: Color,
    val slateGray: Color,
    val ashGray: Color,
    val mossGray: Color,
    val pebbleGray: Color,
    val stoneGray: Color,
    val sageGray: Color,
    val limestoneDark: Color,
    val limestone: Color,
    val boneWhite: Color,
    val inkGray: Color,
    val chalkWhite: Color,
    val warmOffWhite: Color,
    val warmStone: Color,
    val white: Color,
    val white85: Color,
    val black: Color,
    val inkBlack: Color,
    val inkBlack60: Color,
    val periwinkle: Color,
    val lavender: Color,
    val lavenderLight: Color,
    val mutedViolet: Color,
    val duskViolet: Color,
    val coralRed: Color,
    val tomatoRed: Color,
    val emberBrown: Color,
    val rustBrown: Color,
)

val LocalKompassColors = staticCompositionLocalOf {
    KompassColors(
        lampYellow = Color.Unspecified,
        lampShadow = Color.Unspecified,
        nightBlack = Color.Unspecified,
        charcoal = Color.Unspecified,
        charcoalLight = Color.Unspecified,
        graphite = Color.Unspecified,
        slateDeep = Color.Unspecified,
        slateDark = Color.Unspecified,
        slateDivider = Color.Unspecified,
        slateGray = Color.Unspecified,
        ashGray = Color.Unspecified,
        mossGray = Color.Unspecified,
        pebbleGray = Color.Unspecified,
        stoneGray = Color.Unspecified,
        sageGray = Color.Unspecified,
        limestoneDark = Color.Unspecified,
        limestone = Color.Unspecified,
        boneWhite = Color.Unspecified,
        inkGray = Color.Unspecified,
        chalkWhite = Color.Unspecified,
        warmOffWhite = Color.Unspecified,
        warmStone = Color.Unspecified,
        white = Color.Unspecified,
        white85 = Color.Unspecified,
        black = Color.Unspecified,
        inkBlack = Color.Unspecified,
        inkBlack60 = Color.Unspecified,
        periwinkle = Color.Unspecified,
        lavender = Color.Unspecified,
        lavenderLight = Color.Unspecified,
        mutedViolet = Color.Unspecified,
        duskViolet = Color.Unspecified,
        coralRed = Color.Unspecified,
        tomatoRed = Color.Unspecified,
        emberBrown = Color.Unspecified,
        rustBrown = Color.Unspecified,
    )
}

val kompassColors = KompassColors(
    lampYellow = lampYellow,
    lampShadow = lampShadow,
    nightBlack = nightBlack,
    charcoal = charcoal,
    charcoalLight = charcoalLight,
    graphite = graphite,
    slateDeep = slateDeep,
    slateDark = slateDark,
    slateDivider = slateDivider,
    slateGray = slateGray,
    ashGray = ashGray,
    mossGray = mossGray,
    pebbleGray = pebbleGray,
    stoneGray = stoneGray,
    sageGray = sageGray,
    limestoneDark = limestoneDark,
    limestone = limestone,
    boneWhite = boneWhite,
    inkGray = inkGray,
    chalkWhite = chalkWhite,
    warmOffWhite = warmOffWhite,
    warmStone = warmStone,
    white = white,
    white85 = white85,
    black = black,
    inkBlack = inkBlack,
    inkBlack60 = inkBlack60,
    periwinkle = periwinkle,
    lavender = lavender,
    lavenderLight = lavenderLight,
    mutedViolet = mutedViolet,
    duskViolet = duskViolet,
    coralRed = coralRed,
    tomatoRed = tomatoRed,
    emberBrown = emberBrown,
    rustBrown = rustBrown,
)
