@file:Suppress("DEPRECATION")

package com.example.halalyticscompose.feature.expansion.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.feature.expansion.model.CommunityComment
import com.example.halalyticscompose.feature.expansion.model.CommunityLeaderboardEntry
import com.example.halalyticscompose.feature.expansion.model.CommunityPost
import com.example.halalyticscompose.feature.expansion.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val posts by viewModel.posts.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()
    val showComposer by viewModel.showComposer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showLeaderboard by remember { mutableStateOf(false) }

    val categories = listOf(
        null to "Semua",
        "diskusi" to "Diskusi",
        "tips" to "Tips",
        "progress" to "Progress",
        "resep" to "Resep",
        "tanya" to "Tanya",
    )

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { 
                    Text(
                        "Community Hub", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showLeaderboard = true
                        viewModel.loadLeaderboard()
                    }) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFB300))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.toggleComposer() },
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Buat Post") },
                shape = RoundedCornerShape(16.dp)
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(categories) { (type, label) ->
                        FilterChip(
                            selected = activeCategory == type,
                            onClick = { viewModel.loadPosts(type) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00C853),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF5F5F5),
                                labelColor = Color.Gray
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (posts.isEmpty()) {
                CommunityEmptyStateCard(
                    title = "Belum ada diskusi",
                    message = error ?: "Belum ada postingan pada kategori ini. Yuk jadi yang pertama berbagi pengalaman atau pertanyaan.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(posts, key = { it.id }) { post ->
                        CommunityPostCard(
                            post = post,
                            onLike = { viewModel.likePost(post.id) },
                            onOpen = { navController.navigate("community_post/${post.id}") },
                        )
                    }
                }
            }
        }
    }

    if (showComposer) {
        CreatePostBottomSheet(
            onDismiss = { viewModel.toggleComposer() },
            onSubmit = { title, content, category, imageUri, context ->
                viewModel.submitPost(content, category, imageUri, context, title)
            },
        )
    }

    if (showLeaderboard) {
        LeaderboardBottomSheet(entries = leaderboard, onDismiss = { showLeaderboard = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostDetailScreen(
    postId: Int,
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val post by viewModel.selectedPost.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Postingan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Balas diskusi ini...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(postId, commentText.trim())
                            commentText = ""
                        }
                    },
                ) {
                    Text("Kirim")
                }
            }
        },
    ) { padding ->
        if (isLoading && post == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (post == null) {
            CommunityEmptyStateCard(
                title = "Postingan belum tersedia",
                message = error ?: "Detail postingan belum bisa dimuat saat ini. Coba buka ulang beberapa saat lagi.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    CommunityPostCard(
                        post = CommunityPost(
                            id = post!!.id,
                            userId = post!!.userId,
                            userName = post!!.userName,
                            userPhoto = post!!.userPhoto,
                            userBadge = post!!.userBadge,
                            userLevel = post!!.userLevel,
                            title = post!!.title,
                            content = post!!.content,
                            imageUrl = post!!.imageUrl,
                            category = post!!.category,
                            hashtags = post!!.hashtags,
                            likesCount = post!!.likesCount,
                            commentsCount = post!!.commentsCount,
                            isPinned = post!!.isPinned,
                            isLikedByMe = post!!.isLikedByMe,
                            createdAt = post!!.createdAt,
                        ),
                        onLike = { viewModel.likePost(postId) },
                        onOpen = {},
                    )
                }
                items(post!!.comments, key = { it.id }) { comment ->
                    CommentCard(comment)
                }
            }
        }
    }
}

@Composable
private fun CommunityEmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPost,
    onLike: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (post.isPinned) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (post.isPinned) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.PushPin, 
                        contentDescription = null, 
                        tint = Color(0xFF00C853), 
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Disematkan", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color(0xFF00C853),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F8E9)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (post.userPhoto.isNullOrBlank()) {
                        Text(
                            post.userName.take(1).uppercase(), 
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00C853)
                        )
                    } else {
                        AsyncImage(
                            model = post.userPhoto, 
                            contentDescription = null, 
                            modifier = Modifier.fillMaxSize(), 
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        post.userName, 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        post.createdAt, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.Gray
                    )
                }
                Surface(
                    color = Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = post.category.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color(0xFF00C853),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            post.title?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    it, 
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                post.content, 
                maxLines = 4, 
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )

            post.imageUrl?.let {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            if (post.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.hashtags.forEach { hashtag ->
                        Text(
                            "#$hashtag", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color(0xFF00C853),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (post.isLikedByMe) Color.Red else Color.Gray,
                        )
                    }
                    Text(
                        "${post.likesCount}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    IconButton(onClick = onOpen) {
                        Icon(
                            Icons.Default.ChatBubbleOutline, 
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    Text(
                        "${post.commentsCount}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentCard(comment: CommunityComment) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(comment.userName, fontWeight = FontWeight.Bold)
            Text(comment.content, modifier = Modifier.padding(top = 4.dp))
            Text(
                text = comment.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (comment.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                comment.replies.forEach { reply ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(reply.userName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                            Text(reply.content, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (String?, String, String, Uri?, Context) -> Unit,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("diskusi") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Buat Postingan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul opsional") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Isi postingan") },
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("diskusi", "tips", "progress", "resep", "tanya").forEach { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = { category = item },
                        label = { Text(item.replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Gambar")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onSubmit(title.takeIf { it.isNotBlank() }, content, category, imageUri, context) },
                enabled = content.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Posting")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaderboardBottomSheet(
    entries: List<CommunityLeaderboardEntry>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Leaderboard Komunitas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            entries.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${index + 1}", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.userName, fontWeight = FontWeight.SemiBold)
                        Text(entry.level, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(
                            "${entry.totalPoints} poin",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
