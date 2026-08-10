package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ApiKeyManager
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.AvatarSelectorDialog
import com.example.ui.components.CruxLogo
import com.example.ui.components.GoogleLoginDialog
import com.example.ui.components.ModelSelectorDialog
import com.example.ui.components.SupportDialog
import com.example.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToChat: (prompt: String?) -> Unit,
    onNavigateToQuizConfig: () -> Unit,
    onNavigateToStudy: () -> Unit,
    onNavigateToRevision: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedModelId by ApiKeyManager.selectedModelFlow.collectAsStateWithLifecycle()
    val userName by ApiKeyManager.userNameFlow.collectAsStateWithLifecycle()
    val currentLang by ApiKeyManager.appLanguageFlow.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelSelectorDialog by remember { mutableStateOf(false) }
    var showGoogleLoginDialog by remember { mutableStateOf(false) }
    var showAvatarSelectorDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!ApiKeyManager.isLoggedIn()) {
            showGoogleLoginDialog = true
        }
    }

    val currentModelObj = remember(selectedModelId) {
        ApiKeyManager.AVAILABLE_MODELS.firstOrNull { it.id == selectedModelId } ?: ApiKeyManager.AVAILABLE_MODELS[0]
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CruxLogo(size = 38.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Crux Tutor",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.clickable { showModelSelectorDialog = true }
                                ) {
                                    Text(
                                        text = currentModelObj.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = when (currentLang) {
                                    "pt" -> "Olá, $userName"
                                    "es" -> "Hola, $userName"
                                    else -> "Hello, $userName"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    // Language Switcher Button
                    Box {
                        IconButton(
                            onClick = { showLangMenu = true },
                            modifier = Modifier.testTag("lang_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("English 🇺🇸") },
                                onClick = { ApiKeyManager.setLanguage("en"); showLangMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Português 🇧🇷") },
                                onClick = { ApiKeyManager.setLanguage("pt"); showLangMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Español 🇪🇸") },
                                onClick = { ApiKeyManager.setLanguage("es"); showLangMenu = false }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clickable { showAvatarSelectorDialog = true }
                            .testTag("profile_top_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatar(size = 38.dp)
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("overflow_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (currentLang) {
                                            "pt" -> "Trocar Chave API Gemini"
                                            "es" -> "Cambiar Clave API Gemini"
                                            else -> "Change Gemini API Key"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showApiKeyDialog = true
                                },
                                modifier = Modifier.testTag("menu_api_key_item")
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (currentLang) {
                                            "pt" -> "Trocar Modelo IA (${currentModelObj.name})"
                                            "es" -> "Cambiar Modelo IA (${currentModelObj.name})"
                                            else -> "Switch AI Model (${currentModelObj.name})"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showModelSelectorDialog = true
                                },
                                modifier = Modifier.testTag("menu_model_item")
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (currentLang) {
                                            "pt" -> "Login com Google / Conta"
                                            "es" -> "Inicio con Google / Cuenta"
                                            else -> "Google Login / Account"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4285F4)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showGoogleLoginDialog = true
                                },
                                modifier = Modifier.testTag("menu_google_login_item")
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (currentLang) {
                                            "pt" -> "Mudar Foto de Perfil"
                                            "es" -> "Cambiar Avatar"
                                            else -> "Change Student Avatar"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showAvatarSelectorDialog = true
                                },
                                modifier = Modifier.testTag("menu_avatar_item")
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (currentLang) {
                                            "pt" -> "Suporte / Reportar Problema"
                                            "es" -> "Soporte / Reportar Problema"
                                            else -> "Support / Report Issue"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showSupportDialog = true
                                },
                                modifier = Modifier.testTag("menu_support_item")
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Futuristic Hero Banner with Overlay Callout
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_crux_hero_1786402315391),
                        contentDescription = "Crux AI Study Portal",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient Scrim Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLang) {
                                    "pt" -> "Aprenda Mais Rápido com IA"
                                    "es" -> "Aprende Más Rápido con IA"
                                    else -> "Learn Faster with AI"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (currentLang) {
                                "pt" -> "Seu Tutor Pessoal Inteligente"
                                "es" -> "Tu Tutor Personal Inteligente"
                                else -> "Your Personal Intelligent Tutor"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onNavigateToChat(null) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("hero_start_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLang) {
                                    "pt" -> "Conversar com Professor IA"
                                    "es" -> "Hablar con Tutor IA"
                                    else -> "Chat with AI Tutor"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Quick Stats Row Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatGlassBadge(
                        label = when (currentLang) {
                            "pt" -> "Quizzes"
                            "es" -> "Cuestionarios"
                            else -> "Quizzes"
                        },
                        value = "${uiState.totalQuizzes}",
                        icon = Icons.Default.Quiz,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatGlassBadge(
                        label = when (currentLang) {
                            "pt" -> "Média"
                            "es" -> "Promedio"
                            else -> "Score"
                        },
                        value = "${uiState.averageScorePercent}%",
                        icon = Icons.Default.Star,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatGlassBadge(
                        label = when (currentLang) {
                            "pt" -> "Aulas"
                            "es" -> "Lecciones"
                            else -> "Lessons"
                        },
                        value = "${uiState.totalLessons}",
                        icon = Icons.Default.Book,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Daily Tip Banner Widget
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (currentLang) {
                                    "pt" -> "Dica do Tutor"
                                    "es" -> "Consejo del Tutor"
                                    else -> "Tutor's Daily Tip"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.dailyTip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Core Navigation Cards Section
            item {
                Text(
                    text = when (currentLang) {
                        "pt" -> "Ferramentas de Aprendizado"
                        "es" -> "Herramientas de Aprendizaje"
                        else -> "Learning Tools"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                ActionCardItem(
                    title = when (currentLang) {
                        "pt" -> "Tutor IA Interativo"
                        "es" -> "Tutor IA Interactivo"
                        else -> "Interactive AI Tutor"
                    },
                    subtitle = when (currentLang) {
                        "pt" -> "Tire dúvidas, peça explicações e pratique exercícios no chat"
                        "es" -> "Resuelve dudas, pide explicaciones y practica ejercicios"
                        else -> "Ask questions, request explanations & practice in real-time"
                    },
                    icon = Icons.AutoMirrored.Filled.Chat,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    testTag = "action_chat_button",
                    onClick = { onNavigateToChat(null) }
                )
            }

            item {
                ActionCardItem(
                    title = when (currentLang) {
                        "pt" -> "Gerador de Simulados"
                        "es" -> "Generador de Cuestionarios"
                        else -> "Quiz & Exam Generator"
                    },
                    subtitle = when (currentLang) {
                        "pt" -> "Crie simulados com questões de múltipla escolha e discursivas"
                        "es" -> "Crea cuestionarios personalizados de opción múltiple y ensayo"
                        else -> "Generate custom multiple choice & essay practice quizzes"
                    },
                    icon = Icons.Default.Quiz,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    testTag = "action_quiz_button",
                    onClick = onNavigateToQuizConfig
                )
            }

            item {
                ActionCardItem(
                    title = when (currentLang) {
                        "pt" -> "Micro-Aulas Estruturadas"
                        "es" -> "Micro-Lecciones Estructuradas"
                        else -> "Structured Micro-Lessons"
                    },
                    subtitle = when (currentLang) {
                        "pt" -> "Peça aulas didáticas sobre qualquer matéria ou tópico"
                        "es" -> "Solicita lecciones didácticas sobre cualquier materia"
                        else -> "Generate bite-sized structured lessons on any topic"
                    },
                    icon = Icons.Default.School,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    testTag = "action_study_button",
                    onClick = onNavigateToStudy
                )
            }

            item {
                ActionCardItem(
                    title = when (currentLang) {
                        "pt" -> "Plano de Revisão"
                        "es" -> "Plan de Repaso"
                        else -> "Spaced Revision Plan"
                    },
                    subtitle = if (uiState.weakTopicsCount > 0)
                        when (currentLang) {
                            "pt" -> "${uiState.weakTopicsCount} tópicos recomendados para reforço!"
                            "es" -> "¡${uiState.weakTopicsCount} temas recomendados para reforzar!"
                            else -> "${uiState.weakTopicsCount} weak topics recommended for review!"
                        }
                    else when (currentLang) {
                        "pt" -> "Revise matérias aprendidas e reforce seus pontos fracos"
                        "es" -> "Repasa temas aprendidos y refuerza tus puntos débiles"
                        else -> "Review key concepts and reinforce weak areas"
                    },
                    icon = Icons.Default.Repeat,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    testTag = "action_revision_button",
                    onClick = onNavigateToRevision
                )
            }

            // Suggested Smart Prompts Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (currentLang) {
                        "pt" -> "Perguntas Rápidas Recomendadas"
                        "es" -> "Preguntas Rápidas Recomendadas"
                        else -> "Recommended Smart Prompts"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val samplePrompts = when (currentLang) {
                    "pt" -> listOf(
                        "Me explique Equação de 2º Grau de forma simples",
                        "Como funciona a Fotossíntese nas plantas?",
                        "Quais as principais regras de Crase?",
                        "Resuma a Primeira Lei de Newton com exemplos"
                    )
                    "es" -> listOf(
                        "Explícame la Ecuación Quadrática de forma sencilla",
                        "¿Cómo funciona la Fotosíntesis en las plantas?",
                        "Principales reglas de acentuación gramatical",
                        "Resume la Primera Ley de Newton con ejemplos"
                    )
                    else -> listOf(
                        "Explain Quadratic Equations in simple terms",
                        "How does Photosynthesis work in plants?",
                        "What are the main rules of punctuation and grammar?",
                        "Summarize Newton's First Law of Motion with examples"
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(samplePrompts) { prompt ->
                        Card(
                            onClick = { onNavigateToChat(prompt) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.width(230.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(onDismiss = { showApiKeyDialog = false })
    }

    if (showModelSelectorDialog) {
        ModelSelectorDialog(onDismiss = { showModelSelectorDialog = false })
    }

    if (showGoogleLoginDialog) {
        GoogleLoginDialog(
            onDismiss = { showGoogleLoginDialog = false },
            onOpenAvatarPicker = {
                showGoogleLoginDialog = false
                showAvatarSelectorDialog = true
            }
        )
    }

    if (showAvatarSelectorDialog) {
        AvatarSelectorDialog(onDismiss = { showAvatarSelectorDialog = false })
    }

    if (showSupportDialog) {
        SupportDialog(onDismiss = { showSupportDialog = false })
    }
}

@Composable
fun StatMiniBadge(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.25f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun StatGlassBadge(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.25f)
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActionCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}
