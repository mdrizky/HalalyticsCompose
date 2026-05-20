package com.example.halalyticscompose.ui.screens

import android.net.Uri
import java.util.Calendar
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.R
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.ui.viewmodel.*
import com.example.halalyticscompose.utils.ImageUtils

import com.example.halalyticscompose.ui.components.*
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.components.HealthSummarySection
import com.example.halalyticscompose.data.model.CategoryItem
import com.example.halalyticscompose.data.model.HealthArticleItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel(),
    healthViewModel: HealthViewModel = hiltViewModel(),
    articleViewModel: HealthArticleViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val userData by authViewModel.userData.collectAsState()
    val name = userData?.fullName?.takeIf { it.isNotBlank() } ?: userData?.username?.takeIf { it.isNotBlank() } ?: "User"
    val bmiState by healthViewModel.bmi.collectAsState()
    val bmi = if (userData?.bmi != null && userData?.bmi!! > 0) String.format("%.1f", userData?.bmi) else bmiState
    val dailyIntake by healthViewModel.dailyIntake.collectAsState()
    val categories by healthViewModel.categories.collectAsState()
    val articles by articleViewModel.articles.collectAsState()
    val isArticlesLoading by articleViewModel.isLoading.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val banners by historyViewModel.banners.collectAsState()
    
    val recommendedProducts by historyViewModel.recommendedProducts.collectAsState()
    
    // UI State for All Features Sheet
    var showAllFeaturesSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        authViewModel.loadUserProfile()
        healthViewModel.refreshHealthData() // Fetch all health related data
        historyViewModel.refreshAll()
        articleViewModel.loadArticles()
        notificationViewModel.loadNotifications()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val locationText = userData?.address?.substringBefore(",") ?: "Batam, Indonesia"
            GroceryHeader(
                name = name,
                imageUrl = userData?.image,
                unreadCount = unreadCount,
                location = locationText,
                onProfileClick = { navController.navigate("profile") },
                onNotificationClick = { navController.navigate("notifications") }
            )
        },
        floatingActionButton = {
            PulsatingFAB(onClick = { navController.navigate("health_assistant") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Health Summary
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HealthSummarySection(
                    bmi = bmi,
                    dailyIntake = dailyIntake?.dailyIntake,
                    targets = dailyIntake?.targets,
                    onDetailsClick = { navController.navigate("health_monitor") }
                )
            }

            item {
                MedicalAiDisclaimerBanner(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    compact = true
                )
            }


            // 3. Promo Banners
            item {
                Spacer(modifier = Modifier.height(24.dp))
                AutoSlidingBanner(
                    banners = banners,
                    onClick = { banner -> navigateByBannerAction(navController, banner) }
                )
            }

            // 4. Categories (Dynamic)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HomeCategorySection(
                    categories = categories,
                    onCategoryClick = { categorySlug ->
                        navController.navigate("manual_input?category=$categorySlug")
                    }
                )
            }

            // 5. Quick Actions
            item {
                Spacer(modifier = Modifier.height(24.dp))
                FeatureGridSection(
                    onActionClick = { route -> navController.navigate(route) },
                    onLainnyaClick = { showAllFeaturesSheet = true }
                )
            }

            // 6. Recommendations
            item {
                Spacer(modifier = Modifier.height(24.dp))
                RecommendationSection(
                    products = recommendedProducts,
                    onProductClick = { product -> 
                        navController.navigate("product_detail/${product.barcode}")
                    },
                    onViewAll = { navController.navigate("search_external") }
                )
            }

            // 6. Health Articles (Moved here per user request)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HealthArticlesSection(
                    articles = articles,
                    isLoading = isArticlesLoading,
                    onArticleClick = { article -> 
                        articleViewModel.setSelectedArticle(article)
                        navController.navigate("health_article_detail/${article.id}")
                    },
                    onSeeAllClick = { navController.navigate("health_articles") }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // All Features Bottom Sheet
        if (showAllFeaturesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAllFeaturesSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                AllFeaturesSheetContent(
                    navController = navController,
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showAllFeaturesSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AllFeaturesSheetContent(
    navController: NavController,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Semua Fitur",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Box(modifier = Modifier.heightIn(max = 500.dp)) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    "Pilih layanan Halalytics lainnya untuk membantu gaya hidup sehat dan halal Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                Button(
                    onClick = {
                        onClose()
                        navController.navigate("all_features")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lihat Semua Layanan")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GroceryHeader(
    name: String,
    imageUrl: String?,
    unreadCount: Int,
    location: String? = "Indonesia",
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val timeGreeting = when (hour) {
        in 4..10 -> "Good Morning"
        in 11..14 -> "Good Afternoon"
        in 15..18 -> "Good Evening"
        else -> "Good Night"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        com.example.halalyticscompose.ui.theme.EmeraldLight.copy(alpha = 0.5f),
                        Color.White
                    )
                )
            )
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar & Welcome
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(com.example.halalyticscompose.ui.theme.TealLight)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onProfileClick() }
                    ) {
                        val profileImageUrl = ImageUtils.normalizeUrl(imageUrl)
                        if (profileImageUrl != null) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                tint = com.example.halalyticscompose.ui.theme.TealDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = timeGreeting,
                            fontSize = 12.sp,
                            color = com.example.halalyticscompose.ui.theme.Slate500,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.halalyticscompose.ui.theme.Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Notification & Settings
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = com.example.halalyticscompose.ui.theme.Emerald
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location ?: "Indonesia",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = com.example.halalyticscompose.ui.theme.Slate700
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(20.dp),
                                tint = com.example.halalyticscompose.ui.theme.Slate700
                            )
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(com.example.halalyticscompose.ui.theme.Error)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AutoSlidingBanner(banners: List<Banner>, onClick: (Banner?) -> Unit) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })
    
    LaunchedEffect(banners) {
        while (true) {
            delay(4000)
            if (banners.size > 1) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
            }
        }
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(28.dp))
                .shadow(8.dp, RoundedCornerShape(28.dp))
        ) { page ->
            val banner = banners[page]
            Box(modifier = Modifier.fillMaxSize().clickable { onClick(banner) }) {
                AsyncImage(
                    model = ImageUtils.normalizeUrl(banner.image),
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = banner.title ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = banner.description ?: "",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeCategorySection(categories: List<CategoryItem>, onCategoryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 0.dp)) {
        PaddingValues(horizontal = 20.dp).let {
            Row(modifier = Modifier.padding(it)) {
                SectionTitle("Kategori Produk", null, null)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (categories.isEmpty()) {
                items(5) {
                    CategorySkeleton()
                }
            } else {
                items(categories) { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onCategoryClick(category.slug) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when(category.icon) {
                                "kitchen" -> Icons.Default.Restaurant
                                "herb" -> Icons.Default.Grass
                                "medication" -> Icons.Default.Medication
                                "spa" -> Icons.Default.Spa
                                "cookie" -> Icons.Default.Cookie
                                "local_drink" -> Icons.Default.LocalDrink
                                "egg" -> Icons.Default.Egg
                                "restaurant" -> Icons.Default.Restaurant
                                "face" -> Icons.Default.Face
                                "content_cut" -> Icons.Default.ContentCut
                                "child_care" -> Icons.Default.ChildCare
                                "bakery_dining" -> Icons.Default.BakeryDining
                                "pill" -> Icons.Default.Medication
                                "coffee" -> Icons.Default.Coffee
                                "eco" -> Icons.Default.Eco
                                else -> Icons.Default.Category
                            }
                            Icon(
                                icon,
                                contentDescription = category.name,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySkeleton() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(10.dp)
                .background(Color.Gray.copy(alpha = 0.1f))
        )
    }
}

data class CategoryData(val name: String, val icon: ImageVector, val color: Color, val id: String)

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FeatureGridSection(
    onActionClick: (String) -> Unit,
    onLainnyaClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionTitle(stringResource(R.string.home_quick_action), null, null)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(HalalyticsShadows.elevation2, RoundedCornerShape(HalalyticsDimensions.radius2XLarge)),
            shape = RoundedCornerShape(HalalyticsDimensions.radius2XLarge),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val features = listOf(
                    FeatureItemData(stringResource(R.string.feature_scan_halal), Icons.Default.QrCodeScanner, "scan", Emerald),
                    FeatureItemData(stringResource(R.string.feature_cek_obat), Icons.AutoMirrored.Filled.MenuBook, "drug_interaction", HaramRed),
                    FeatureItemData(stringResource(R.string.feature_kosmetik), Icons.Default.AutoAwesome, "skincare_scanner", Color(0xFF7B1FA2)),
                    FeatureItemData(stringResource(R.string.feature_bpom_id), Icons.Default.HealthAndSafety, "bpom_scanner", Color(0xFF0277BD)),
                    FeatureItemData(stringResource(R.string.feature_riwayat_id), Icons.Default.History, "history", Teal),
                    FeatureItemData(stringResource(R.string.feature_bmi_calculator), Icons.Default.Calculate, "bmi_calculator", TealDark),
                    FeatureItemData(stringResource(R.string.feature_ai_assistant), Icons.Default.SmartToy, "health_assistant", Color(0xFF512DA8)),
                    FeatureItemData(stringResource(R.string.feature_lainnya_id), Icons.Default.GridView, "all_features", Slate500)
                )

                features.chunked(4).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        row.forEach { item ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { if (item.route == "all_features") onLainnyaClick() else onActionClick(item.route) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(item.color.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(26.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = Slate900,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationSection(
    products: List<com.example.halalyticscompose.data.model.ProductInfo>,
    onProductClick: (com.example.halalyticscompose.data.model.ProductInfo) -> Unit,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Rekomendasi Premium", stringResource(R.string.home_see_all), onViewAll)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { onProductClick(product) }
                        .shadow(
                            elevation = com.example.halalyticscompose.ui.theme.HalalyticsShadows.elevation2,
                            shape = RoundedCornerShape(com.example.halalyticscompose.ui.theme.HalalyticsDimensions.radius2XLarge)
                        ),
                    shape = RoundedCornerShape(com.example.halalyticscompose.ui.theme.HalalyticsDimensions.radius2XLarge),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            AsyncImage(
                                model = ImageUtils.normalizeUrl(product.image) ?: "https://ui-avatars.com/api/?name=${product.name}&background=random",
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = product.name ?: "Produk",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = product.brand ?: "Local Brand",
                                style = MaterialTheme.typography.labelSmall,
                                color = com.example.halalyticscompose.ui.theme.Slate500,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthArticlesSection(
    articles: List<HealthArticleItem>,
    isLoading: Boolean = false,
    onArticleClick: (HealthArticleItem) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionTitle(stringResource(R.string.home_health_articles), stringResource(R.string.home_see_all), onSeeAllClick)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading && articles.isEmpty()) {
            repeat(3) {
                ArticleSkeleton()
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else if (articles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada artikel kesehatan tersedia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            articles.take(8).forEach { article ->
                ArticleCard(article = article, onClick = { onArticleClick(article) })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ArticleSkeleton() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray.copy(alpha = alpha)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.width(60.dp).height(10.dp).background(Color.LightGray.copy(alpha = alpha)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color.LightGray.copy(alpha = alpha)))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.width(150.dp).height(16.dp).background(Color.LightGray.copy(alpha = alpha)))
            }
        }
    }
}

@Composable
fun ArticleCard(article: HealthArticleItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageUtils.normalizeUrl(article.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.category ?: "Kesehatan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, action: String?, onAction: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}


@Composable
fun PulsatingFAB(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        )
    )
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        modifier = Modifier.scale(scale)
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
    }
}

@Composable
fun AdminDashboardCard(onNavigate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onNavigate() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Admin Control Center", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Monitor app activity & products", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(16.dp))
        }
    }
}

private fun navigateByBannerAction(navController: NavController, banner: Banner?) {
    val route = when (banner?.action_type) {
        "open_screen" -> banner.action_value
        "open_news" -> "health_articles"
        else -> "scan"
    }
    if (!route.isNullOrBlank()) navController.navigate(route)
}

data class FeatureItemData(val title: String, val icon: ImageVector, val route: String, val color: Color)
