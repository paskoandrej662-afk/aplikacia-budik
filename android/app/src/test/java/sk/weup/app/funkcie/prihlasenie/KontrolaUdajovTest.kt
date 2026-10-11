package sk.weup.app.funkcie.prihlasenie

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KontrolaUdajovTest {

    @Test
    fun `platne e-maily prejdu`() {
        assertTrue(KontrolaUdajov.jePlatnyEmail("anna@skuska.sk"))
        assertTrue(KontrolaUdajov.jePlatnyEmail("  anna.novakova+budik@gmail.com  "))
    }

    @Test
    fun `neplatne e-maily neprejdu`() {
        listOf("", "anna", "anna@", "anna@skuska", "anna@skuska.s", "an na@skuska.sk").forEach {
            assertFalse("„$it“ nemal prejsť", KontrolaUdajov.jePlatnyEmail(it))
        }
    }

    @Test
    fun `heslo musi mat aspon 8 znakov`() {
        assertFalse(KontrolaUdajov.jePlatneHeslo("1234567"))
        assertTrue(KontrolaUdajov.jePlatneHeslo("12345678"))
    }

    @Test
    fun `meno nesmie byt prazdne ani prilis dlhe`() {
        assertFalse(KontrolaUdajov.jePlatneMeno("   "))
        assertTrue(KontrolaUdajov.jePlatneMeno("Anna"))
        assertFalse(KontrolaUdajov.jePlatneMeno("a".repeat(51)))
    }
}
