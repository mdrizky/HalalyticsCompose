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
import com.example.halalyticscompose.ui.components.*
import com.example.halalyticscompose.ui.components.HealthSummarySection
import com.example.halalyticscompose.data.model.CategoryItem
import com.example.halalyticscompose.data.model.HealthArticleItem
import kotlinx.coroutines.delay

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
    
    // PREMIUM RECOMMENDATIONS: Real data for demo
    val recommendedProducts = remember {
        listOf(
            com.example.halalyticscompose.data.model.ProductInfo(
                id = 1,
                barcode = "8996001600016",
                name = "Indomie Goreng Special",
                brand = "Indofood",
                category = "Food",
                image = "https://images.openfoodfacts.org/images/products/899/600/160/0016/front_en.6.400.jpg"
            ),
            com.example.halalyticscompose.data.model.ProductInfo(
                id = 2,
                barcode = "7622210440532",
                name = "Oreo Sandwich Cookies",
                brand = "Mondelez",
                category = "Food",
                image = "https://images.openfoodfacts.org/images/products/762/221/044/0532/front_en.12.400.jpg"
            ),
            com.example.halalyticscompose.data.model.ProductInfo(
                id = 3,
                barcode = "8992761121016",
                name = "SilverQueen Milk Chocolate",
                brand = "SilverQueen",
                category = "Food",
                image = "https://images.openfoodfacts.org/images/products/899/276/112/1016/front_id.10.400.jpg"
            ),
            com.example.halalyticscompose.data.model.ProductInfo(
                id = 4,
                barcode = "8991001111166",
                name = "Kopi Kapal Api Special",
                brand = "Kapal Api",
                category = "Drink",
                image = "https://images.openfoodfacts.org/images/products/899/100/111/1166/front_id.7.400.jpg"
            )
        )
    }
    
    var showAllFeaturesSheet by remember { mutableStateOf(false) }

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
                    onDetailsClick = { navController.navigate("user_stats") }
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
                    onViewAll = { navController.navigate("explore_products") }
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

        if (showAllFeaturesSheet) {
            AllFeaturesSheet(
                onDismiss = { showAllFeaturesSheet = false },
                onNavigate = { route ->
                    showAllFeaturesSheet = false
                    navController.navigate(route)
                }
            )
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
        in 4..10 -> "Selamat Pagi"
        in 11..14 -> "Selamat Siang"
        in 15..18 -> "Selamat Sore"
        else -> "Selamat Malam"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.halalyticscompose.R.drawable.logo_halalytics),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = location ?: "Indonesia",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Selamat Datang, $name",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$timeGreeting, mau cek apa hari ini?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }

        Box {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (unreadCount > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(if (unreadCount > 9) "9+" else unreadCount.toString())
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onProfileClick() }
        ) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                    model = banner.image,
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val features = listOf(
                    FeatureItemData(stringResource(R.string.feature_scan_halal), Icons.Default.QrCodeScanner, "scan", MaterialTheme.colorScheme.primary),
                    FeatureItemData(stringResource(R.string.feature_cek_obat), Icons.Default.Medication, "drug_interaction", Color(0xFFD32F2F)),
                    FeatureItemData(stringResource(R.string.feature_kosmetik), Icons.Default.AutoAwesome, "skincare_scanner", Color(0xFF7B1FA2)),
                    FeatureItemData(stringResource(R.string.feature_bpom_id), Icons.Default.HealthAndSafety, "bpom_scanner", Color(0xFF0277BD)),
                    FeatureItemData(stringResource(R.string.feature_riwayat_id), Icons.Default.History, "history", MaterialTheme.colorScheme.secondary),
                    FeatureItemData(stringResource(R.string.feature_bmi_calculator), Icons.Default.Calculate, "bmi_calculator", Color(0xFF00897B)),
                    FeatureItemData(stringResource(R.string.feature_ai_assistant), Icons.Default.SmartToy, "health_assistant", Color(0xFF512DA8)),
                    FeatureItemData(stringResource(R.string.feature_lainnya_id), Icons.Default.GridView, "all_features", MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    color = MaterialTheme.colorScheme.onSurface,
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
        SectionTitle("Rekomendasi Halal", stringResource(R.string.home_see_all), onViewAll)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { onProductClick(product) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            AsyncImage(
                                model = product.image ?: "https://ui-avatars.com/api/?name=${product.name}&background=random",
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            articles.take(4).forEach { article ->
                ArticleCard(article = article, onClick = { onArticleClick(article) })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ArticleSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray.copy(alpha = 0.3f)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.width(60.dp).height(10.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.width(150.dp).height(16.dp).background(Color.LightGray.copy(alpha = 0.3f)))
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
                    model = article.imageUrl,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeaturesSheet(onDismiss: () -> Unit, onNavigate: (String) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.all_features_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val features = listOf(
                FeatureItemData(stringResource(R.string.feature_scan_halal), Icons.Default.QrCode2, "scan", MaterialTheme.colorScheme.primary),
                FeatureItemData(stringResource(R.string.feature_bpom), Icons.Default.HealthAndSafety, "bpom_scanner", Color(0xFF0277BD)),
                FeatureItemData(stringResource(R.string.feature_cosmetic), Icons.Default.AutoAwesome, "skincare_scanner", Color(0xFF7B1FA2)),
                FeatureItemData(stringResource(R.string.feature_check_medicine), Icons.Default.Medication, "drug_interaction", Color(0xFFD32F2F)),
                FeatureItemData(stringResource(R.string.feature_bmi_calculator), Icons.Default.Calculate, "bmi_calculator", Color(0xFF00897B)),
                FeatureItemData(stringResource(R.string.feature_medical_info), Icons.Default.Info, "medical_info", Color(0xFF5D4037)),
                FeatureItemData(stringResource(R.string.feature_ai_assistant), Icons.Default.SmartToy, "health_assistant", Color(0xFF512DA8)),
                FeatureItemData(stringResource(R.string.feature_recipe_ai), Icons.Default.MenuBook, "recipes", Color(0xFF6A1B9A)),
                FeatureItemData(stringResource(R.string.feature_riwayat_id), Icons.Default.History, "history", MaterialTheme.colorScheme.secondary),
                FeatureItemData(stringResource(R.string.feature_favorite_list), Icons.Default.Favorite, "favorites", Color(0xFFE91E63)),
                FeatureItemData(stringResource(R.string.feature_halocode), Icons.Default.Chat, "halocode", Color(0xFF00695C)),
                FeatureItemData(stringResource(R.string.feature_community), Icons.Default.Groups, "community", Color(0xFF1976D2)),
                FeatureItemData(stringResource(R.string.feature_local_ai), Icons.Default.Psychology, "local_ai_chat", Color(0xFFE65100)),
                FeatureItemData(stringResource(R.string.feature_lainnya_id), Icons.Default.GridView, "all_features", MaterialTheme.colorScheme.onSurfaceVariant)
            )

            features.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(item.route) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(item.color.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (rowItems.size < 4) {
                        repeat(4 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
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
