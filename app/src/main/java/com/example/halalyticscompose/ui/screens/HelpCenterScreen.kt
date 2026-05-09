package com.example.halalyticscompose.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.ui.viewmodel.HelpCenterViewModel
import androidx.navigation.NavController

// ═══════════════════════════════════════════════════════════════════
// COLOR CONSTANTS — Emerald Forest Premium
// ═══════════════════════════════════════════════════════════════════
private val EmeraldDark = Color(0xFF004D40)
private val EmeraldMedium = Color(0xFF00695C)
private val EmeraldLight = Color(0xFF26A69A)
private val SageBg = Color(0xFFF4F9F8)
private val SoftSage = Color(0xFFE0F2F1)
private val CardBg = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF212121)
private val TextMedium = Color(0xFF757575)
private val TextLight = Color(0xFF9E9E9E)

data class FaqCategory(
    val name: String,
    val emoji: String,
    val color: Color
)

data class FaqItem(
    val question: String,
    val answer: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    navController: NavController,
    viewModel: HelpCenterViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var expandedFaq by remember { mutableStateOf<String?>(null) }
    
    val dynamicCategories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    val categories = listOf(
        FaqCategory("Panduan Pengguna", "📖", Color(0xFF1565C0)),
        FaqCategory("Fitur Kesehatan", "❤️", Color(0xFFD32F2F)),
        FaqCategory("Akun & Keamanan", "🔐", Color(0xFF6A1B9A)),
        FaqCategory("Teknis", "🔧", Color(0xFFF57C00)),
        FaqCategory("Pembayaran", "💰", Color(0xFF2E7D32)),
        FaqCategory("Lainnya", "📋", TextLight),
    )

    val faqs = listOf(
        FaqItem(
            "Bagaimana cara scan barcode produk?",
            "Buka aplikasi → tap tombol Scan di menu bawah → arahkan kamera ke barcode produk → hasil akan muncul otomatis.",
            "Panduan Pengguna"
        ),
        FaqItem(
            "Bagaimana cara mengecek status halal?",
            "Anda bisa scan barcode, ketik nama produk di pencarian, atau gunakan fitur Verifikasi Sertifikat Halal.",
            "Panduan Pengguna"
        ),
        FaqItem(
            "Apakah data scan saya tersimpan?",
            "Ya, semua riwayat scan tersimpan di menu Riwayat. Anda bisa melihat produk yang pernah di-scan kapan saja.",
            "Panduan Pengguna"
        ),
        FaqItem(
            "Bagaimana cara menggunakan Pengingat Obat?",
            "Masuk ke Health Suite → Pengingat Obat → Buat Pengingat Baru → Cari obat → Isi jadwal → Simpan.",
            "Fitur Kesehatan"
        ),
        FaqItem(
            "Apakah hasil kuis kesehatan mental akurat?",
            "Kuis GAD-7 dan PHQ-9 adalah alat skrining standar internasional. Hasilnya bersifat panduan awal, bukan diagnosis resmi.",
            "Fitur Kesehatan"
        ),
        FaqItem(
            "Bagaimana AI Health Assistant bekerja?",
            "AI menggunakan teknologi Gemini untuk informasi kesehatan. AI mempertimbangkan profil medis Anda untuk jawaban yang lebih personal.",
            "Fitur Kesehatan"
        ),
        FaqItem(
            "Bagaimana cara mengubah kata sandi?",
            "Masuk ke Pengaturan → Pengaturan Akun → Ubah Kata Sandi → Masukkan kata sandi lama dan baru → Simpan.",
            "Akun & Keamanan"
        ),
        FaqItem(
            "Apakah data medis saya aman?",
            "Ya, semua data medis dienkripsi dan hanya bisa diakses oleh Anda. Kami tidak membagikan data Anda ke pihak ketiga.",
            "Akun & Keamanan"
        ),
        FaqItem(
            "Kenapa kamera scan tidak berfungsi?",
            "Pastikan izin kamera sudah diberikan. Buka Pengaturan HP → Aplikasi → Halalytics → Izin → aktifkan Kamera.",
            "Teknis"
        ),
        FaqItem(
            "Aplikasi terasa lambat?",
            "Coba clear cache aplikasi, pastikan koneksi internet stabil, dan update ke versi terbaru.",
            "Teknis"
        ),
    )

    val filteredFaqs = faqs.filter { faq ->
        val matchesSearch = searchQuery.isBlank() ||
                faq.question.contains(searchQuery, ignoreCase = true) ||
                faq.answer.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || faq.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(containerColor = SageBg) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── EMERALD GRADIENT HEADER ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(EmeraldDark, EmeraldMedium, EmeraldLight)
                            )
                        )
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 28.dp)
                ) {
                    Column {
                        // Back + Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable { navController.popBackStack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack, null,
                                    tint = Color.White, modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Pusat Bantuan",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            "Apa yang bisa kami bantu? 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search, null,
                                tint = TextLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Cari topik atau pertanyaan...",
                                        fontSize = 13.sp,
                                        color = TextLight
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close, "Clear",
                                        tint = TextLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── CATEGORY GRID ──
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedCategory = if (selectedCategory == cat.name) null else cat.name
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedCategory == cat.name) cat.color.copy(alpha = 0.08f) else CardBg
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                border = if (selectedCategory == cat.name) BorderStroke(1.5.dp, cat.color.copy(alpha = 0.4f)) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(cat.emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        cat.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (selectedCategory == cat.name) cat.color else TextDark
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.drop(3).forEach { cat ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedCategory = if (selectedCategory == cat.name) null else cat.name
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedCategory == cat.name) cat.color.copy(alpha = 0.08f) else CardBg
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                border = if (selectedCategory == cat.name) BorderStroke(1.5.dp, cat.color.copy(alpha = 0.4f)) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(cat.emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        cat.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (selectedCategory == cat.name) cat.color else TextDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── FAQ HEADER ──
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftSage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QuestionAnswer, null,
                            tint = EmeraldDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedCategory != null) "FAQ: $selectedCategory" else "Pertanyaan Populer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark
                    )
                }
            }

            // ── FAQ ACCORDION LIST ──
            items(filteredFaqs) { faq ->
                val isExpanded = expandedFaq == faq.question
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable {
                            expandedFaq = if (isExpanded) null else faq.question
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) EmeraldDark.copy(alpha = 0.04f) else CardBg
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isExpanded) 0.dp else 1.dp
                    ),
                    border = if (isExpanded) BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.3f)) else null
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                faq.question,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                lineHeight = 19.sp,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = if (isExpanded) EmeraldDark else TextLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    faq.answer,
                                    fontSize = 13.sp,
                                    color = TextMedium,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── CONTACT SUPPORT ──
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SoftSage),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SupportAgent, null,
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Hubungi Customer Support",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Text(
                                    "Kami siap 24 jam untuk membantu",
                                    fontSize = 11.sp,
                                    color = TextMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Chat
                            Button(
                                onClick = { /* Open chat */ },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                                    Text(
                                        "Chat",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            // Email
                            Button(
                                onClick = { /* Open email */ },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Email, null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Email",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            // Phone
                            Button(
                                onClick = { /* Open phone */ },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Phone, null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Telepon",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── LEGAL LINKS ──
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { }) {
                        Text(
                            "Ketentuan Penggunaan",
                            fontSize = 12.sp,
                            color = EmeraldDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        "•",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                        color = TextLight
                    )
                    TextButton(onClick = { }) {
                        Text(
                            "Kebijakan Privasi",
                            fontSize = 12.sp,
                            color = EmeraldDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
