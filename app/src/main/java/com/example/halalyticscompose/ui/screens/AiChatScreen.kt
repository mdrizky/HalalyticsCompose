package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.AiChatViewModel
import com.example.halalyticscompose.ui.viewmodel.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavController,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Halalytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Column {
                if (state.messages.isEmpty()) {
                    // Suggestion Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val suggestions = listOf(
                            "Tanya resep sehat",
                            "Cek label makanan",
                            "Apa itu E471?",
                            "Alternatif gula sehat"
                        )
                        items(suggestions) { suggestion ->
                            SuggestionChip(
                                onClick = { 
                                    input = suggestion
                                },
                                label = { Text(suggestion) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = EmeraldLight.copy(alpha = 0.1f)
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = Emerald.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tanya seputar halal & gizi...", color = Slate400) },
                        enabled = !state.loading,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = Slate50,
                            unfocusedContainerColor = Slate50
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (input.isNotBlank() && !state.loading) Emerald else Slate200)
                            .clickable(enabled = input.isNotBlank() && !state.loading) {
                                viewModel.send(input.trim())
                                input = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        containerColor = Slate50
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(EmeraldLight.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 40.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Halo! Saya AI Halalytics.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate900
                        )
                        Text(
                            "Siap membantu pertanyaan nutrisi & kehalalan Anda.",
                            color = Slate500,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(state.messages) { msg ->
                    ChatBubble(msg)
                }
                if (state.loading) {
                    item {
                        TypingAnimationBubble()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    val bg = if (isUser) Emerald else Color.White
    val fg = if (isUser) Color.White else Slate900
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bg)
                .padding(16.dp)
        ) {
            Text(msg.text, color = fg, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun TypingAnimationBubble() {
    val infiniteTransition = rememberInfiniteTransition()
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 0), RepeatMode.Reverse)
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 200), RepeatMode.Reverse)
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500, delayMillis = 400), RepeatMode.Reverse)
    )

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Emerald.copy(alpha = dot1Alpha)))
                Box(Modifier.size(8.dp).clip(CircleShape).background(Emerald.copy(alpha = dot2Alpha)))
                Box(Modifier.size(8.dp).clip(CircleShape).background(Emerald.copy(alpha = dot3Alpha)))
            }
        }
    }
}
