package sk.weup.app.jadro.dizajn

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** Zaoblenie kariet, polí a tlačidiel. */
val WeupTvary = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/** Okraj obrazovky zľava a sprava. */
val OkrajObrazovky = 24.dp

/** Vzhľad celej appky. Svetlý alebo tmavý režim sa riadi podľa nastavenia telefónu. */
@Composable
fun WeupTema(tmavy: Boolean = isSystemInDarkTheme(), obsah: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (tmavy) TmaveFarby else SvetleFarby,
        typography = WeupPismo,
        shapes = WeupTvary,
        content = obsah,
    )
}
