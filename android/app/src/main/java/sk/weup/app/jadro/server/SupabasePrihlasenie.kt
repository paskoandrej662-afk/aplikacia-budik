package sk.weup.app.jadro.server

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException

/** Prihlásenie cez server Supabase. */
class SupabasePrihlasenie(supabase: SupabaseClient) : PrihlasenieRepozitar {

    private val auth = supabase.auth

    override val stav: Flow<StavPrihlasenia> = auth.sessionStatus.map { stav ->
        when (stav) {
            is SessionStatus.Initializing -> StavPrihlasenia.NACITAVA
            is SessionStatus.Authenticated -> StavPrihlasenia.PRIHLASENY
            // Prihlásenie sa nepodarilo obnoviť (napr. bez internetu), ale človek prihlásený ostáva.
            is SessionStatus.RefreshFailure -> StavPrihlasenia.PRIHLASENY
            is SessionStatus.NotAuthenticated -> StavPrihlasenia.ODHLASENY
        }
    }

    override fun meno(): String? {
        val udaje = auth.currentUserOrNull()?.userMetadata ?: return null
        // „meno“ zadá človek pri registrácii e-mailom, „full_name“ pošle Google.
        return listOf("meno", "full_name", "name")
            .firstNotNullOfOrNull { kluc -> udaje[kluc]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } }
    }

    override suspend fun prihlas(email: String, heslo: String) = prekladajChyby {
        auth.signInWith(Email) {
            this.email = email
            password = heslo
        }
    }

    override suspend fun zaregistruj(meno: String, email: String, heslo: String) = prekladajChyby {
        auth.signUpWith(Email) {
            this.email = email
            password = heslo
            data = buildJsonObject {
                put("meno", meno)
                put("jazyk", "sk")
            }
        }
        // Ak server vyžaduje potvrdenie e-mailu, po registrácii ešte nie je nikto prihlásený.
        if (auth.currentSessionOrNull() != null) VysledokRegistracie.PRIHLASENY
        else VysledokRegistracie.TREBA_POTVRDIT_EMAIL
    }

    override suspend fun prihlasCezGoogle(idToken: String, nonce: String) = prekladajChyby {
        auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            this.nonce = nonce
        }
    }

    override suspend fun odhlas() = prekladajChyby { auth.signOut() }

    /** Chyby zo servera preloží na [ChybaPrihlasenia], ktorej rozumejú obrazovky. */
    private suspend fun <T> prekladajChyby(akcia: suspend () -> T): T = try {
        akcia()
    } catch (e: CancellationException) {
        throw e
    } catch (e: AuthRestException) {
        throw ChybaPrihlasenia(prelozKod(e.errorCode), e)
    } catch (e: HttpRequestException) {
        throw ChybaPrihlasenia(DruhChyby.BEZ_INTERNETU, e)
    } catch (e: HttpRequestTimeoutException) {
        throw ChybaPrihlasenia(DruhChyby.BEZ_INTERNETU, e)
    } catch (e: IOException) {
        throw ChybaPrihlasenia(DruhChyby.BEZ_INTERNETU, e)
    } catch (e: Exception) {
        throw ChybaPrihlasenia(DruhChyby.NEZNAMA, e)
    }

    private fun prelozKod(kod: AuthErrorCode?): DruhChyby = when (kod) {
        AuthErrorCode.InvalidCredentials -> DruhChyby.ZLE_UDAJE
        AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> DruhChyby.UCET_EXISTUJE
        AuthErrorCode.WeakPassword -> DruhChyby.SLABE_HESLO
        AuthErrorCode.EmailNotConfirmed -> DruhChyby.NEPOTVRDENY_EMAIL
        AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> DruhChyby.VELA_POKUSOV
        else -> DruhChyby.NEZNAMA
    }
}
