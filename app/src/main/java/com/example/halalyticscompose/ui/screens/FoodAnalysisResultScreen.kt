package com.example.halalyticscompose.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.MainViewModel // Assuming ViewModel handles API call
import java.io.File
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.halalyticscompose.ui.components.HalalBadge
import com.example.halalyticscompose.data.model.MealData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodAnalysisResultScreen(
    navController: NavController,
    imagePath: String,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val imageFile = File(imagePath)
    val bitmap = remember(imagePath) {
        if (imageFile.exists()) BitmapFactory.decodeFile(imageFile.absolutePath) else null
    }

    // Trigger analysis on first load
    LaunchedEffect(Unit) {
        if (imageFile.exists()) {
             viewModel.analyzeMealImage(imageFile) 
        }
    }

    val analysisState by viewModel.mealAnalysisState.collectAsState() 

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Food Analysis", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
             modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Captured Image
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured Food",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                     Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(MaterialTheme.colorScheme.onSurface.copy(0.2f)))
                }

                // 2. Loading State
                if (analysisState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                // 3. Error State
                if (analysisState.error != null) {
                     Card(
                         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                         modifier = Modifier.padding(16.dp).fillMaxWidth()
                     ) {
                         Column(modifier = Modifier.padding(16.dp)) {
                             Text("Analysis Failed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                             Text(analysisState.error ?: "Unknown error", color = MaterialTheme.colorScheme.onErrorContainer)
                             Button(
                                 onClick = { if (imageFile.exists()) viewModel.analyzeMealImage(imageFile) },
                                 modifier = Modifier.padding(top=8.dp),
                                 colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                             ) { Text("Retry", color = MaterialTheme.colorScheme.onError) }
                         }
                     }
                }

                // 4. Success State
                analysisState.data?.let { data ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        
                        // Dish Name & Description
                        Text(
                            text = data.mealName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Halal Status Card
                        val statusColor = when(data.halalAnalysis.status.lowercase()) {
                            "halal" -> MaterialTheme.colorScheme.primary
                            "haram" -> MaterialTheme.colorScheme.error
                            else -> MushboohYellow
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha=0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if(data.halalAnalysis.status == "halal") Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = statusColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = data.halalAnalysis.status.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(data.halalAnalysis.reason, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                
                                if (data.halalAnalysis.riskFactors.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Detected Risks:", fontWeight = FontWeight.Bold, color = statusColor, fontSize = 12.sp)
                                    data.halalAnalysis.riskFactors.forEach { risk ->
                                    Text("• $risk", color = MaterialTheme.colorScheme.onSurface.copy(0.8f), fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Health Score
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { data.healthScore / 100f },
                                color = if(data.healthScore > 70) MaterialTheme.colorScheme.primary else if (data.healthScore > 40) MushboohYellow else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(60.dp),
                                strokeWidth = 6.dp,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Health Score", color = MaterialTheme.colorScheme.onBackground.copy(0.6f), fontSize = 12.sp)
                                Text("${data.healthScore}/100", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("Grade: ${data.healthGrade}", color = MaterialTheme.colorScheme.onBackground.copy(0.4f), fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nutrition Grid
                        Text("Nutrition (per serving)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NutritionItem("Calories", "${data.nutrition.calories}", "kcal", Modifier.weight(1f))
                            NutritionItem("Protein", "%.1f".format(data.nutrition.protein), "g", Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NutritionItem("Carbs", "%.1f".format(data.nutrition.carbs), "g", Modifier.weight(1f))
                            NutritionItem("Fat", "%.1f".format(data.nutrition.fat), "g", Modifier.weight(1f))
                            NutritionItem("Sugar", "%.1f".format(data.nutrition.sugar), "g", Modifier.weight(1f))
                        }
                        
                         Spacer(modifier = Modifier.height(16.dp))
                         
                         // Portion Advice
                         Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                             Column(modifier = Modifier.padding(16.dp)) {
                                 Row(verticalAlignment = Alignment.CenterVertically) {
                                     Icon(Icons.Default.Restaurant, contentDescription=null, tint=MaterialTheme.colorScheme.primary)
                                     Spacer(modifier = Modifier.width(8.dp))
                                     Text("Portion Advice", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                 }
                                 Spacer(modifier = Modifier.height(4.dp))
                                 Text(data.portionAdvice, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 14.sp)
                             }
                         }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionItem(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(unit, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
        }
    }
}
