package sk.weup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import sk.weup.app.jadro.dizajn.WeupTema

/** Jediné okno appky. Všetky obrazovky sa v ňom striedajú (pozri Navigacia.kt). */
class HlavnaAktivita : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val startovaciaObrazovka = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Zelená štartovacia obrazovka ostane, kým nevieme, či je používateľ prihlásený.
        var nacitava = true
        startovaciaObrazovka.setKeepOnScreenCondition { nacitava }

        val zavislosti = (application as WeupAplikacia).zavislosti
        setContent {
            WeupTema {
                Navigacia(zavislosti = zavislosti, poNacitani = { nacitava = false })
            }
        }
    }
}
