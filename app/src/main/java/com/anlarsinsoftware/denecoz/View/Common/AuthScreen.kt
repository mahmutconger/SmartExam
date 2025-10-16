package com.anlarsinsoftware.denecoz.View.Common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.anlarsinsoftware.denecoz.R

// Bu ekranın hangi modda olduğunu belirtmek için bir enum
enum class AuthMode { LOGIN, REGISTER }

// Hem Login hem de Register için ortak UI State arayüzü
interface AuthUiState {
    val name: String
    val email: String
    val password: String
    val isLoading: Boolean
}

// Hem Login hem de Register için ortak Event'ler
sealed class AuthEvent {
    data class OnNameChanged(val name: String) : AuthEvent()
    data class OnEmailChanged(val email: String) : AuthEvent()
    data class OnPasswordChanged(val pass: String) : AuthEvent()
    object OnPrimaryButtonClicked : AuthEvent() // "Giriş Yap" veya "Kayıt Ol" butonu
    object OnSecondaryTextClicked : AuthEvent() // "Kayıt Ol!" veya "Giriş Yap!" metni
    // TODO: Google ve Telefon ile giriş event'leri
}

@Composable
fun AuthScreenContent(
    uiState: AuthUiState,
    mode: AuthMode,
    onEvent: (AuthEvent) -> Unit,
    nameLabel: String = "İsim Soyisim"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(50.dp))
        Text(
            // Mod'a göre başlığı değiştir
            text = if (mode == AuthMode.REGISTER) "SmartExam'a Kayıt Ol" else "SmartExam'a Giriş Yap",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(32.dp))

        // ... (SocialLoginButton'lar buraya gelecek)

        Spacer(Modifier.height(24.dp))
        SeparatorWithText("Veya")
        Spacer(Modifier.height(24.dp))

        AuthTextField(
            value = uiState.email,
            onValueChange = { onEvent(AuthEvent.OnEmailChanged(it)) },
            label = "E-posta",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(16.dp))

        // Sadece KAYIT modundaysa İsim alanını göster
        if (mode == AuthMode.REGISTER) {
            AuthTextField(
                value = uiState.name,
                onValueChange = { onEvent(AuthEvent.OnNameChanged(it)) },
                label = nameLabel,
                leadingIcon = Icons.Default.Person,
                keyboardType = KeyboardType.Text
            )
            Spacer(Modifier.height(16.dp))
        }

        AuthTextField(
            value = uiState.password,
            onValueChange = { onEvent(AuthEvent.OnPasswordChanged(it)) },
            label = "Şifre",
            leadingIcon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        // TODO: "Forgot Password?" metni buraya eklenebilir

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { onEvent(AuthEvent.OnPrimaryButtonClicked) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                // Mod'a göre buton metnini değiştir
                Text(if (mode == AuthMode.REGISTER) "Kayıt Ol" else "Giriş Yap")
            }
        }

        Spacer(Modifier.height(24.dp))
        SwitchAuthModeText(
            mode = mode,
            onClick = { onEvent(AuthEvent.OnSecondaryTextClicked) }
        )
        Spacer(Modifier.height(24.dp))
    }
}


// --- BU YARDIMCILAR DA AYNI DOSYADA KALABİLİR ---

@Composable
private fun SwitchAuthModeText(mode: AuthMode, onClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        append(if (mode == AuthMode.REGISTER) "Zaten bir hesabın var mı? " else "Hesabın yok mu? ")
        pushStringAnnotation(tag = "SWITCH", annotation = "SWITCH")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
            append(if (mode == AuthMode.REGISTER) "Giriş Yap!" else "Kayıt Ol!")
        }
        pop()
    }
    ClickableText(text = annotatedString, onClick = { offset ->
        annotatedString.getStringAnnotations(tag = "SWITCH", start = offset, end = offset)
            .firstOrNull()?.let { onClick() }
    })
}

@Composable
private fun SocialLoginButton(icon: Int?, imageVector: ImageVector? = null, text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (icon != null) {
            Icon(painterResource(id = icon), contentDescription = text, tint = Color.Unspecified)
        } else if (imageVector != null) {
            Icon(imageVector, contentDescription = text, tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SeparatorWithText(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Divider(modifier = Modifier.weight(1f))
        Text(text, modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
        Divider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LoginInsteadText(onLoginClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        append("Zaten bir hesabın var mı? ")
        pushStringAnnotation(tag = "LOGIN", annotation = "LOGIN")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
            append("Giriş Yap!")
        }
        pop()
    }
    ClickableText(text = annotatedString, onClick = { offset ->
        annotatedString.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
            .firstOrNull()?.let {
                onLoginClick()
            }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(imageVector = leadingIcon, contentDescription = label)
        },
        // Şifre alanı için özel mantık
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Şifreyi Gizle" else "Şifreyi Göster"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray,
        )
    )
}

