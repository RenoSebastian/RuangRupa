package com.example.ruangai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruangai.ui.navigation.BottomTab
import com.example.ruangai.ui.navigation.Screen
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        color = WarmLinen,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CharcoalDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = WarmCamelAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isRegisterMode) "Buat Akun RuangAI" else "Selamat Datang Kembali",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isRegisterMode) "Dapatkan 3 kredit render gratis untuk ruangan Anda" else "Masuk untuk melanjutkan desain dan daftar belanja Anda",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Form Fields
            if (isRegisterMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CharcoalDark,
                        unfocusedBorderColor = OutlineWarm,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_input")
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CharcoalDark,
                    unfocusedBorderColor = OutlineWarm,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Kata Sandi") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CharcoalDark,
                    unfocusedBorderColor = OutlineWarm,
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input")
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = TerracottaRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Mohon isi email dan kata sandi Anda."
                    } else {
                        errorMessage = null
                        isLoading = true
                        onAuthSuccess(Screen.Main(BottomTab.HOME))
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_submit_btn")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PureWhite, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = if (isRegisterMode) "Daftar Akun" else "Masuk",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign-In (Simulated)
            OutlinedButton(
                onClick = {
                    onAuthSuccess(Screen.Main(BottomTab.HOME))
                },
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineWarm),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = PureWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_google_btn")
            ) {
                Text(
                    text = "Lanjutkan dengan Google",
                    color = CharcoalDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Toggle Mode
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isRegisterMode) "Sudah punya akun? " else "Belum punya akun? ",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = if (isRegisterMode) "Masuk" else "Daftar",
                    color = WarmCamelAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable {
                            isRegisterMode = !isRegisterMode
                            errorMessage = null
                        }
                        .testTag("auth_toggle_mode")
                )
            }
        }
    }
}
