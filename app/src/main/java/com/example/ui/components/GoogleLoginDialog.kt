package com.example.ui.components

import com.example.ui.components.performGoogleSignIn
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ApiKeyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleLoginDialog(
    onDismiss: () -> Unit,
    onOpenAvatarPicker: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isLoggedIn = remember { ApiKeyManager.isLoggedIn() }
    val isAlreadySignedIn by ApiKeyManager.isGoogleSignedInFlow.collectAsStateWithLifecycle()
    val currentName by ApiKeyManager.userNameFlow.collectAsStateWithLifecycle()
    val currentEmail by ApiKeyManager.userEmailFlow.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Google SSO, 1 = Email & Senha

    // Email form state
    var emailInput by remember { mutableStateOf(if (!isAlreadySignedIn && currentEmail.contains("@")) currentEmail else "") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf(if (currentName != "Estudante Crux") currentName else "") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var showTermsDetails by remember { mutableStateOf(false) }
    var showGoogleAccountChooser by remember { mutableStateOf(false) }
    var isAuthenticatingGoogle by remember { mutableStateOf(false) }
    var selectedGoogleName by remember { mutableStateOf("Bernardo Ivanowski") }
    var selectedGoogleEmail by remember { mutableStateOf("bernardoivanowski79@gmail.com") }
    var showCustomAccountInput by remember { mutableStateOf(false) }
    var customNameInput by remember { mutableStateOf("") }
    var customEmailInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {
            // Dismiss only allowed if user is already logged in
            if (ApiKeyManager.isLoggedIn()) {
                onDismiss()
            }
        },
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CruxLogo(size = 22.dp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (ApiKeyManager.isLoggedIn()) "Your Crux Tutor Account" else "Welcome to Crux Tutor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Student ID & Synchronization",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Language Selector
                var expandedLang by remember { mutableStateOf(false) }
                val currentLang by ApiKeyManager.appLanguageFlow.collectAsStateWithLifecycle()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(onClick = { expandedLang = true }) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Language", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when(currentLang) {
                                "pt" -> "Português"
                                "es" -> "Español"
                                else -> "English"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = expandedLang,
                        onDismissRequest = { expandedLang = false }
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("English") },
                            onClick = { ApiKeyManager.setLanguage("en"); expandedLang = false }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Português") },
                            onClick = { ApiKeyManager.setLanguage("pt"); expandedLang = false }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Español") },
                            onClick = { ApiKeyManager.setLanguage("es"); expandedLang = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "G",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Google SSO", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email & Password", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(vertical = 4.dp)
            ) {
                if (selectedTab == 0) {
                    // GOOGLE SSO TAB
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isAuthenticatingGoogle) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(36.dp), color = Color(0xFF4285F4))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Conectando à sua Conta do Google...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        } else if (isAlreadySignedIn) {
                            Text(
                                text = "Você está conectado com sua conta do Google.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(size = 44.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentName,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF34A853),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Text(
                                            text = currentEmail,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // BUTTON TO SWITCH GOOGLE ACCOUNT
                            Button(
                                onClick = { 
                                    isAuthenticatingGoogle = true
                                    performGoogleSignIn(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        onSuccess = { name, email ->
                                            ApiKeyManager.saveGoogleLogin(name, email)
                                            ApiKeyManager.markFirstLaunchPrompted()
                                            isAuthenticatingGoogle = false
                                            onOpenAvatarPicker()
                                        },
                                        onError = { error ->
                                            isAuthenticatingGoogle = false
                                            Toast.makeText(context, "Erro: $error", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("switch_google_account_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Trocar de Conta do Google", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { onOpenAvatarPicker() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("change_photo_after_login_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                UserAvatar(size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Alterar Foto / Avatar do Aluno")
                            }
                        } else {
                            Text(
                                text = "Faça login rápido e seguro com sua conta do Google. Sincronize seu perfil e dados de estudo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                onClick = { 
                                    isAuthenticatingGoogle = true
                                    performGoogleSignIn(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        onSuccess = { name, email ->
                                            ApiKeyManager.saveGoogleLogin(name, email)
                                            ApiKeyManager.markFirstLaunchPrompted()
                                            isAuthenticatingGoogle = false
                                            onOpenAvatarPicker()
                                        },
                                        onError = { error ->
                                            isAuthenticatingGoogle = false
                                            Toast.makeText(context, "Erro: $error", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(14.dp))
                                    .testTag("google_sso_direct_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "G",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 18.sp,
                                                color = Color(0xFF4285F4)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Fazer Login com o Google",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3C4043)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Abre a janela oficial para você selecionar ou trocar de conta do Google",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // EMAIL & SENHA TAB
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Sign in or create your account using your student email and password:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("seu.email@exemplo.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Account Password") },
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar ou ocultar senha"
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name (Optional)") },
                            placeholder = { Text("Ex: Bernardo Ivanowski") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (emailInput.contains("@") && passwordInput.length >= 4) {
                                    ApiKeyManager.saveEmailLogin(
                                        email = emailInput.trim(),
                                        name = nameInput.trim()
                                    )
                                    ApiKeyManager.markFirstLaunchPrompted()
                                    onOpenAvatarPicker()
                                }
                            },
                            enabled = emailInput.contains("@") && passwordInput.length >= 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_login_submit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Login / Register with Email", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TERMOS & PRIVACIDADE SECTION
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showTermsDetails = !showTermsDetails }
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Terms Termos & O que pegamos / NÃO pegamos What we collect / DO NOT collect",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = if (showTermsDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (showTermsDetails) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "What we collect and use:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34A853)
                            )
                            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34A853), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Your Name and Email to identify your student profile", fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34A853), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Profile Picture / Custom Avatar", fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34A853), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Local study history for score and level", fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "What we DO NOT collect (Totally Private):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Passwords of your Google account or external personal data", fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Your personal emails, contacts or private messages", fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Browsing history outside the app or credit cards", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (ApiKeyManager.isLoggedIn()) {
                Button(
                    onClick = {
                        ApiKeyManager.markFirstLaunchPrompted()
                        onDismiss()
                    }
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (ApiKeyManager.isLoggedIn()) {
                TextButton(
                    onClick = {
                        ApiKeyManager.signOut()
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}
