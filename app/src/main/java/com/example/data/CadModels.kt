package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Point2D(
    val x: Float,
    val y: Float
)

@JsonClass(generateAdapter = true)
data class CadElement(
    val type: String, // "wall", "door", "window", "column", "stair", "balcony", "toilet", "kitchen"
    val start: Point2D? = null,
    val end: Point2D? = null,
    val bounds: List<Point2D>? = null, // For polygons like columns or rooms
    val label: String? = null, // For rooms or annotations
    val width: Float? = null,
    val height: Float? = null,
    val thickness: Float? = null
)

@JsonClass(generateAdapter = true)
data class ProjectStats(
    val projectName: String,
    val floorCount: Int,
    val flatCount: Int,
    val roomCount: Int,
    val bedroomCount: Int,
    val bathroomCount: Int,
    val livingCount: Int,
    val diningCount: Int,
    val kitchenCount: Int,
    val balconyCount: Int,
    val stairCount: Int,
    val liftCount: Int,
    val corridorCount: Int,
    val wallLengthMeters: Float,
    val wallAreaSqMeters: Float,
    val builtUpAreaSqMeters: Float,
    val carpetAreaSqMeters: Float,
    val superBuiltUpAreaSqMeters: Float,
    val columnCount: Int,
    val estimatedFlatSizeSqMeters: Float
)

@JsonClass(generateAdapter = true)
data class RoomDetail(
    val name: String,
    val sizeString: String, // e.g., "3.6m x 3.0m"
    val areaSqMeters: Float
)

@JsonClass(generateAdapter = true)
data class OpeningDetail(
    val type: String, // "Door" or "Window"
    val sizeString: String, // e.g., "1.0m x 2.1m"
    val count: Int
)

@JsonClass(generateAdapter = true)
data class DrawingError(
    val type: String, // "missing_door", "overlapping_walls", "room_mismatch", "unsupported_span"
    val description: String,
    val severity: String, // "high", "medium", "low"
    val x: Float, // Normalized x-coord for highlighting in UI (0-100)
    val y: Float  // Normalized y-coord for highlighting in UI (0-100)
)

@JsonClass(generateAdapter = true)
data class BoqItem(
    val materialName: String,
    val category: String, // "Structure", "Masonry", "Finishing", "Fixtures"
    val quantity: Float,
    val unit: String, // "m3", "m2", "kg", "units"
    val unitPrice: Float,
    val totalPrice: Float
)

@JsonClass(generateAdapter = true)
data class AiSuggestion(
    val category: String, // "Efficiency", "Safety", "Lighting", "Cost"
    val recommendation: String,
    val justification: String
)

@JsonClass(generateAdapter = true)
data class FloorPlanAnalysis(
    val stats: ProjectStats,
    val rooms: List<RoomDetail>,
    val openings: List<OpeningDetail>,
    val elements: List<CadElement>,
    val errors: List<DrawingError>,
    val boq: List<BoqItem>,
    val suggestions: List<AiSuggestion>
)

@Entity(tableName = "projects")
data class SavedProject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val jsonAnalysis: String, // Serialized FloorPlanAnalysis
    val dxfContent: String,
    val svgContent: String
)
