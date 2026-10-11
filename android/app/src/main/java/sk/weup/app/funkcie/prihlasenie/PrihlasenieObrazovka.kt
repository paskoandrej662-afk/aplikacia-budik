package sk.weup.app.funkcie.prihlasenie

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import sk.weup.app.R
import sk.weup.app.jadro.dizajn.HlavneTlacidlo
import sk.weup.app.jadro.dizajn.OkrajObrazovky
import sk.weup.app.jadro.dizajn.TextovePole
import sk.weup.app.jadro.dizajn.VedlajsieTlacidlo
import sk.weup.app.jadro.dizajn.WeupTema
import sk.weup.app.jadro.server.DruhChyby

/** Obrazovka prihlásenia a registrácie (e-mail a heslo, alebo cez Google). */
@Composable
fun PrihlasenieObrazovka(riadic: PrihlasenieRiadic) {
    val stav by riadic.stav.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val korutiny = rememberCoroutineScope()

    PrihlasenieObsah(
        stav = stav,
        googleNastavene = GooglePrihlasenie.jeNastavene,
        priZmeneMena = riadic::zmenMeno,
        priZmeneEmailu = riadic::zmenEmail,
        priZmeneHesla = riadic::zmenHeslo,
        odosli = riadic::odosli,
        prepniRezim = riadic::prepniRezim,
        prihlasCezGoogle = {
            korutiny.launch {
                when (val vysledok = GooglePrihlasenie.ziskajToken(context)) {
                    is VysledokGoogle.Uspech -> riadic.prihlasCezGoogle(vysledok.token.idToken, vysledok.token.nonce)
                    VysledokGoogle.Chyba -> riadic.chybaGoogle()
                    VysledokGoogle.Zrusene -> Unit
                }
            }
        },
    )
}

@Composable
private fun PrihlasenieObsah(
    stav: PrihlasenieStav,
    googleNastavene: Boolean,
    priZmeneMena: (String) -> Unit,
    priZmeneEmailu: (String) -> Unit,
    priZmeneHesla: (String) -> Unit,
    odosli: () -> Unit,
    prepniRezim: () -> Unit,
    prihlasCezGoogle: () -> Unit,
) {
    val registracia = stav.rezim == Rezim.REGISTRACIA
    val fokus = LocalFocusManager.current
    var hesloViditelne by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OkrajObrazovky, vertical = 32.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(if (registracia) R.string.registracia_nadpis else R.string.prihlasenie_nadpis),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(if (registracia) R.string.registracia_podnadpis else R.string.prihlasenie_podnadpis),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(visible = stav.potvrdEmail) {
                Oznam(stringResource(R.string.info_potvrd_email), chyba = false)
            }

            AnimatedVisibility(visible = registracia) {
                TextovePole(
                    hodnota = stav.meno,
                    priZmene = priZmeneMena,
                    popis = stringResource(R.string.pole_meno),
                    chyba = stav.chybaMena?.let { textChyby(it) },
                    klavesnica = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    povolene = !stav.nacitava,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .semantics { contentType = ContentType.PersonFirstName },
                )
            }

            TextovePole(
                hodnota = stav.email,
                priZmene = priZmeneEmailu,
                popis = stringResource(R.string.pole_email),
                chyba = stav.chybaEmailu?.let { textChyby(it) },
                klavesnica = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                povolene = !stav.nacitava,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .semantics { contentType = ContentType.EmailAddress },
            )

            TextovePole(
                hodnota = stav.heslo,
                priZmene = priZmeneHesla,
                popis = stringResource(R.string.pole_heslo),
                chyba = stav.chybaHesla?.let { textChyby(it) },
                klavesnica = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                akcieKlavesnice = KeyboardActions(onDone = {
                    fokus.clearFocus()
                    odosli()
                }),
                zobrazenie = if (hesloViditelne) VisualTransformation.None else PasswordVisualTransformation(),
                vpravo = {
                    TextButton(onClick = { hesloViditelne = !hesloViditelne }) {
                        Text(stringResource(if (hesloViditelne) R.string.heslo_skryt else R.string.heslo_zobrazit))
                    }
                },
                povolene = !stav.nacitava,
                modifier = Modifier.semantics {
                    contentType = if (registracia) ContentType.NewPassword else ContentType.Password
                },
            )

            AnimatedVisibility(visible = stav.chybaServera != null) {
                stav.chybaServera?.let { Oznam(textChyby(it), chyba = true) }
            }

            Spacer(Modifier.height(24.dp))
            HlavneTlacidlo(
                text = stringResource(if (registracia) R.string.tlacidlo_vytvorit_ucet else R.string.tlacidlo_prihlasit),
                onClick = {
                    fokus.clearFocus()
                    odosli()
                },
                nacitava = stav.nacitava,
            )

            if (googleNastavene) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 20.dp),
                ) {
                    HorizontalDivider(Modifier.weight(1f))
                    Text(
                        stringResource(R.string.alebo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    HorizontalDivider(Modifier.weight(1f))
                }
                VedlajsieTlacidlo(
                    text = stringResource(R.string.tlacidlo_google),
                    onClick = prihlasCezGoogle,
                    povolene = !stav.nacitava,
                )
            }

            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = prepniRezim,
                enabled = !stav.nacitava,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(if (registracia) R.string.prepnut_na_prihlasenie else R.string.prepnut_na_registraciu),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

/** Zvýraznený oznam v karte (chyba zo servera alebo informácia). */
@Composable
private fun Oznam(text: String, chyba: Boolean) {
    val farba = if (chyba) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(farba.copy(alpha = 0.12f), MaterialTheme.shapes.medium)
            .padding(16.dp),
    )
}

@Composable
private fun textChyby(chyba: ChybaPola): String = when (chyba) {
    ChybaPola.MENO_PRAZDNE -> stringResource(R.string.chyba_meno_prazdne)
    ChybaPola.EMAIL_NEPLATNY -> stringResource(R.string.chyba_email_neplatny)
    ChybaPola.HESLO_KRATKE -> stringResource(R.string.chyba_heslo_kratke, KontrolaUdajov.MIN_DLZKA_HESLA)
}

@Composable
private fun textChyby(chyba: DruhChyby): String = stringResource(
    when (chyba) {
        DruhChyby.ZLE_UDAJE -> R.string.chyba_zle_udaje
        DruhChyby.UCET_EXISTUJE -> R.string.chyba_ucet_existuje
        DruhChyby.SLABE_HESLO -> R.string.chyba_slabe_heslo
        DruhChyby.NEPOTVRDENY_EMAIL -> R.string.chyba_nepotvrdeny_email
        DruhChyby.VELA_POKUSOV -> R.string.chyba_vela_pokusov
        DruhChyby.BEZ_INTERNETU -> R.string.chyba_bez_internetu
        DruhChyby.GOOGLE -> R.string.chyba_google
        DruhChyby.NEZNAMA -> R.string.chyba_neznama
    },
)

@Preview(showBackground = true)
@Composable
private fun NahladPrihlasenia() = WeupTema {
    PrihlasenieObsah(
        stav = PrihlasenieStav(email = "anna@", chybaEmailu = ChybaPola.EMAIL_NEPLATNY),
        googleNastavene = true,
        priZmeneMena = {}, priZmeneEmailu = {}, priZmeneHesla = {},
        odosli = {}, prepniRezim = {}, prihlasCezGoogle = {},
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NahladRegistracieTmavy() = WeupTema(tmavy = true) {
    PrihlasenieObsah(
        stav = PrihlasenieStav(rezim = Rezim.REGISTRACIA, chybaServera = DruhChyby.UCET_EXISTUJE),
        googleNastavene = true,
        priZmeneMena = {}, priZmeneEmailu = {}, priZmeneHesla = {},
        odosli = {}, prepniRezim = {}, prihlasCezGoogle = {},
    )
}
