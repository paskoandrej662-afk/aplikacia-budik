package sk.weup.app

import sk.weup.app.jadro.server.PrihlasenieRepozitar
import sk.weup.app.jadro.server.SupabasePrihlasenie
import sk.weup.app.jadro.server.vytvorSupabaseKlienta

/**
 * Jedno miesto, kde sa vytvárajú spoločné časti appky (pripojenie na server, repozitáre).
 * Obrazovky si ich berú odtiaľto, v testoch sa dajú nahradiť skúšobnými.
 */
class Zavislosti {
    private val supabase = vytvorSupabaseKlienta()

    val prihlasenie: PrihlasenieRepozitar = SupabasePrihlasenie(supabase)
}
