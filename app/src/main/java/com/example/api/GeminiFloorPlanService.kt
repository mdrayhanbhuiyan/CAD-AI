package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.FloorPlanAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GeminiFloorPlanService {

    private val jsonAdapter = GeminiClient.moshi.adapter(FloorPlanAnalysis::class.java)

    suspend fun analyzeFloorPlanImage(bitmap: Bitmap): Result<FloorPlanAnalysis> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("API_KEY_MISSING"))
        }

        val base64Image = bitmap.toBase64()
        val prompt = """
            You are an expert structural engineer, CAD designer, and computer vision AI.
            Analyze this floor plan image and generate a complete, high-precision architectural vector layout, statistics, error diagnostics, and BOQ estimation in JSON.
            
            Your response MUST be a single, raw, valid JSON object conforming EXACTLY to the structure described. Do not wrap it in ```json blocks or provide any extra text.
            
            Schema details:
            {
              "stats": {
                "projectName": "Extracted or inferred name of project",
                "floorCount": 1,
                "flatCount": 1,
                "roomCount": 4,
                "bedroomCount": 1,
                "bathroomCount": 1,
                "livingCount": 1,
                "diningCount": 0,
                "kitchenCount": 1,
                "balconyCount": 1,
                "stairCount": 0,
                "liftCount": 0,
                "corridorCount": 0,
                "wallLengthMeters": 48.5,
                "wallAreaSqMeters": 14.55,
                "builtUpAreaSqMeters": 72.0,
                "carpetAreaSqMeters": 61.2,
                "superBuiltUpAreaSqMeters": 86.4,
                "columnCount": 8,
                "estimatedFlatSizeSqMeters": 72.0
              },
              "rooms": [
                {"name": "Master Bedroom", "sizeString": "4.0m x 4.0m", "areaSqMeters": 16.0},
                {"name": "Living Room", "sizeString": "4.0m x 4.5m", "areaSqMeters": 18.0},
                {"name": "Kitchen", "sizeString": "4.0m x 3.5m", "areaSqMeters": 14.0},
                {"name": "Bathroom", "sizeString": "2.0m x 4.0m", "areaSqMeters": 8.0}
              ],
              "openings": [
                {"type": "Door", "sizeString": "1.0m x 2.1m", "count": 1},
                {"type": "Door", "sizeString": "0.9m x 2.1m", "count": 3},
                {"type": "Window", "sizeString": "1.5m x 1.2m", "count": 3},
                {"type": "Window", "sizeString": "0.6m x 0.6m", "count": 1}
              ],
              "elements": [
                {"type": "wall", "start": {"x": 10.0, "y": 10.0}, "end": {"x": 90.0, "y": 10.0}},
                {"type": "wall", "start": {"x": 90.0, "y": 10.0}, "end": {"x": 90.0, "y": 90.0}},
                {"type": "wall", "start": {"x": 90.0, "y": 90.0}, "end": {"x": 10.0, "y": 90.0}},
                {"type": "wall", "start": {"x": 10.0, "y": 90.0}, "end": {"x": 10.0, "y": 10.0}},
                {"type": "wall", "start": {"x": 10.0, "y": 50.0}, "end": {"x": 50.0, "y": 50.0}},
                {"type": "wall", "start": {"x": 50.0, "y": 10.0}, "end": {"x": 50.0, "y": 90.0}},
                {"type": "wall", "start": {"x": 50.0, "y": 55.0}, "end": {"x": 90.0, "y": 55.0}},
                {"type": "door", "start": {"x": 50.0, "y": 20.0}, "end": {"x": 50.0, "y": 26.0}, "label": "D1"},
                {"type": "door", "start": {"x": 50.0, "y": 75.0}, "end": {"x": 50.0, "y": 81.0}, "label": "D2"},
                {"type": "door", "start": {"x": 25.0, "y": 90.0}, "end": {"x": 31.0, "y": 90.0}, "label": "MD"},
                {"type": "door", "start": {"x": 70.0, "y": 55.0}, "end": {"x": 76.0, "y": 55.0}, "label": "D3"},
                {"type": "window", "start": {"x": 10.0, "y": 25.0}, "end": {"x": 10.0, "y": 35.0}, "label": "W1"},
                {"type": "window", "start": {"x": 90.0, "y": 30.0}, "end": {"x": 90.0, "y": 42.0}, "label": "W2"},
                {"type": "window", "start": {"x": 90.0, "y": 70.0}, "end": {"x": 90.0, "y": 78.0}, "label": "W3"},
                {"type": "window", "start": {"x": 30.0, "y": 10.0}, "end": {"x": 36.0, "y": 10.0}, "label": "W4"},
                {"type": "column", "bounds": [{"x": 9.5, "y": 9.5}, {"x": 11.5, "y": 9.5}, {"x": 11.5, "y": 11.5}, {"x": 9.5, "y": 11.5}]},
                {"type": "column", "bounds": [{"x": 88.5, "y": 9.5}, {"x": 90.5, "y": 9.5}, {"x": 90.5, "y": 11.5}, {"x": 88.5, "y": 11.5}]},
                {"type": "column", "bounds": [{"x": 49.0, "y": 9.5}, {"x": 51.0, "y": 9.5}, {"x": 51.0, "y": 11.5}, {"x": 49.0, "y": 11.5}]},
                {"type": "column", "bounds": [{"x": 9.5, "y": 49.0}, {"x": 11.5, "y": 49.0}, {"x": 11.5, "y": 51.0}, {"x": 9.5, "y": 51.0}]},
                {"type": "column", "bounds": [{"x": 49.0, "y": 49.0}, {"x": 51.0, "y": 49.0}, {"x": 51.0, "y": 51.0}, {"x": 49.0, "y": 51.0}]},
                {"type": "column", "bounds": [{"x": 9.5, "y": 88.5}, {"x": 11.5, "y": 88.5}, {"x": 11.5, "y": 90.5}, {"x": 9.5, "y": 90.5}]},
                {"type": "column", "bounds": [{"x": 88.5, "y": 88.5}, {"x": 90.5, "y": 88.5}, {"x": 90.5, "y": 90.5}, {"x": 88.5, "y": 90.5}]},
                {"type": "column", "bounds": [{"x": 49.0, "y": 88.5}, {"x": 51.0, "y": 88.5}, {"x": 51.0, "y": 90.5}, {"x": 49.0, "y": 90.5}]}
              ],
              "errors": [
                {"type": "room_mismatch", "description": "The Kitchen lacks direct ventilation output on the specified boundary.", "severity": "medium", "x": 70.0, "y": 75.0},
                {"type": "missing_door", "description": "Potential missing entrance transition or door threshold at Balcony edge.", "severity": "low", "x": 75.0, "y": 90.0}
              ],
              "boq": [
                {"materialName": "Concrete (M25 Grade)", "category": "Structure", "quantity": 18.5, "unit": "m3", "unitPrice": 110.0, "totalPrice": 2035.0},
                {"materialName": "TMT Steel Rebars", "category": "Structure", "quantity": 1450.0, "unit": "kg", "unitPrice": 1.2, "totalPrice": 1740.0},
                {"materialName": "Red Clay Bricks", "category": "Masonry", "quantity": 4200.0, "unit": "units", "unitPrice": 0.15, "totalPrice": 630.0},
                {"materialName": "Cement Plastering", "category": "Masonry", "quantity": 310.0, "unit": "m2", "unitPrice": 4.5, "totalPrice": 1395.0},
                {"materialName": "Acrylic Emulsion Paint", "category": "Finishing", "quantity": 120.0, "unit": "liters", "unitPrice": 8.0, "totalPrice": 960.0},
                {"materialName": "Vitrified Floor Tiles", "category": "Finishing", "quantity": 65.0, "unit": "m2", "unitPrice": 22.0, "totalPrice": 1430.0},
                {"materialName": "Teakwood Main Door", "category": "Fixtures", "quantity": 1.0, "unit": "units", "unitPrice": 350.0, "totalPrice": 350.0},
                {"materialName": "UPVC Windows", "category": "Fixtures", "quantity": 4.0, "unit": "units", "unitPrice": 120.0, "totalPrice": 480.0}
              ],
              "suggestions": [
                {"category": "Efficiency", "recommendation": "Rearrange kitchen counters into an L-shape to improve work triangle efficiency.", "justification": "Saves 20% traversal distance during meal prep."},
                {"category": "Lighting", "recommendation": "Increase Master Bedroom window width to 1.8m for better natural morning lighting.", "justification": "Enhances diurnal rhythm sync and reduces electrical usage."},
                {"category": "Safety", "recommendation": "Relocate structural column at Bathroom partition 10cm west to keep standard wall thickness.", "justification": "Ensures consistent seismic loading across the central hallway."}
              ]
            }
            
            CRITICAL CRITERIA FOR COORDINATES:
            - All coordinates (x, y) MUST be normalized floats between 0.0 and 100.0, where (0,0) is top-left and (100,100) is bottom-right.
            - Trace walls carefully along room boundaries to represent the layout.
            - Place doors and windows at wall gaps.
            - Ensure room labels reflect the text read via OCR or logical placement.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("No content returned from Gemini"))
            
            val cleanJsonText = sanitizeJson(jsonText)
            val analysis = jsonAdapter.fromJson(cleanJsonText)
                ?: return@withContext Result.failure(Exception("Failed to parse analysis JSON"))
            
            Result.success(analysis)
        } catch (e: Exception) {
            Log.e("GeminiFloorPlanService", "Error analyzing floor plan", e)
            Result.failure(e)
        }
    }

    private fun sanitizeJson(rawJson: String): String {
        var clean = rawJson.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        }
        if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
