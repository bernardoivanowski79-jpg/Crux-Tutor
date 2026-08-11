package com.example.ui.news

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ApiKeyManager
import com.example.data.database.NewsEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsPublisherDialog(
    newsViewModel: NewsViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Manual, 1 = IA Auto, 2 = Gerenciar
    val isGeneratingAi by newsViewModel.isGeneratingAiNews.collectAsStateWithLifecycle()
    val allNews by newsViewModel.newsList.collectAsStateWithLifecycle()
    val currentUserName by ApiKeyManager.userNameFlow.collectAsStateWithLifecycle()
    val currentLang by ApiKeyManager.appLanguageFlow.collectAsStateWithLifecycle()

    // Manual form state
    var manualTitle by remember { mutableStateOf("") }
    var manualSummary by remember { mutableStateOf("") }
    var manualContent by remember { mutableStateOf("") }
    var manualCategory by remember { mutableStateOf("ENEM") }
    var manualAuthor by remember { mutableStateOf(currentUserName) }

    // AI Generation state
    var aiThemePrompt by remember { mutableStateOf("") }
    var aiCategory by remember { mutableStateOf("ENEM") }

    val categories = listOf("ENEM", "Vestibular", "IA & Tecnologia", "Dicas de Estudo", "Geral")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Newspaper,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = when (currentLang) {
                                    "pt" -> "Portal do Publisher"
                                    "es" -> "Portal del Publicador"
                                    else -> "News Publisher Hub"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = when (currentLang) {
                                    "pt" -> "Publicar & Gerenciar Notícias"
                                    "es" -> "Publicar y Gestionar Noticias"
                                    else -> "Publish & Manage News"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (currentLang == "pt") "Manual" else "Manual")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFB703), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gerar com IA")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Minhas (${allNews.size})")
                            }
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                if (selectedTab == 0) {
                    // MANUAL PUBLISHING FORM
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (currentLang == "pt") "Preencha os campos para publicar uma nova notícia:" else "Fill in the fields to publish a news article:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = manualTitle,
                            onValueChange = { manualTitle = it },
                            label = { Text(if (currentLang == "pt") "Título da Notícia" else "News Title") },
                            placeholder = { Text("Ex: Novo Calendário do Vestibular 2026") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("news_manual_title_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (currentLang == "pt") "Categoria:" else "Category:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                FilterChip(
                                    selected = manualCategory == cat,
                                    onClick = { manualCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = manualAuthor,
                            onValueChange = { manualAuthor = it },
                            label = { Text(if (currentLang == "pt") "Autor / Redação" else "Author") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = manualSummary,
                            onValueChange = { manualSummary = it },
                            label = { Text(if (currentLang == "pt") "Resumo Curto (1 a 2 frases)" else "Short Summary") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = manualContent,
                            onValueChange = { manualContent = it },
                            label = { Text(if (currentLang == "pt") "Conteúdo Completo da Notícia" else "Full Content") },
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (manualTitle.isNotBlank() && manualContent.isNotBlank()) {
                                    newsViewModel.publishNewsManual(
                                        title = manualTitle.trim(),
                                        summary = manualSummary.trim().ifBlank { manualTitle },
                                        content = manualContent.trim(),
                                        category = manualCategory,
                                        authorName = manualAuthor.trim()
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = manualTitle.isNotBlank() && manualContent.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_news_manual_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (currentLang == "pt") "Publicar Notícia Agora" else "Publish News Now", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (selectedTab == 1) {
                    // AI AUTO GENERATION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (currentLang == "pt") "Gerador de Notícias com IA Gemini" else "AI Gemini News Generator",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (currentLang == "pt") "Informe o tema e a IA escreverá o título, resumo e artigo completo." else "Enter the topic and AI will generate title, summary and article.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = aiThemePrompt,
                            onValueChange = { aiThemePrompt = it },
                            label = { Text(if (currentLang == "pt") "Tema ou Tópico da Notícia" else "News Topic or Prompt") },
                            placeholder = { Text("Ex: Dicas para a prova de Redação do ENEM 2026") },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_news_prompt_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (currentLang == "pt") "Categoria Pretendida:" else "Target Category:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                FilterChip(
                                    selected = aiCategory == cat,
                                    onClick = { aiCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isGeneratingAi) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (currentLang == "pt") "Gemini escrevendo o artigo completo..." else "Gemini is writing the article...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (aiThemePrompt.isNotBlank()) {
                                        newsViewModel.generateNewsWithAi(
                                            theme = aiThemePrompt.trim(),
                                            category = aiCategory
                                        )
                                    }
                                },
                                enabled = aiThemePrompt.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_ai_news_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLang == "pt") "Gerar & Publicar Notícia com IA" else "Generate & Publish with AI",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // MANAGE PUBLISHED NEWS LIST
                    if (allNews.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentLang == "pt") "Nenhuma notícia publicada ainda." else "No news published yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allNews, key = { it.id }) { item ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = item.category,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (item.isAiGenerated) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = "IA",
                                                        tint = Color(0xFFFFB703),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "Por ${item.authorName} • ${item.dateFormatted}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { newsViewModel.deleteNews(item.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLang == "pt") "Concluído" else "Done")
            }
        }
    )
}
