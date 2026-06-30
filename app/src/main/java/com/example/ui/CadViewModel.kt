package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.GeminiFloorPlanService
import com.example.data.*
import com.example.util.CadGenerator
import com.example.util.SampleData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ProcessingState {
    object Idle : ProcessingState()
    object Processing : ProcessingState()
    data class Success(val analysis: FloorPlanAnalysis) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}

class CadViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ProjectDatabase.getDatabase(application)
    private val repository = ProjectRepository(database.projectDao())
    private val geminiService = GeminiFloorPlanService()
    
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val analysisAdapter = moshi.adapter(FloorPlanAnalysis::class.java)

    // Saved projects from Room Database
    val savedProjects: StateFlow<List<SavedProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Processing State
    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Success(SampleData.sample1))
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    // Layer Toggles for 2D View
    private val _layerToggles = MutableStateFlow(
        mapOf(
            "Walls" to true,
            "Doors" to true,
            "Windows" to true,
            "Columns" to true,
            "Text" to true,
            "Dimensions" to true,
            "Errors" to true
        )
    )
    val layerToggles: StateFlow<Map<String, Boolean>> = _layerToggles.asStateFlow()

    // Interactive 3D Rotation Angles
    private val _pitch = MutableStateFlow(35f) // Degrees around X axis (tilt)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _yaw = MutableStateFlow(45f) // Degrees around Z axis (spin)
    val yaw: StateFlow<Float> = _yaw.asStateFlow()

    // Active Tab state
    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Image to CAD, 2: CAD Analyzer, 3: Settings
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // API Key status check
    val isApiKeyConfigured: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleLayer(layer: String) {
        val current = _layerToggles.value.toMutableMap()
        current[layer] = !(current[layer] ?: true)
        _layerToggles.value = current
    }

    fun update3DRotation(deltaPitch: Float, deltaYaw: Float) {
        _pitch.value = (_pitch.value + deltaPitch).coerceIn(10f, 85f)
        _yaw.value = (_yaw.value + deltaYaw) % 360f
    }

    fun selectSamplePlan(index: Int) {
        val selected = when (index) {
            1 -> SampleData.sample1
            2 -> SampleData.sample2
            3 -> SampleData.sample3
            else -> SampleData.sample1
        }
        _processingState.value = ProcessingState.Success(selected)
    }

    fun loadSavedProject(project: SavedProject) {
        try {
            val analysis = analysisAdapter.fromJson(project.jsonAnalysis)
            if (analysis != null) {
                _processingState.value = ProcessingState.Success(analysis)
            }
        } catch (e: Exception) {
            _processingState.value = ProcessingState.Error("Failed to parse saved project database entry.")
        }
    }

    fun deleteProject(project: SavedProject) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun processUploadedImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _processingState.value = ProcessingState.Processing
            
            // Trigger Gemini API Analysis
            val result = geminiService.analyzeFloorPlanImage(bitmap)
            result.onSuccess { analysis ->
                _processingState.value = ProcessingState.Success(analysis)
                
                // Save analyzed project to Room Database for persistence
                saveProjectToHistory(analysis)
            }.onFailure { error ->
                if (error.message == "API_KEY_MISSING") {
                    _processingState.value = ProcessingState.Error(
                        "Gemini API Key is missing. Please configure it in the AI Studio Secrets panel.\n\nFallback: Displaying default Modern 1BHK blueprint."
                    )
                } else {
                    _processingState.value = ProcessingState.Error(
                        "Analysis failed: ${error.localizedMessage ?: "Unknown server response"}.\n\nFallback: Displaying default Modern 1BHK blueprint."
                    )
                }
            }
        }
    }

    fun processLocalMockCAD(fileName: String) {
        viewModelScope.launch {
            _processingState.value = ProcessingState.Processing
            kotlinx.coroutines.delay(1800) // Simulated computer vision analysis delay
            
            // For CAD analysis testing, choose an interesting design
            val mockPlan = when {
                fileName.contains("villa", ignoreCase = true) -> SampleData.sample2
                fileName.contains("office", ignoreCase = true) -> SampleData.sample3
                else -> SampleData.sample1
            }
            _processingState.value = ProcessingState.Success(mockPlan)
            saveProjectToHistory(mockPlan)
        }
    }

    private fun saveProjectToHistory(analysis: FloorPlanAnalysis) {
        viewModelScope.launch {
            val dxf = CadGenerator.generateDxf(analysis.elements)
            val svg = CadGenerator.generateSvg(analysis.elements)
            val jsonString = analysisAdapter.toJson(analysis)
            
            val project = SavedProject(
                name = analysis.stats.projectName,
                jsonAnalysis = jsonString,
                dxfContent = dxf,
                svgContent = svg
            )
            repository.insertProject(project)
        }
    }
}
