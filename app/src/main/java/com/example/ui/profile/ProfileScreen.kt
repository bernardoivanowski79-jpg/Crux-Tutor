package com.example.ui.profile

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ApiKeyManager
import com.example.data.StudentScheduleManager
import com.example.data.SubjectGradeItem
import com.example.data.database.QuizResultEntity
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.AvatarSelectorDialog
import com.example.ui.components.CruxLogo
import com.example.ui.components.GoogleLoginDialog
import com.example.ui.components.ModelSelectorDialog
import com.example.ui.components.SupportDialog
import com.example.ui.components.UserAvatar
import com.example.ui.home.StatMiniBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val userName by ApiKeyManager.userNameFlow.collectAsStateWithLifecycle()
    val userEmail by ApiKeyManager.userEmailFlow.collectAsStateWithLifecycle()
    val selectedModelId by ApiKeyManager.selectedModelFlow.collectAsStateWithLifecycle()
    val isGoogleSignedIn by ApiKeyManager.isGoogleSignedInFlow.collectAsStateWithLifecycle()

    val currentShift by StudentScheduleManager.shiftFlow.collectAsStateWithLifecycle()
    val subjectsList by StudentScheduleManager.subjectsFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    var selectedQuizForDetail by remember { mutableStateOf<QuizResultEntity?>(null) }

    var editingSubject by remember { mutableStateOf<SubjectGradeItem?>(null) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var notificationPlanPreview by remember { mutableStateOf<String?>(null) }

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelSelectorDialog by remember { mutableStateOf(false) }
    var showGoogleLoginDialog by remember { mutableStateOf(false) }
    var showAvatarSelectorDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    val currentLang by ApiKeyManager.appLanguageFlow.collectAsStateWithLifecycle()

    val currentModelObj = remember(selectedModelId) {
        ApiKeyManager.AVAILABLE_MODELS.firstOrNull { it.id == selectedModelId } ?: ApiKeyManager.AVAILABLE_MODELS[0]
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLang) {
                            "pt" -> "Perfil de Aprendizagem"
                            "es" -> "Perfil de Aprendizaje"
                            else -> "Learning Profile"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Profile Summary Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clickable { showAvatarSelectorDialog = true }
                        ) {
                            UserAvatar(size = 56.dp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                if (isGoogleSignedIn) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF4285F4).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Google",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4285F4),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        CruxLogo(size = 32.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Configuration Badges & Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showModelSelectorDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentModelObj.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showGoogleLoginDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4285F4),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isGoogleSignedIn) "Google OK" else "Login Google",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable { showSupportDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Suporte",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMiniBadge(label = "Simulados", value = "${state.totalQuizzes}")
                        StatMiniBadge(label = "Questões", value = "${state.totalQuestionsAnswered}")
                        StatMiniBadge(label = "Acertos", value = "${state.averageScorePercent}%")
                        StatMiniBadge(label = "Lessons", value = "${state.lessons.size}")
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Grades & Shift", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Quizzes (${state.totalQuizzes})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Lessons (${state.lessons.size})") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Progress") }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Grade Curricular, Turno e Notificações de Revisão
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }

                            // TURN SELECTION CARD
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Your Study Shift",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = "Sets the schedule and notification routine for your lessons",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            StudentScheduleManager.AVAILABLE_SHIFTS.forEach { shift ->
                                                val isSelected = currentShift == shift
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { StudentScheduleManager.setShift(shift) },
                                                    label = {
                                                        Text(
                                                            text = shift,
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // DAILY NOTIFICATION BUTTON CARD
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Notification for Shift $currentShift",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                Text(
                                                    text = "Triggers a status bar reminder with your lessons and what to review.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                val planText = StudentScheduleManager.triggerDailyNotification(context)
                                                notificationPlanPreview = planText
                                                Toast.makeText(context, "Notificação de estudo enviada!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("trigger_study_notification_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Send Notification with Lessons & Review")
                                        }
                                    }
                                }
                            }

                            // SUBJECTS & GRADES HEADER
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Curriculum & Grades",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Grade -1 = no grade / not taken",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = { showAddSubjectDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("add_subject_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", fontSize = 12.sp)
                                    }
                                }
                            }

                            // SUBJECT ITEMS LIST
                            items(subjectsList) { subject ->
                                SubjectGradeCard(
                                    item = subject,
                                    onEdit = { editingSubject = subject },
                                    onDelete = { StudentScheduleManager.removeSubject(subject.id) }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                    1 -> { // Quizzes
                        if (state.quizResults.isEmpty()) {
                            EmptyStatePlaceholder("No quizzes taken yet.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                items(state.quizResults) { quizRes ->
                                    val pct = if (quizRes.totalQuestions > 0) (quizRes.score * 100) / quizRes.totalQuestions else 0
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(quizRes.timestamp))

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedQuizForDetail = quizRes },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = quizRes.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = "${quizRes.subject} • ${quizRes.difficulty} • $dateStr", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = "${quizRes.score}/${quizRes.totalQuestions} ($pct%)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                    2 -> { // Lessons
                        if (state.lessons.isEmpty()) {
                            EmptyStatePlaceholder("No lessons saved yet.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                items(state.lessons) { lesson ->
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(lesson.createdAt))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = lesson.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = "${lesson.subject} • $dateStr", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                    3 -> { // Progress
                        if (state.progressList.isEmpty()) {
                            EmptyStatePlaceholder("No accumulated progress records.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                items(state.progressList) { prog ->
                                    val pct = if (prog.totalQuestions > 0) (prog.correctCount * 100) / prog.totalQuestions else 0
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = "${prog.subject}: ${prog.topic}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = "Attempts: ${prog.attemptsCount} | Total score: ${prog.correctCount}/${prog.totalQuestions}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(
                                                text = "$pct%",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = if (pct >= 70) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }

        // Quiz Detail Dialog Popup
        if (selectedQuizForDetail != null) {
            val q = selectedQuizForDetail!!
            AlertDialog(
                onDismissRequest = { selectedQuizForDetail = null },
                title = { Text(text = q.title, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Matéria: ${q.subject}", fontWeight = FontWeight.SemiBold)
                        Text(text = "Dificuldade: ${q.difficulty}")
                        Text(text = "Pontuação: ${q.score} de ${q.totalQuestions} questões")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedQuizForDetail = null }) {
                        Text("Fechar")
                    }
                }
            )
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

        if (showAddSubjectDialog) {
            AddOrEditSubjectDialog(
                initialItem = null,
                onDismiss = { showAddSubjectDialog = false },
                onConfirm = { name, grade, topics ->
                    StudentScheduleManager.addSubject(name, grade, topics)
                }
            )
        }

        if (editingSubject != null) {
            AddOrEditSubjectDialog(
                initialItem = editingSubject,
                onDismiss = { editingSubject = null },
                onConfirm = { name, grade, topics ->
                    StudentScheduleManager.updateSubjectGrade(editingSubject!!.id, grade, topics)
                }
            )
        }

        if (notificationPlanPreview != null) {
            AlertDialog(
                onDismissRequest = { notificationPlanPreview = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Plano de Revisão Disparado!", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Uma notificação da barra de status foi enviada para o Android com o resumo do seu turno ($currentShift):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(2.dp)
                        ) {
                            Text(
                                text = notificationPlanPreview!!,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { notificationPlanPreview = null }) {
                        Text("OK, Excelente!")
                    }
                }
            )
        }
    }
}

@Composable
fun SubjectGradeCard(
    item: SubjectGradeItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            item.grade == -1.0 -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            item.grade < 7.0 -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = if (item.grade == -1.0) "Sem Nota (-1)" else "Nota: ${item.grade}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = when {
                                item.grade == -1.0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                item.grade < 7.0 -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (item.topicsToReview.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Revisar: ${item.topicsToReview}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddOrEditSubjectDialog(
    initialItem: SubjectGradeItem?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, grade: Double, topics: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(initialItem?.name ?: "") }
    var gradeInput by remember { mutableStateOf(if (initialItem == null || initialItem.grade == -1.0) "-1" else initialItem.grade.toString()) }
    var topicsInput by remember { mutableStateOf(initialItem?.topicsToReview ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "Add Matéria" else "Edit Matéria",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nome da Matéria") },
                    placeholder = { Text("Ex: Física, Geografia, Robótica...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = gradeInput,
                    onValueChange = { gradeInput = it },
                    label = { Text("Nota da Matéria (-1 para sem nota)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { gradeInput = "-1" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("-1 (Sem Nota)", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = { gradeInput = "6.0" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("6.0", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = { gradeInput = "8.5" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("8.5", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = topicsInput,
                    onValueChange = { topicsInput = it },
                    label = { Text("Tópicos / Lessons para revisar") },
                    placeholder = { Text("Ex: Álgebra, Equações, Leis de Newton...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedGrade = gradeInput.toDoubleOrNull() ?: -1.0
                    if (nameInput.isNotBlank()) {
                        onConfirm(nameInput.trim(), parsedGrade, topicsInput.trim())
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
