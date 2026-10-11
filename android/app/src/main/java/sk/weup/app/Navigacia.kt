package sk.weup.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import sk.weup.app.funkcie.domov.DomovObrazovka
import sk.weup.app.funkcie.prihlasenie.PrihlasenieObrazovka
import sk.weup.app.funkcie.prihlasenie.PrihlasenieRiadic
import sk.weup.app.jadro.server.StavPrihlasenia

/** Rozhoduje, ktorá obrazovka sa ukáže: prihlásenie, alebo appka pre prihláseného. */
@Composable
fun Navigacia(zavislosti: Zavislosti, poNacitani: () -> Unit) {
    val stav by zavislosti.prihlasenie.stav.collectAsStateWithLifecycle(StavPrihlasenia.NACITAVA)

    LaunchedEffect(stav) {
        if (stav != StavPrihlasenia.NACITAVA) poNacitani()
    }

    AnimatedContent(
        targetState = stav,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "navigacia",
    ) { aktualny ->
        when (aktualny) {
            StavPrihlasenia.NACITAVA -> Unit
            StavPrihlasenia.ODHLASENY -> {
                val riadic: PrihlasenieRiadic = viewModel(
                    factory = viewModelFactory { initializer { PrihlasenieRiadic(zavislosti.prihlasenie) } },
                )
                PrihlasenieObrazovka(riadic)
            }
            StavPrihlasenia.PRIHLASENY -> DomovObrazovka(zavislosti.prihlasenie)
        }
    }
}
