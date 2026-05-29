package com.example.halalyticscompose.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import com.example.halalyticscompose.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.ui.viewmodel.AuthViewModel
import com.example.halalyticscompose.data.model.BmiAdviceData

private val BmiBlue = Color(0xFF3B82F6)
private val BmiGreen = Color(0xFF10B981)
private val BmiYellow = Color(0xFFF59E0B)
private val BmiRed = Color(0xFFEF4444)
private val MintAccent = Color(0xFF00BFA6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userData by authViewModel.userData.collectAsState()
    val isLoadingAi by authViewModel.isLoading.collectAsState()

    var weightInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var bmiResult by remember { mutableStateOf<Float?>(null) }
    var bmiCategory by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(true) }
    var aiAdvice by remember { mutableStateOf<BmiAdviceData?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val catUnderweightSevere = stringResource(R.string.bmi_cat_underweight_severe)
    val catUnderweightMild = stringResource(R.string.bmi_cat_underweight_mild)
    val catNormalThin = stringResource(R.string.bmi_cat_normal_thin)
    val catNormalIdeal = stringResource(R.string.bmi_cat_normal_ideal)
    val catOverweight = stringResource(R.string.bmi_cat_overweight)
    val catObese1 = stringResource(R.string.bmi_cat_obese_1)
    val catObese2 = stringResource(R.string.bmi_cat_obese_2)

    val descUnderweightSevere = stringResource(R.string.bmi_desc_underweight_severe)
    val descUnderweightMild = stringResource(R.string.bmi_desc_underweight_mild)
    val descNormalThin = stringResource(R.string.bmi_desc_normal_thin)
    val descNormalIdeal = stringResource(R.string.bmi_desc_normal_ideal)
    val descOverweight = stringResource(R.string.bmi_desc_overweight)
    val descObese1 = stringResource(R.string.bmi_desc_obese_1)
    val descObese2 = stringResource(R.string.bmi_desc_obese_2)

    // Initialize with user data if available
    LaunchedEffect(userData) {
        userData?.let { user ->
            if (weightInput.isEmpty() && user.weight != null && user.weight > 0) {
                weightInput = user.weight.toString()
            }
            if (heightInput.isEmpty() && user.height != null && user.height > 0) {
                heightInput = user.height.toString()
            }
        }
    }

    fun calculateBmi() {
        val weight = weightInput.toFloatOrNull() ?: return
        val height = heightInput.toFloatOrNull() ?: return
        if (height <= 0 || weight <= 0) return
        val heightM = height / 100f
        val bmi = weight / (heightM * heightM)
        bmiResult = bmi
        bmiCategory = getBMICategoryCode(bmi)
        showResult = true
        showInfo = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bmi_calculator_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Info Section
            AnimatedVisibility(visible = showInfo) {
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(stringResource(R.string.bmi_classification_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.bmi_classification_desc),
                                fontSize = 13.sp, color = Color.DarkGray, lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(stringResource(R.string.bmi_asia_standards), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            listOf(
                                Triple("< 18.5", stringResource(R.string.bmi_underweight), BmiBlue),
                                Triple("18.5 – 22.9", stringResource(R.string.bmi_normal), BmiGreen),
                                Triple("23.0 – 24.9", stringResource(R.string.bmi_overweight), BmiYellow),
                                Triple("> 25.0", stringResource(R.string.bmi_obese), BmiRed)
                            ).forEach { (range, label, color) ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("$range → $label", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.bmi_calculate_yours), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(stringResource(R.string.bmi_weight_label)) },
                        placeholder = { Text("62") },
                        leadingIcon = { Text("⚖️", fontSize = 20.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(stringResource(R.string.bmi_height_label)) },
                        placeholder = { Text("178") },
                        leadingIcon = { Text("📏", fontSize = 20.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { calculateBmi() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintAccent),
                        enabled = weightInput.isNotBlank() && heightInput.isNotBlank()
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("HITUNG", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Result Section
            AnimatedVisibility(
                visible = showResult && bmiResult != null,
                enter = slideInVertically() + fadeIn()
            ) {
                val bmi = bmiResult ?: 0f
                val resultColor = when (bmiCategory) {
                    "underweight_severe", "underweight_mild" -> BmiBlue
                    "normal_thin", "normal_ideal" -> BmiGreen
                    "overweight" -> BmiYellow
                    "obese_1", "obese_2" -> BmiRed
                    else -> Color.Gray
                }
                val categoryLabel = when (bmiCategory) {
                    "underweight_severe" -> catUnderweightSevere
                    "underweight_mild" -> catUnderweightMild
                    "normal_thin" -> catNormalThin
                    "normal_ideal" -> catNormalIdeal
                    "overweight" -> catOverweight
                    "obese_1" -> catObese1
                    "obese_2" -> catObese2
                    else -> ""
                }
                val description = when (bmiCategory) {
                    "underweight_severe" -> descUnderweightSevere
                    "underweight_mild" -> descUnderweightMild
                    "normal_thin" -> descNormalThin
                    "normal_ideal" -> descNormalIdeal
                    "overweight" -> descOverweight
                    "obese_1" -> descObese1
                    "obese_2" -> descObese2
                    else -> ""
                }

                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = resultColor.copy(alpha = 0.08f)),
                        border = BorderStroke(2.dp, resultColor.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular BMI Indicator
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(160.dp).padding(10.dp)
                            ) {
                                val animatedBmi = animateFloatAsState(
                                    targetValue = (bmi / 40f).coerceIn(0f, 1.2f),
                                    animationSpec = tween(1500)
                                )
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 14.dp.toPx()
                                    // Background circle
                                    drawArc(
                                        color = resultColor.copy(alpha = 0.1f),
                                        startAngle = 135f,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                    // Foreground arc
                                    drawArc(
                                        color = resultColor,
                                        startAngle = 135f,
                                        sweepAngle = 270f * (animatedBmi.value / 1.2f).coerceIn(0f, 1f),
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        String.format("%.1f", bmi),
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black,
                                        color = resultColor
                                    )
                                    Text(stringResource(R.string.bmi_score_label), fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = resultColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    categoryLabel,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = resultColor,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                description,
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            // BMI Scale Bar
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Pointer Indicator
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                val barWidth = maxWidth
                                val indicatorPos = ((bmi / 40f).coerceIn(0f, 1f))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = resultColor,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .offset(x = barWidth * indicatorPos - 12.dp, y = (-10).dp)
                                        .size(24.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp))
                            ) {
                                Box(modifier = Modifier.weight(18.5f).fillMaxHeight().background(BmiBlue))
                                Box(modifier = Modifier.weight(4.5f).fillMaxHeight().background(BmiGreen))
                                Box(modifier = Modifier.weight(2f).fillMaxHeight().background(BmiYellow))
                                Box(modifier = Modifier.weight(15f).fillMaxHeight().background(BmiRed))
                                Box(modifier = Modifier.weight(10f).fillMaxHeight().background(BmiRed.copy(0.7f)))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0", fontSize = 10.sp, color = Color.Gray)
                                Text("18.5", fontSize = 10.sp, color = Color.Gray)
                                Text("23", fontSize = 10.sp, color = Color.Gray)
                                Text("25", fontSize = 10.sp, color = Color.Gray)
                                Text("40+", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    // --- AI ADVICE SECTION ---
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (aiAdvice == null) {
                        Button(
                            onClick = {
                                val w = weightInput.toFloatOrNull() ?: bmiResult ?: 0f
                                val h = heightInput.toFloatOrNull() ?: 0f
                                if (w > 0 && h > 0) {
                                    authViewModel.getBmiAdvice(w, h) { result ->
                                        if (result != null) {
                                            aiAdvice = result
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(context.getString(R.string.bmi_ai_error))
                                                // Local fallback logic
                                                aiAdvice = BmiAdviceData(
                                                    statusFisik = "Berdasarkan BMI $bmi, Anda masuk kategori $categoryLabel.",
                                                    target2Bulan = if (bmi < 18.5) "Menaikkan 2-3 kg" else if (bmi > 25) "Menurunkan 3-5 kg" else "Mempertahankan berat badan",
                                                    saranNutrisi = listOf("Konsumsi protein tinggi", "Cukupi serat dari sayuran", "Minum air 2L/hari"),
                                                    saranOlahraga = listOf("Jalan santai 30 menit", "Latihan beban ringan", "Istirahat cukup"),
                                                    pesanMotivasi = "Kesehatan adalah investasi terbaik. Mulailah dari langkah kecil hari ini!"
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = resultColor),
                            enabled = !isLoadingAi
                        ) {
                            if (isLoadingAi) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.bmi_ai_advice_button), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AnimatedVisibility(visible = aiAdvice != null) {
                        aiAdvice?.let { advice ->
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = resultColor)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(stringResource(R.string.bmi_ai_analysis_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(advice.statusFisik, fontSize = 14.sp, color = Color.Gray)
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                        Text("${stringResource(R.string.bmi_target_label).substringBefore(":")}: ${advice.target2Bulan}", fontWeight = FontWeight.Bold, color = resultColor)
                                    }
                                }

                                // Nutrition & Exercise Cards
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AdviceCard(
                                        title = stringResource(R.string.bmi_nutrition_label),
                                        items = advice.saranNutrisi,
                                        icon = Icons.Default.Restaurant,
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdviceCard(
                                        title = stringResource(R.string.bmi_exercise_label),
                                        items = advice.saranOlahraga,
                                        icon = Icons.Default.FitnessCenter,
                                        color = Color(0xFF3B82F6),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Motivation Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = resultColor.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = resultColor)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(advice.pesanMotivasi, fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                    // --- END AI ADVICE ---

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            showResult = false
                            showInfo = true
                            bmiResult = null
                            weightInput = ""
                            heightInput = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.bmi_recalculate_button), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AdviceCard(title: String, items: List<String>, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.take(3).forEach { item ->
                Row(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text("•", color = color, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item, fontSize = 11.sp, lineHeight = 16.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

private fun getBMICategoryCode(bmi: Float): String {
    return when {
        bmi < 16.0f -> "underweight_severe"
        bmi < 18.5f -> "underweight_mild"
        bmi < 23.0f -> "normal_thin"
        bmi < 25.0f -> "normal_ideal"
        bmi < 30.0f -> "overweight"
        bmi < 35.0f -> "obese_1"
        else -> "obese_2"
    }
}
