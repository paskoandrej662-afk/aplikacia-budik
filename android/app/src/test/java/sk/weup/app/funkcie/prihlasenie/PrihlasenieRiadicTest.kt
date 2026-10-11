package sk.weup.app.funkcie.prihlasenie

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import sk.weup.app.jadro.server.ChybaPrihlasenia
import sk.weup.app.jadro.server.DruhChyby
import sk.weup.app.jadro.server.PrihlasenieRepozitar
import sk.weup.app.jadro.server.StavPrihlasenia
import sk.weup.app.jadro.server.VysledokRegistracie

/** Skúšobný repozitár bez servera: zapamätá si volania a vráti, čo mu test nastaví. */
private class SkusobnePrihlasenie : PrihlasenieRepozitar {
    override val stav: Flow<StavPrihlasenia> = MutableStateFlow(StavPrihlasenia.ODHLASENY)
    var chyba: DruhChyby? = null
    var vysledokRegistracie = VysledokRegistracie.PRIHLASENY
    val volania = mutableListOf<String>()

    override fun meno(): String? = null

    override suspend fun prihlas(email: String, heslo: String) {
        volania += "prihlas $email"
        chyba?.let { throw ChybaPrihlasenia(it) }
    }

    override suspend fun zaregistruj(meno: String, email: String, heslo: String): VysledokRegistracie {
        volania += "zaregistruj $meno $email"
        chyba?.let { throw ChybaPrihlasenia(it) }
        return vysledokRegistracie
    }

    override suspend fun prihlasCezGoogle(idToken: String, nonce: String) {
        volania += "google"
        chyba?.let { throw ChybaPrihlasenia(it) }
    }

    override suspend fun odhlas() {
        volania += "odhlas"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PrihlasenieRiadicTest {

    private val repozitar = SkusobnePrihlasenie()
    private lateinit var riadic: PrihlasenieRiadic

    @Before
    fun priprav() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        riadic = PrihlasenieRiadic(repozitar)
    }

    @After
    fun uprac() = Dispatchers.resetMain()

    @Test
    fun `zly e-mail sa na server vobec neposle`() {
        riadic.zmenEmail("anna@")
        riadic.zmenHeslo("tajneheslo")
        riadic.odosli()

        assertEquals(ChybaPola.EMAIL_NEPLATNY, riadic.stav.value.chybaEmailu)
        assertTrue(repozitar.volania.isEmpty())
    }

    @Test
    fun `prihlasenie posle orezany e-mail`() {
        riadic.zmenEmail("  anna@skuska.sk ")
        riadic.zmenHeslo("tajneheslo")
        riadic.odosli()

        assertEquals(listOf("prihlas anna@skuska.sk"), repozitar.volania)
        assertFalse(riadic.stav.value.nacitava)
    }

    @Test
    fun `registracia vyzaduje meno a heslo aspon 8 znakov`() {
        riadic.prepniRezim()
        riadic.zmenEmail("anna@skuska.sk")
        riadic.zmenHeslo("kratke")
        riadic.odosli()

        val stav = riadic.stav.value
        assertEquals(ChybaPola.MENO_PRAZDNE, stav.chybaMena)
        assertEquals(ChybaPola.HESLO_KRATKE, stav.chybaHesla)
        assertTrue(repozitar.volania.isEmpty())
    }

    @Test
    fun `chyba zo servera sa ukaze a po uprave pola zmizne`() {
        repozitar.chyba = DruhChyby.ZLE_UDAJE
        riadic.zmenEmail("anna@skuska.sk")
        riadic.zmenHeslo("zleheslo")
        riadic.odosli()

        assertEquals(DruhChyby.ZLE_UDAJE, riadic.stav.value.chybaServera)
        riadic.zmenHeslo("ineheslo")
        assertNull(riadic.stav.value.chybaServera)
    }

    @Test
    fun `po registracii s potvrdenim e-mailu sa prepne na prihlasenie s oznamom`() {
        repozitar.vysledokRegistracie = VysledokRegistracie.TREBA_POTVRDIT_EMAIL
        riadic.prepniRezim()
        riadic.zmenMeno(" Anna ")
        riadic.zmenEmail("anna@skuska.sk")
        riadic.zmenHeslo("tajneheslo")
        riadic.odosli()

        val stav = riadic.stav.value
        assertEquals(listOf("zaregistruj Anna anna@skuska.sk"), repozitar.volania)
        assertEquals(Rezim.PRIHLASENIE, stav.rezim)
        assertTrue(stav.potvrdEmail)
        assertEquals("", stav.heslo)
    }

    @Test
    fun `chyba google sa ukaze ako chyba servera`() {
        riadic.chybaGoogle()
        assertEquals(DruhChyby.GOOGLE, riadic.stav.value.chybaServera)
    }
}
