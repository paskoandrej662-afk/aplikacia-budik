package sk.weup.app.funkcie.domov

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sk.weup.app.R
import sk.weup.app.jadro.dizajn.OkrajObrazovky
import sk.weup.app.jadro.dizajn.VedlajsieTlacidlo
import sk.weup.app.jadro.server.ChybaPrihlasenia
import sk.weup.app.jadro.server.PrihlasenieRepozitar

/** Dočasná hlavná obrazovka po prihlásení. Vo fáze 2 ju nahradí prehľad budíkov. */
@Composable
fun DomovObrazovka(prihlasenie: PrihlasenieRepozitar) {
    val korutiny = rememberCoroutineScope()
    var odhlasuje by remember { mutableStateOf(false) }
    val meno = remember { prihlasenie.meno() }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = OkrajObrazovky, vertical = 32.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = meno?.let { stringResource(R.string.domov_pozdrav, it) }
                ?: stringResource(R.string.domov_pozdrav_bez_mena),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.domov_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        VedlajsieTlacidlo(
            text = stringResource(R.string.tlacidlo_odhlasit),
            povolene = !odhlasuje,
            onClick = {
                odhlasuje = true
                korutiny.launch {
                    try {
                        prihlasenie.odhlas()
                    } catch (_: ChybaPrihlasenia) {
                        odhlasuje = false
                    }
                }
            },
        )
    }
}
