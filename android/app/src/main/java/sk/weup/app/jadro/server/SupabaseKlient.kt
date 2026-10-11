package sk.weup.app.jadro.server

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import sk.weup.app.BuildConfig

/** Pripojenie na server Supabase. Používa iba verejný kľúč, tajné kľúče v appke nie sú. */
fun vytvorSupabaseKlienta(): SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_ADRESA,
    supabaseKey = BuildConfig.SUPABASE_VEREJNY_KLUC,
) {
    install(Auth)
    install(Postgrest)
}
