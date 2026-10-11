package sk.weup.app.jadro.dizajn

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

/** Textové pole so zaoblenými rohmi a chybovou hláškou pod ním. */
@Composable
fun TextovePole(
    hodnota: String,
    priZmene: (String) -> Unit,
    popis: String,
    modifier: Modifier = Modifier,
    chyba: String? = null,
    klavesnica: KeyboardOptions = KeyboardOptions.Default,
    akcieKlavesnice: KeyboardActions = KeyboardActions.Default,
    zobrazenie: VisualTransformation = VisualTransformation.None,
    vpravo: (@Composable () -> Unit)? = null,
    povolene: Boolean = true,
) {
    OutlinedTextField(
        value = hodnota,
        onValueChange = priZmene,
        label = { Text(popis) },
        isError = chyba != null,
        supportingText = chyba?.let { { Text(it) } },
        singleLine = true,
        enabled = povolene,
        keyboardOptions = klavesnica,
        keyboardActions = akcieKlavesnice,
        visualTransformation = zobrazenie,
        trailingIcon = vpravo,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
