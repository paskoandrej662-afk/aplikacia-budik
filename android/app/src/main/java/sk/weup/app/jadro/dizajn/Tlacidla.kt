package sk.weup.app.jadro.dizajn

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Výška veľkých tlačidiel, aby sa dali pohodlne stlačiť aj rozospatým palcom. */
private val VyskaTlacidla = 58.dp

/** Hlavné zelené tlačidlo. Na jednej obrazovke má byť iba jedno. */
@Composable
fun HlavneTlacidlo(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    nacitava: Boolean = false,
    povolene: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = povolene && !nacitava,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth().height(VyskaTlacidla),
    ) {
        if (nacitava) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Vedľajšie tlačidlo s obrysom (napr. „Pokračovať cez Google“). */
@Composable
fun VedlajsieTlacidlo(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    povolene: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = povolene,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth().height(VyskaTlacidla),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
