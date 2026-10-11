package sk.weup.app.funkcie.prihlasenie

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import sk.weup.app.BuildConfig
import kotlinx.coroutines.CancellationException
import java.security.MessageDigest
import java.util.UUID

/** Token od Googlu a jednorazový kód, ktorým server overí, že token patrí k tomuto pokusu. */
data class GoogleToken(val idToken: String, val nonce: String)

sealed interface VysledokGoogle {
    data class Uspech(val token: GoogleToken) : VysledokGoogle
    /** Človek okno Googlu zavrel – nie je to chyba, nič neukazujeme. */
    data object Zrusene : VysledokGoogle
    data object Chyba : VysledokGoogle
}

object GooglePrihlasenie {

    /** Tlačidlo Google sa ukáže, až keď je v server.properties vyplnený googleKlientId. */
    val jeNastavene: Boolean get() = BuildConfig.GOOGLE_KLIENT_ID.isNotBlank()

    /** Otvorí systémové okno „Prihlásiť sa cez Google“ a vráti token. */
    suspend fun ziskajToken(context: Context): VysledokGoogle {
        val nonce = UUID.randomUUID().toString()
        val moznost = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_KLIENT_ID)
            .setNonce(sha256(nonce))
            .build()
        val poziadavka = GetCredentialRequest.Builder().addCredentialOption(moznost).build()
        return try {
            val vysledok = CredentialManager.create(context).getCredential(context, poziadavka)
            val token = GoogleIdTokenCredential.createFrom(vysledok.credential.data).idToken
            VysledokGoogle.Uspech(GoogleToken(token, nonce))
        } catch (e: GetCredentialCancellationException) {
            VysledokGoogle.Zrusene
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialException) {
            VysledokGoogle.Chyba
        } catch (e: Exception) {
            VysledokGoogle.Chyba
        }
    }

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
