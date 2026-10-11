package sk.weup.app.jadro.server

import kotlinx.coroutines.flow.Flow

/** Či je používateľ prihlásený. */
enum class StavPrihlasenia { NACITAVA, PRIHLASENY, ODHLASENY }

/** Výsledok registrácie: buď je človek hneď prihlásený, alebo musí najprv potvrdiť e-mail. */
enum class VysledokRegistracie { PRIHLASENY, TREBA_POTVRDIT_EMAIL }

/** Druhy chýb pri prihlásení, ktoré vieme človeku zrozumiteľne vysvetliť. */
enum class DruhChyby {
    ZLE_UDAJE,
    UCET_EXISTUJE,
    SLABE_HESLO,
    NEPOTVRDENY_EMAIL,
    VELA_POKUSOV,
    BEZ_INTERNETU,
    GOOGLE,
    NEZNAMA,
}

class ChybaPrihlasenia(val druh: DruhChyby, pricina: Throwable? = null) : Exception(druh.name, pricina)

/**
 * Prihlásenie, registrácia a odhlásenie. Obrazovky pracujú iba s týmto rozhraním,
 * takže v testoch ho nahradí skúšobná verzia bez servera.
 * Pri chybe každá funkcia vyhodí [ChybaPrihlasenia].
 */
interface PrihlasenieRepozitar {
    val stav: Flow<StavPrihlasenia>

    /** Meno prihláseného používateľa, ak ho poznáme. */
    fun meno(): String?

    suspend fun prihlas(email: String, heslo: String)

    suspend fun zaregistruj(meno: String, email: String, heslo: String): VysledokRegistracie

    /** [idToken] je od Googlu, [nonce] je jednorazový kód, ktorým server overí, že token je čerstvý. */
    suspend fun prihlasCezGoogle(idToken: String, nonce: String)

    suspend fun odhlas()
}
