package com.example.halalyticscompose.ui.screens

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.model.HealthArticleItem
import com.example.halalyticscompose.ui.viewmodel.HealthArticleViewModel

private data class HealthArticleLocal(
    val id: String,
    val title: String,
    val excerpt: String,
    val content: String,
    val category: String,
    val imageUrl: String
)

private val fallbackArticles = listOf(
    HealthArticleLocal(
        id = "hidrasi-sehat",
        title = "Pentingnya Hidrasi: Kapan Tubuh Mulai Kekurangan Cairan?",
        excerpt = "Dehidrasi ringan bisa menurunkan fokus, energi, dan performa harian.",
        content = "Tubuh memerlukan cairan cukup untuk menjaga tekanan darah, suhu tubuh, dan fungsi organ. Tanda awal dehidrasi antara lain bibir kering, urine pekat, pusing, dan lemas. Pola sederhana: minum rutin sepanjang hari, bukan menunggu haus.",
        category = "Nutrisi",
        imageUrl = "https://images.unsplash.com/photo-1550505095-81378a674395?auto=format&fit=crop&w=800&q=80"
    ),
    HealthArticleLocal(
        id = "gula-tersembunyi",
        title = "Gula Tersembunyi di Produk Harian dan Cara Membacanya",
        excerpt = "Banyak produk kemasan tampak sehat namun tinggi gula tambahan.",
        content = "Periksa label nutrition facts dan ingredients. Nama gula bisa muncul sebagai sucrose, glucose syrup, fructose, maltodextrin, atau corn syrup. Prioritaskan produk dengan gula lebih rendah per 100g/100ml.",
        category = "Fakta Kesehatan",
        imageUrl = "https://images.unsplash.com/photo-1551024601-bec78aea704b?auto=format&fit=crop&w=800&q=80"
    ),
    HealthArticleLocal(
        id = "tidur-berkualitas",
        title = "Tidur Berkualitas: Pondasi Imunitas dan Kesehatan Metabolik",
        excerpt = "Kurang tidur berdampak pada hormon lapar, mood, dan daya tahan tubuh.",
        content = "Tidur 7-9 jam untuk dewasa membantu pemulihan jaringan, fungsi kognitif, dan stabilitas hormon. Biasakan jadwal tidur konsisten dan batasi layar sebelum tidur.",
        category = "Gaya Hidup",
        imageUrl = "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?auto=format&fit=crop&w=800&q=80"
    ),
    HealthArticleLocal(
        id = "cek-tekanan-darah",
        title = "Cek Tekanan Darah Mandiri: Angka yang Perlu Diwaspadai",
        excerpt = "Pemantauan rutin membantu deteksi dini risiko kardiovaskular.",
        content = "Tekanan darah tinggi sering tanpa gejala. Lakukan pengukuran pada waktu yang sama, posisi duduk tenang, dan alat tervalidasi.",
        category = "Pantauan Tubuh",
        imageUrl = "https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&w=800&q=80"
    ),
    HealthArticleLocal(
        id = "aman-pilih-obat",
        title = "Tips Memilih Obat OTC dengan Aman dan Tepat",
        excerpt = "Pahami kandungan aktif, dosis, dan interaksi obat-makanan.",
        content = "Baca label indikasi, kontraindikasi, serta dosis maksimal harian. Hindari penggunaan ganda bahan aktif yang sama dari dua produk berbeda.",
        category = "Obat",
        imageUrl = "https://images.unsplash.com/photo-1587370560942-ad2a04eabb6d?auto=format&fit=crop&w=800&q=80"
    )
)

private const val DEFAULT_ARTICLE_IMAGE =
    "https://images.unsplash.com/photo-1550505095-81378a674395?auto=format&fit=crop&w=800&q=80"

private fun HealthArticleItem.toUiKey(): String = slug ?: id

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthArticleListScreen(
    navController: NavController,
    viewModel: HealthArticleViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val remoteArticles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val recommended by viewModel.recommendedArticles.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadArticles(includeExternal = true)
        viewModel.loadRecommendedArticles()
    }

    val displayRemote = remember(remoteArticles, query) {
        val q = query.trim().lowercase()
        val source = if (remoteArticles.isNotEmpty()) remoteArticles else emptyList()
        if (q.isBlank()) source else source.filter {
            it.title.lowercase().contains(q) ||
                (it.excerpt ?: "").lowercase().contains(q) ||
                (it.category ?: "").lowercase().contains(q)
        }
    }

    val displayFallback = remember(query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) fallbackArticles
        else fallbackArticles.filter {
            it.title.lowercase().contains(q) || it.excerpt.lowercase().contains(q) || it.category.lowercase().contains(q)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artikel Kesehatan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari artikel kesehatan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (recommended.isNotEmpty() && query.isBlank()) {
                Text(
                    "Direkomendasikan Untukmu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommended) { article ->
                        RecommendedArticleCard(article) {
                            viewModel.setSelectedArticle(article)
                            navController.navigate("health_article_detail/${Uri.encode(article.toUiKey())}")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Semua Artikel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            when {
                isLoading && remoteArticles.isEmpty() -> {
                    ArticleStatusCard(
                        message = "Memuat artikel kesehatan terbaru...",
                        isLoading = true
                    )
                }
                remoteArticles.isEmpty() -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            ArticleStatusCard(
                                message = error ?: "Sumber utama artikel sedang tidak tersedia. Menampilkan artikel cadangan agar layar tetap terisi.",
                                isError = !error.isNullOrBlank()
                            )
                        }
                        items(displayFallback) { article ->
                            ArticleCard(
                                category = article.category,
                                title = article.title,
                                excerpt = article.excerpt,
                                imageUrl = article.imageUrl,
                                onClick = { navController.navigate("health_article_detail/${Uri.encode(article.id)}") }
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(displayRemote) { article ->
                            ArticleCard(
                                category = article.category ?: "Kesehatan",
                                title = article.title,
                                excerpt = article.excerpt ?: "",
                                imageUrl = article.imageUrl,
                                onClick = {
                                    viewModel.setSelectedArticle(article)
                                    navController.navigate("health_article_detail/${Uri.encode(article.toUiKey())}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(
    category: String,
    title: String,
    excerpt: String,
    imageUrl: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl ?: DEFAULT_ARTICLE_IMAGE,
                contentDescription = title,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = excerpt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthArticleDetailScreen(
    navController: NavController,
    articleId: String,
    viewModel: HealthArticleViewModel = hiltViewModel()
) {
    val selected by viewModel.selectedArticle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val decodedArticleId = remember(articleId) { Uri.decode(articleId) }
    val fallback = remember(decodedArticleId) { fallbackArticles.firstOrNull { it.id == decodedArticleId } }

    LaunchedEffect(decodedArticleId) {
        val current = selected
        if (current == null || (current.toUiKey() != decodedArticleId && current.id != decodedArticleId)) {
            if (fallback == null) viewModel.loadArticleDetail(decodedArticleId)
        }
    }

    val remoteArticle = selected?.takeIf { it.toUiKey() == decodedArticleId || it.id == decodedArticleId }
    val recommendations by viewModel.recommendedArticles.collectAsState()

    LaunchedEffect(decodedArticleId) {
        viewModel.loadRecommendations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Artikel", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            fallback != null -> {
                ArticleDetailContent(
                    modifier = Modifier.padding(padding),
                    category = fallback.category,
                    title = fallback.title,
                    content = fallback.content,
                    imageUrl = fallback.imageUrl,
                )
            }
            isLoading && remoteArticle == null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Memuat detail artikel...")
                }
            }
            remoteArticle != null -> {
                if (!remoteArticle.sourceUrl.isNullOrEmpty() && remoteArticle.content == remoteArticle.title) {
                     // For pure external links without much content, show WebView
                     AndroidView(
                         factory = { context ->
                             WebView(context).apply {
                                 settings.javaScriptEnabled = true
                                 settings.domStorageEnabled = true
                                 webViewClient = WebViewClient()
                                 loadUrl(remoteArticle.sourceUrl)
                             }
                         },
                         modifier = Modifier.fillMaxSize().padding(padding)
                     )
                } else {
                    ArticleDetailContent(
                        modifier = Modifier.padding(padding),
                        category = remoteArticle.category ?: "Kesehatan",
                        title = remoteArticle.title,
                        content = remoteArticle.content ?: (remoteArticle.excerpt ?: "-"),
                        imageUrl = remoteArticle.imageUrl,
                        aiSummary = remoteArticle.aiSummary,
                        sourceUrl = remoteArticle.sourceUrl,
                        recommendations = recommendations,
                        onRecommendationClick = { rec ->
                             val slug = rec.slug ?: rec.id.toString()
                             navController.navigate("health_article_detail/${Uri.encode(slug)}")
                        }
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    ArticleStatusCard(
                        message = error ?: "Artikel tidak ditemukan atau sedang tidak bisa dimuat.",
                        isError = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleStatusCard(
    message: String,
    isLoading: Boolean = false,
    isError: Boolean = false
) {
    val containerColor = when {
        isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when {
        isError -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Icon(
                    imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.Description,
                    contentDescription = null,
                    tint = contentColor
                )
            }

            Text(
                text = message,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ArticleDetailContent(
    modifier: Modifier = Modifier,
    category: String,
    title: String,
    content: String,
    imageUrl: String?,
    aiSummary: String? = null,
    sourceUrl: String? = null,
    recommendations: List<HealthArticleItem> = emptyList(),
    onRecommendationClick: (HealthArticleItem) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().then(modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box {
                AsyncImage(
                    model = imageUrl ?: DEFAULT_ARTICLE_IMAGE,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                    error = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = category.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (!aiSummary.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI TL;DR Summary",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Justify
                )
                if (!sourceUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    androidx.compose.material3.Button(
                        onClick = { /* Not used since WebView handles layout but just to be safe */ },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Baca Sumber Asli", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        if (recommendations.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Rekomendasi Untukmu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recommendations) { rec ->
                            RecommendedArticleCard(article = rec) {
                                onRecommendationClick(rec)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun RecommendedArticleCard(
    article: HealthArticleItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column {
            AsyncImage(
                model = article.imageUrl ?: DEFAULT_ARTICLE_IMAGE,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = androidx.compose.ui.graphics.painter.ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = (article.category ?: "Kesehatan").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
