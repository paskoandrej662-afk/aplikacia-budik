package sk.weup.app.funkcie.prihlasenie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sk.weup.app.jadro.server.ChybaPrihlasenia
import sk.weup.app.jadro.server.DruhChyby
import sk.weup.app.jadro.server.PrihlasenieRepozitar
import sk.weup.app.jadro.server.VysledokRegistracie

enum class Rezim { PRIHLASENIE, REGISTRACIA }

/** Chyby jednotlivých polí formulára. */
enum class ChybaPola { MENO_PRAZDNE, EMAIL_NEPLATNY, HESLO_KRATKE }

/** Všetko, čo obrazovka prihlásenia potrebuje vedieť, aby sa vedela nakresliť. */
data class PrihlasenieStav(
    val rezim: Rezim = Rezim.PRIHLASENIE,
    val meno: String = "",
    val email: String = "",
    val heslo: String = "",
    val chybaMena: ChybaPola? = null,
    val chybaEmailu: ChybaPola? = null,
    val chybaHesla: ChybaPola? = null,
    val chybaServera: DruhChyby? = null,
    val nacitava: Boolean = false,
    /** Po registrácii treba potvrdiť e-mail, obrazovka ukáže upozornenie. */
    val potvrdEmail: Boolean = false,
)

/**
 * Riadi obrazovku prihlásenia a registrácie: kontroluje polia, posiela údaje na server
 * a ukazuje chyby. Po úspešnom prihlásení appka sama prepne na ďalšiu obrazovku.
 */
class PrihlasenieRiadic(private val repozitar: PrihlasenieRepozitar) : ViewModel() {

    private val _stav = MutableStateFlow(PrihlasenieStav())
    val stav: StateFlow<PrihlasenieStav> = _stav.asStateFlow()

    fun zmenMeno(meno: String) = _stav.update { it.copy(meno = meno, chybaMena = null, chybaServera = null) }

    fun zmenEmail(email: String) = _stav.update { it.copy(email = email, chybaEmailu = null, chybaServera = null) }

    fun zmenHeslo(heslo: String) = _stav.update { it.copy(heslo = heslo, chybaHesla = null, chybaServera = null) }

    fun prepniRezim() = _stav.update {
        it.copy(
            rezim = if (it.rezim == Rezim.PRIHLASENIE) Rezim.REGISTRACIA else Rezim.PRIHLASENIE,
            chybaMena = null,
            chybaEmailu = null,
            chybaHesla = null,
            chybaServera = null,
            potvrdEmail = false,
        )
    }

    fun odosli() {
        val s = _stav.value
        if (s.nacitava) return

        val registracia = s.rezim == Rezim.REGISTRACIA
        val chybaMena = if (registracia && !KontrolaUdajov.jePlatneMeno(s.meno)) ChybaPola.MENO_PRAZDNE else null
        val chybaEmailu = if (!KontrolaUdajov.jePlatnyEmail(s.email)) ChybaPola.EMAIL_NEPLATNY else null
        // Pri prihlásení kontrolujeme iba, že heslo nie je prázdne – staré heslá mohli mať iné pravidlá.
        val chybaHesla = when {
            registracia && !KontrolaUdajov.jePlatneHeslo(s.heslo) -> ChybaPola.HESLO_KRATKE
            !registracia && s.heslo.isEmpty() -> ChybaPola.HESLO_KRATKE
            else -> null
        }
        if (chybaMena != null || chybaEmailu != null || chybaHesla != null) {
            _stav.update { it.copy(chybaMena = chybaMena, chybaEmailu = chybaEmailu, chybaHesla = chybaHesla) }
            return
        }

        vykonaj {
            val email = s.email.trim()
            if (registracia) {
                val vysledok = repozitar.zaregistruj(s.meno.trim(), email, s.heslo)
                if (vysledok == VysledokRegistracie.TREBA_POTVRDIT_EMAIL) {
                    _stav.update { it.copy(rezim = Rezim.PRIHLASENIE, heslo = "", potvrdEmail = true) }
                }
            } else {
                repozitar.prihlas(email, s.heslo)
            }
        }
    }

    /** Google vrátil prihlasovací token, overíme ho na serveri. */
    fun prihlasCezGoogle(idToken: String, nonce: String) = vykonaj {
        repozitar.prihlasCezGoogle(idToken, nonce)
    }

    /** Okno Googlu skončilo chybou (nie zrušením zo strany človeka). */
    fun chybaGoogle() = _stav.update { it.copy(chybaServera = DruhChyby.GOOGLE) }

    private fun vykonaj(akcia: suspend () -> Unit) {
        _stav.update { it.copy(nacitava = true, chybaServera = null, potvrdEmail = false) }
        viewModelScope.launch {
            try {
                akcia()
            } catch (e: ChybaPrihlasenia) {
                _stav.update { it.copy(chybaServera = e.druh) }
            } finally {
                _stav.update { it.copy(nacitava = false) }
            }
        }
    }
}
