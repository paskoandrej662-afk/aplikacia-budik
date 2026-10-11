package sk.weup.app.funkcie.prihlasenie

/** Kontrola údajov vo formulári ešte predtým, než ich pošleme na server. */
object KontrolaUdajov {
    /** Musí sedieť s nastavením servera (minimum_password_length v server/supabase/config.toml). */
    const val MIN_DLZKA_HESLA = 8
    const val MAX_DLZKA_MENA = 50

    private val vzorEmailu = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]{2,}$""")

    fun jePlatnyEmail(email: String): Boolean = vzorEmailu.matches(email.trim())

    fun jePlatneHeslo(heslo: String): Boolean = heslo.length >= MIN_DLZKA_HESLA

    fun jePlatneMeno(meno: String): Boolean = meno.trim().length in 1..MAX_DLZKA_MENA
}
