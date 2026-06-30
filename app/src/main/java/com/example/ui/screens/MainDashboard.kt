package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.BoqItem
import com.example.data.DrawingError
import com.example.data.FloorPlanAnalysis
import com.example.data.SavedProject
import com.example.ui.CadViewModel
import com.example.ui.ProcessingState
import com.example.ui.components.CadViewer2D
import com.example.ui.components.CadViewer3D
import com.example.ui.theme.*
import com.example.util.CadGenerator
import com.example.util.SampleData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainDashboard(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val processingState by viewModel.processingState.collectAsState()
    val layerToggles by viewModel.layerToggles.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val yaw by viewModel.yaw.collectAsState()

    var activeViewTab by remember { mutableStateOf(0) } // 0: 2D CAD, 1: 3D Model, 2: BOQ, 3: Diagnostics
    var selectedError by remember { mutableStateOf<DrawingError?>(null) }

    // File selection launcher for Module 1 (Image/PDF floor plan)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = loadBitmapFromUri(context, it)
            if (bitmap != null) {
                viewModel.processUploadedImage(bitmap)
            }
        }
    }

    // File selection launcher for Module 2 (CAD DXF/DWG upload)
    val cadPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val name = it.lastPathSegment ?: "drawing.dxf"
            viewModel.processLocalMockCAD(name)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- SOPHISTICATED APP HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI CAD SYSTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SophisticatedTextDim,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = buildAnnotatedString {
                        append("Analyzer ")
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, color = SophisticatedPrimaryLight, fontWeight = FontWeight.Normal)) {
                            append("Pro")
                        }
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = SophisticatedTextPrimary,
                    letterSpacing = (-0.5).sp
                )
            }

            // User Profile Circle badge JD
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SophisticatedPrimary.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, SophisticatedPrimary.copy(alpha = 0.4f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JD",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SophisticatedPrimaryLight
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SOPHISTICATED UPLOAD / SCANNER ZONE CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SophisticatedPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, SophisticatedPrimary.copy(alpha = 0.2f)), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = SophisticatedPrimaryLight,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Upload Blueprint File",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = SophisticatedTextPrimary
                )
                Text(
                    text = "PNG, JPG, PDF or DXF drawing files",
                    fontSize = 11.sp,
                    color = SophisticatedTextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Button Module 1: Scan image to CAD
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Button Module 2: Analyze DXF file
                    Button(
                        onClick = { cadPickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = SophisticatedSurfaceAlt),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select DXF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- PROCESSING AND AI RUNTIME OR DEFAULT VIEW SCREEN ---
        when (val state = processingState) {
            is ProcessingState.Idle -> {
                DisplayAnalysisOutputs(
                    analysis = SampleData.sample1,
                    layerToggles = layerToggles,
                    viewModel = viewModel,
                    activeTab = activeViewTab,
                    onTabSelected = { activeViewTab = it },
                    pitch = pitch,
                    yaw = yaw,
                    selectedError = selectedError,
                    onSelectError = { selectedError = it }
                )
            }

            is ProcessingState.Processing -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceAlt),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = SophisticatedPrimaryLight)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI Computer Vision Auditing Active...", color = SophisticatedTextPrimary, fontWeight = FontWeight.Medium)
                        Text("Detecting wall bounds, door indices, and column grids...", fontSize = 11.sp, color = SophisticatedTextMuted)
                    }
                }
            }

            is ProcessingState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x11EF4444)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = "Error", tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(state.message, color = SophisticatedTextSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Fallback rendering using first sample
                DisplayAnalysisOutputs(
                    analysis = SampleData.sample1,
                    layerToggles = layerToggles,
                    viewModel = viewModel,
                    activeTab = activeViewTab,
                    onTabSelected = { activeViewTab = it },
                    pitch = pitch,
                    yaw = yaw,
                    selectedError = selectedError,
                    onSelectError = { selectedError = it }
                )
            }

            is ProcessingState.Success -> {
                DisplayAnalysisOutputs(
                    analysis = state.analysis,
                    layerToggles = layerToggles,
                    viewModel = viewModel,
                    activeTab = activeViewTab,
                    onTabSelected = { activeViewTab = it },
                    pitch = pitch,
                    yaw = yaw,
                    selectedError = selectedError,
                    onSelectError = { selectedError = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // --- SAVED BLUEPRINT HISTORY (From Room Local DB) ---
        Text(
            text = "Saved Blueprints History",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = SophisticatedTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (savedProjects.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceAlt),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = SophisticatedTextDim, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No local history found.", color = SophisticatedTextMuted, fontSize = 12.sp)
                    Text("Successfully processed floor plans will appear here.", color = SophisticatedTextDim, fontSize = 10.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                savedProjects.forEach { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.loadSavedProject(project) },
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = SophisticatedPrimaryLight)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(project.name, color = SophisticatedTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(project.timestamp)
                                Text("Audited on $dateStr", color = SophisticatedTextMuted, fontSize = 10.sp)
                            }
                            IconButton(onClick = { viewModel.deleteProject(project) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xBBEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayAnalysisOutputs(
    analysis: FloorPlanAnalysis,
    layerToggles: Map<String, Boolean>,
    viewModel: CadViewModel,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    pitch: Float,
    yaw: Float,
    selectedError: DrawingError?,
    onSelectError: (DrawingError) -> Unit
) {
    val context = LocalContext.current

    // Sample Selector Playgrounds
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceAlt),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Sample Blueprint Playgrounds", fontSize = 11.sp, color = SophisticatedTextMuted, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val p1 = "Modern 1BHK Apartment"
                val p2 = "Luxury 2BHK Duplex Villa"
                val p3 = "Compact Studio Office"

                Button(
                    onClick = { viewModel.selectSamplePlan(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (analysis.stats.projectName == p1) SophisticatedPrimary else SophisticatedSurface),
                    border = BorderStroke(1.dp, if (analysis.stats.projectName == p1) SophisticatedPrimaryLight else Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("1BHK Apt", fontSize = 10.sp, color = SophisticatedTextPrimary)
                }
                Button(
                    onClick = { viewModel.selectSamplePlan(2) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (analysis.stats.projectName == p2) SophisticatedPrimary else SophisticatedSurface),
                    border = BorderStroke(1.dp, if (analysis.stats.projectName == p2) SophisticatedPrimaryLight else Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Duplex Villa", fontSize = 10.sp, color = SophisticatedTextPrimary)
                }
                Button(
                    onClick = { viewModel.selectSamplePlan(3) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (analysis.stats.projectName == p3) SophisticatedPrimary else SophisticatedSurface),
                    border = BorderStroke(1.dp, if (analysis.stats.projectName == p3) SophisticatedPrimaryLight else Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Studio Office", fontSize = 10.sp, color = SophisticatedTextPrimary)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // --- CAD GENERATION AND VIEW SELECTION TABS ---
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val tabTitles = listOf("2D CAD View", "3D Preview", "BOQ Costing", "AI Advisor")
        tabTitles.forEachIndexed { idx, title ->
            Button(
                onClick = { onTabSelected(idx) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == idx) SophisticatedSurface else SophisticatedBackground
                ),
                border = BorderStroke(1.dp, if (activeTab == idx) SophisticatedPrimaryLight.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Text(
                    title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (activeTab == idx) SophisticatedPrimaryLight else SophisticatedTextMuted
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tab Contents
    when (activeTab) {
        0 -> { // 2D CAD blueprint view
            Column {
                CadViewer2D(
                    elements = analysis.elements,
                    layerToggles = layerToggles,
                    errors = analysis.errors,
                    selectedError = selectedError,
                    onSelectError = onSelectError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Layer Checklists
                Text("Layer Visibility Filters", fontSize = 12.sp, color = SophisticatedTextMuted, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Walls", "Doors", "Windows", "Columns", "Text", "Dimensions", "Errors").forEach { layer ->
                        val isChecked = layerToggles[layer] ?: true
                        FilterChip(
                            selected = isChecked,
                            onClick = { viewModel.toggleLayer(layer) },
                            label = { Text(layer, fontSize = 11.sp, color = SophisticatedTextPrimary) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SophisticatedPrimary,
                                containerColor = SophisticatedSurfaceAlt
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selected = isChecked,
                                enabled = true,
                                borderColor = Color.White.copy(alpha = 0.05f),
                                selectedBorderColor = SophisticatedPrimaryLight
                            )
                        )
                    }
                }
            }
        }

        1 -> { // 3D Isometric View
            Column {
                CadViewer3D(
                    elements = analysis.elements,
                    pitch = pitch,
                    yaw = yaw,
                    onRotate = { p, y -> viewModel.update3DRotation(p, y) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Interactive 3D View: Drag finger to rotate pitch (tilt) and yaw (spin).",
                    fontSize = 11.sp,
                    color = SophisticatedTextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        2 -> { // BOQ Costing
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bill of Quantities (BOQ) Estimation", fontWeight = FontWeight.Bold, color = SophisticatedTextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val grandTotal = analysis.boq.sumOf { it.totalPrice.toDouble() }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimated Material Cost", color = SophisticatedTextMuted, fontSize = 12.sp)
                        Text("$${String.format("%,.2f", grandTotal)}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    
                    Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        analysis.boq.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.materialName, color = SophisticatedTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("Category: ${item.category} | Qty: ${item.quantity} ${item.unit}", color = SophisticatedTextMuted, fontSize = 10.sp)
                                }
                                Text("$${String.format("%,.0f", item.totalPrice)}", color = SophisticatedTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        3 -> { // AI Advisor (Diagnostics and suggestions)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Drawing Diagnostics Check
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Automatic Drawing Diagnostics", fontWeight = FontWeight.Bold, color = SophisticatedTextPrimary, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (analysis.errors.isEmpty()) {
                            Text("No design errors or overlapping walls found. Drawing verified!", color = Color(0xFF10B981), fontSize = 11.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                analysis.errors.forEach { err ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onTabSelected(0) // Focus back to 2D view to see error
                                                onSelectError(err)
                                            }
                                            .background(
                                                if (err.severity == "high") Color(0x18EF4444) else Color(0x18FBBF24),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (err.severity == "high") Color(0xFFEF4444) else Color(0xFFFBBF24),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(err.type.replace("_", " ").uppercase(), color = SophisticatedTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(err.description, color = SophisticatedTextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Suggestions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = SophisticatedAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Structural & Design Advice", fontWeight = FontWeight.Bold, color = SophisticatedTextPrimary, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            analysis.suggestions.forEach { sug ->
                                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                    Text("${sug.category}: ${sug.recommendation}", color = SophisticatedTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(sug.justification, color = SophisticatedTextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // --- DOWNLOAD / SAVE BUTTON PANEL ---
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceAlt),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Download & Export Formats", fontWeight = FontWeight.Bold, color = SophisticatedTextPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Download DXF Button
                Button(
                    onClick = {
                        val dxfText = CadGenerator.generateDxf(analysis.elements)
                        shareFile(context, "${analysis.stats.projectName.replace(" ", "_")}.dxf", dxfText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = SophisticatedPrimaryLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DXF", fontSize = 10.sp, color = SophisticatedTextPrimary)
                }

                // Download SVG Button
                Button(
                    onClick = {
                        val svgText = CadGenerator.generateSvg(analysis.elements)
                        shareFile(context, "${analysis.stats.projectName.replace(" ", "_")}.svg", svgText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = SophisticatedPrimaryLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SVG", fontSize = 10.sp, color = SophisticatedTextPrimary)
                }

                // Download JSON Button
                Button(
                    onClick = {
                        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                        val adapter = moshi.adapter(FloorPlanAnalysis::class.java).indent("  ")
                        val jsonText = adapter.toJson(analysis)
                        shareFile(context, "${analysis.stats.projectName.replace(" ", "_")}_report.json", jsonText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = SophisticatedPrimaryLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JSON", fontSize = 10.sp, color = SophisticatedTextPrimary)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // --- DETAILED DRAWING AUDIT STATISTICS PANEL ---
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${analysis.stats.projectName} Metrics",
                    fontWeight = FontWeight.Bold,
                    color = SophisticatedTextPrimary,
                    fontSize = 15.sp
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(99.dp))
                        .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)), RoundedCornerShape(99.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Complete",
                        color = Color(0xFF10B981),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Stats Grid 2x4
            val statsList = listOf(
                Pair("Total Built-up", "${analysis.stats.builtUpAreaSqMeters} m²"),
                Pair("Carpet Area", "${analysis.stats.carpetAreaSqMeters} m²"),
                Pair("Wall Length", "${analysis.stats.wallLengthMeters} m"),
                Pair("Columns", "${analysis.stats.columnCount} pillars"),
                Pair("Floor Count", "${analysis.stats.floorCount} floor"),
                Pair("Flat Count", "${analysis.stats.flatCount} flat"),
                Pair("Room Count", "${analysis.stats.roomCount} spaces"),
                Pair("Est. Flat Size", "${analysis.stats.estimatedFlatSizeSqMeters} m²")
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                statsList.forEach { stat ->
                    Column(
                        modifier = Modifier
                            .width(140.dp)
                            .background(SophisticatedSurfaceAlt, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.02f)), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(stat.first, color = SophisticatedTextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(stat.second, color = SophisticatedTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

// Helper to share text content as files
fun shareFile(context: Context, fileName: String, fileContent: String) {
    try {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)
        file.writeText(fileContent)
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export/Share CAD File"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Helper to load bitmap securely from Uri
fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
