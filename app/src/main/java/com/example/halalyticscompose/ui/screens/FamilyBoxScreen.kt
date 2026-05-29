package com.example.halalyticscompose.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halalyticscompose.data.model.FamilyProfile
import com.example.halalyticscompose.ui.theme.*
import com.example.halalyticscompose.ui.viewmodel.FamilyViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyBoxScreen(
    navController: NavController,
    viewModel: FamilyViewModel = hiltViewModel()
) {
    val familyProfiles by viewModel.familyProfiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<FamilyProfile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Box", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Member")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Self Profile Item (Main User)
            MainUserProfileItem(
                isSelected = selectedProfile == null,
                onClick = { viewModel.selectProfile(null) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            if (familyProfiles.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Belum ada profil keluarga",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HalalGreen)
                        ) {
                            Text("Tambah Anggota")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(familyProfiles) { profile ->
                        FamilyMemberCard(
                            profile = profile,
                            isSelected = selectedProfile?.id == profile.id,
                            onSelect = { viewModel.selectProfile(profile) },
                            onEdit = { profileToEdit = profile },
                            onDelete = {
                                viewModel.deleteFamilyProfile(profile.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        FamilyProfileFormDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, rel, age, gender, allergies, medHistory, image ->
                viewModel.addFamilyProfile(
                    name = name,
                    relationship = rel,
                    age = age,
                    gender = gender,
                    allergies = allergies,
                    medicalHistory = medHistory,
                    image = image,
                    onSuccess = { showAddDialog = false },
                    onError = { /* Handle error if needed */ }
                )
            }
        )
    }

    if (profileToEdit != null) {
        FamilyProfileFormDialog(
            initialProfile = profileToEdit,
            onDismiss = { profileToEdit = null },
            onConfirm = { name, rel, age, gender, allergies, medHistory, image ->
                viewModel.updateFamilyProfile(
                    id = profileToEdit!!.id,
                    name = name,
                    relationship = rel,
                    age = age,
                    gender = gender,
                    allergies = allergies,
                    medicalHistory = medHistory,
                    image = image,
                    onSuccess = { profileToEdit = null },
                    onError = { /* Handle error if needed */ }
                )
            }
        )
    }
}

@Composable
fun MainUserProfileItem(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
            .background(
                if (isSelected) HalalGreen.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isSelected) HalalGreen else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Profil Saya (Utama)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Gunakan profil kesehatan Anda sendiri", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = HalalGreen)
        }
    }
}

@Composable
fun FamilyMemberCard(
    profile: FamilyProfile,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HalalGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, HalalGreen) else null,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!profile.imagePath.isNullOrEmpty()) {
                    AsyncImage(
                        model = profile.imagePath,
                        contentDescription = profile.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${profile.relationship ?: "Keluarga"} • ${profile.age ?: "?"} thn",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!profile.allergies.isNullOrEmpty()) {
                    Text(
                        text = "Alergi: ${profile.allergies}",
                        fontSize = 11.sp,
                        color = Color.Red.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyProfileFormDialog(
    initialProfile: FamilyProfile? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Int?, String?, String?, String?, File?) -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var relationship by remember { mutableStateOf(initialProfile?.relationship ?: "") }
    var age by remember { mutableStateOf(initialProfile?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(initialProfile?.gender ?: "male") }
    var allergies by remember { mutableStateOf(initialProfile?.allergies ?: "") }
    var medicalHistory by remember { mutableStateOf(initialProfile?.medicalHistory ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProfile == null) "Tambah Anggota Keluarga" else "Edit Anggota Keluarga") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!initialProfile?.imagePath.isNullOrEmpty()) {
                        AsyncImage(
                            model = initialProfile?.imagePath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Hubungan (istri, anak, dll)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                        label = { Text("Usia") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Jenis Kelamin", fontSize = 12.sp)
                        Row {
                            FilterChip(
                                selected = gender == "male",
                                onClick = { gender = "male" },
                                label = { Text("L") }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(
                                selected = gender == "female",
                                onClick = { gender = "female" },
                                label = { Text("P") }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Alergi (pisahkan koma)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("kacang, gluten, seafood...") }
                )

                OutlinedTextField(
                    value = medicalHistory,
                    onValueChange = { medicalHistory = it },
                    label = { Text("Riwayat Medis") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("diabetes, hipertensi...") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    
                    val imageFile = selectedImageUri?.let { uri ->
                        val file = File(context.cacheDir, "family_${System.currentTimeMillis()}.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        file
                    }
                    
                    onConfirm(
                        name,
                        relationship.ifBlank { null },
                        age.toIntOrNull(),
                        gender,
                        allergies.ifBlank { null },
                        medicalHistory.ifBlank { null },
                        imageFile
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = HalalGreen)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
