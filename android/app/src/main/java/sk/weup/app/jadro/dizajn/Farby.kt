package sk.weup.app.jadro.dizajn

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Jedna výrazná farba značky (sviežo zelená), všetko ostatné neutrálne.
val Zelena = Color(0xFF20C965)
val ZelenaTmavsia = Color(0xFF17A853)
val TakmerCierna = Color(0xFF0B0F0C)
val Biela = Color(0xFFFFFFFF)
val SivaSvetla = Color(0xFFF2F4F3)
val SivaStredna = Color(0xFF6B736E)
val SivaTmava = Color(0xFF1A1F1C)
val SivaTmavsia = Color(0xFF242A26)
val Cervena = Color(0xFFE5484D)

/** Farby pre svetlý režim telefónu. */
val SvetleFarby = lightColorScheme(
    primary = Zelena,
    onPrimary = TakmerCierna,
    secondary = TakmerCierna,
    onSecondary = Biela,
    background = Biela,
    onBackground = TakmerCierna,
    surface = Biela,
    onSurface = TakmerCierna,
    surfaceVariant = SivaSvetla,
    onSurfaceVariant = SivaStredna,
    outline = Color(0xFFD5DAD7),
    error = Cervena,
    onError = Biela,
)

/** Farby pre tmavý režim telefónu. */
val TmaveFarby = darkColorScheme(
    primary = Zelena,
    onPrimary = TakmerCierna,
    secondary = Biela,
    onSecondary = TakmerCierna,
    background = TakmerCierna,
    onBackground = Biela,
    surface = SivaTmava,
    onSurface = Biela,
    surfaceVariant = SivaTmavsia,
    onSurfaceVariant = Color(0xFFA3ABA6),
    outline = Color(0xFF3A423D),
    error = Cervena,
    onError = Biela,
)
