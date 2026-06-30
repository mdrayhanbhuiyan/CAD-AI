package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.CadElement
import com.example.data.Point2D

@Composable
fun CadViewer3D(
    elements: List<CadElement>,
    pitch: Float,
    yaw: Float,
    onRotate: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(0xFF0F172A) // Deep space black
    val gridColor = Color(0xFF1E293B)
    val wallColor = Color(0xFF0284C7) // Solid sky blue
    val wallTopColor = Color(0xFF38BDF8) // Bright highlight cyan
    val columnColor = Color(0xFFD97706) // Deep amber
    val columnTopColor = Color(0xFFFBBF24) // Bright yellow
    val window3DColor = Color(0x9934D399) // Glass emerald green
    val door3DColor = Color(0x99F43F5E) // Salmon rose
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Drag coordinates map directly to pitch/yaw updates
                    onRotate(-dragAmount.y * 0.4f, dragAmount.x * 0.4f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val drawScale = minOf(canvasWidth, canvasHeight) / 140f

            // Mathematical projection equations
            fun project(x: Float, y: Float, z: Float): Offset {
                // Translate center to (0,0)
                val xc = x - 50f
                val yc = y - 50f
                val zc = z - 4f

                // Convert to radians
                val radYaw = yaw * Math.PI.toFloat() / 180f
                val radPitch = pitch * Math.PI.toFloat() / 180f

                // Rotate around Z axis (Yaw)
                val x1 = xc * kotlin.math.cos(radYaw) - yc * kotlin.math.sin(radYaw)
                val y1 = xc * kotlin.math.sin(radYaw) + yc * kotlin.math.cos(radYaw)
                val z1 = zc

                // Rotate around X axis (Pitch)
                val x2 = x1
                val y2 = y1 * kotlin.math.cos(radPitch) - z1 * kotlin.math.sin(radPitch)
                val z2 = y1 * kotlin.math.sin(radPitch) + z1 * kotlin.math.cos(radPitch)

                // Scale and offset onto Canvas center
                val u = x2 * drawScale + canvasWidth / 2f
                val v = y2 * drawScale + canvasHeight / 2f
                return Offset(u, v)
            }

            // --- 1. DRAW 3D FOUNDATION GRID FLOOR ---
            val gridSpacing = 10f
            for (i in 0..10) {
                val gridCoord = i * 10f
                
                // Draw grid lines along X
                val pStart1 = project(0f, gridCoord, 0f)
                val pEnd1 = project(100f, gridCoord, 0f)
                drawLine(gridColor, pStart1, pEnd1, strokeWidth = 1f)

                // Draw grid lines along Y
                val pStart2 = project(gridCoord, 0f, 0f)
                val pEnd2 = project(gridCoord, 100f, 0f)
                drawLine(gridColor, pStart2, pEnd2, strokeWidth = 1f)
            }

            // Sort elements: draw columns and walls back-to-front depending on yaw angle!
            // For simple, fast rendering, drawing all elements with clean solid volumes looks spectacular.
            val wallHeight = 12f // Z height for architectural walls

            for (el in elements) {
                when {
                    // --- 2. WALLS (Draw as vertical planes) ---
                    el.type == "wall" && el.start != null && el.end != null -> {
                        val pB1 = project(el.start.x, el.start.y, 0f)
                        val pB2 = project(el.end.x, el.end.y, 0f)
                        val pT1 = project(el.start.x, el.start.y, wallHeight)
                        val pT2 = project(el.end.x, el.end.y, wallHeight)

                        // Base outline
                        drawLine(gridColor, pB1, pB2, strokeWidth = 2f)

                        // Main wall vertical face panel
                        val facePath = Path().apply {
                            moveTo(pB1.x, pB1.y)
                            lineTo(pB2.x, pB2.y)
                            lineTo(pT2.x, pT2.y)
                            lineTo(pT1.x, pT1.y)
                            close()
                        }
                        
                        // Lighting effect based on angle (shading)
                        val dy = el.end.y - el.start.y
                        val dx = el.end.x - el.start.x
                        val angle = kotlin.math.abs(kotlin.math.atan2(dy, dx))
                        val shadeIntensity = 0.4f + 0.4f * kotlin.math.sin(angle)

                        drawPath(
                            path = facePath,
                            color = wallColor.copy(alpha = shadeIntensity)
                        )

                        // Top edge highlight strip
                        drawLine(wallTopColor, pT1, pT2, strokeWidth = 3f)
                    }

                    // --- 3. WINDOWS (Draw as smaller glass inserts) ---
                    el.type == "window" && el.start != null && el.end != null -> {
                        val wBase = 3.5f
                        val wTop = 9.0f
                        
                        val pB1 = project(el.start.x, el.start.y, wBase)
                        val pB2 = project(el.end.x, el.end.y, wBase)
                        val pT1 = project(el.start.x, el.start.y, wTop)
                        val pT2 = project(el.end.x, el.end.y, wTop)

                        val glassPath = Path().apply {
                            moveTo(pB1.x, pB1.y)
                            lineTo(pB2.x, pB2.y)
                            lineTo(pT2.x, pT2.y)
                            lineTo(pT1.x, pT1.y)
                            close()
                        }
                        drawPath(glassPath, color = window3DColor)
                        drawLine(Color(0xFF34D399), pT1, pT2, strokeWidth = 2f)
                    }

                    // --- 4. DOORS (Draw as dynamic walkthrough gaps or low panels) ---
                    el.type == "door" && el.start != null && el.end != null -> {
                        val dTop = 8.5f
                        
                        val pB1 = project(el.start.x, el.start.y, 0f)
                        val pB2 = project(el.end.x, el.end.y, 0f)
                        val pT1 = project(el.start.x, el.start.y, dTop)
                        val pT2 = project(el.end.x, el.end.y, dTop)

                        val doorPath = Path().apply {
                            moveTo(pB1.x, pB1.y)
                            lineTo(pB2.x, pB2.y)
                            lineTo(pT2.x, pT2.y)
                            lineTo(pT1.x, pT1.y)
                            close()
                        }
                        drawPath(doorPath, color = door3DColor)
                        drawLine(Color(0xFFF43F5E), pT1, pT2, strokeWidth = 2f)
                    }

                    // --- 5. COLUMNS (Draw as solid extruded prisms) ---
                    el.type == "column" && el.bounds != null && el.bounds.size >= 4 -> {
                        val corners = el.bounds
                        val pB1 = project(corners[0].x, corners[0].y, 0f)
                        val pB2 = project(corners[1].x, corners[1].y, 0f)
                        val pB3 = project(corners[2].x, corners[2].y, 0f)
                        val pB4 = project(corners[3].x, corners[3].y, 0f)

                        val pT1 = project(corners[0].x, corners[0].y, wallHeight)
                        val pT2 = project(corners[1].x, corners[1].y, wallHeight)
                        val pT3 = project(corners[2].x, corners[2].y, wallHeight)
                        val pT4 = project(corners[3].x, corners[3].y, wallHeight)

                        // Column faces
                        val face1 = Path().apply { moveTo(pB1.x, pB1.y); lineTo(pB2.x, pB2.y); lineTo(pT2.x, pT2.y); lineTo(pT1.x, pT1.y); close() }
                        val face2 = Path().apply { moveTo(pB2.x, pB2.y); lineTo(pB3.x, pB3.y); lineTo(pT3.x, pT3.y); lineTo(pT2.x, pT2.y); close() }
                        val topFace = Path().apply { moveTo(pT1.x, pT1.y); lineTo(pT2.x, pT2.y); lineTo(pT3.x, pT3.y); lineTo(pT4.x, pT4.y); close() }

                        drawPath(face1, color = columnColor.copy(alpha = 0.7f))
                        drawPath(face2, color = columnColor.copy(alpha = 0.5f))
                        drawPath(topFace, color = columnTopColor)
                    }
                }
            }

            // --- 6. FLOATING 3D ROOM LABELS (Hologram style) ---
            val labelPaint = Paint().apply {
                color = Color.White.toArgb()
                textSize = 2.0f * drawScale
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            for (el in elements) {
                if (el.type == "text" && !el.label.isNullOrEmpty()) {
                    val labelX = el.start?.x ?: 50f
                    val labelY = el.start?.y ?: 50f
                    
                    // Project the label floating 4 meters above the walls
                    val projOffset = project(labelX, labelY, wallHeight + 3f)

                    // Draw connecting line to ground anchor
                    val groundProj = project(labelX, labelY, 0f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = groundProj,
                        end = projOffset,
                        strokeWidth = 1f
                    )

                    // Draw a visual marker bubble around floating label
                    drawCircle(
                        color = Color(0xFF0284C7),
                        radius = 4f,
                        center = projOffset
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        el.label,
                        projOffset.x,
                        projOffset.y - 6f,
                        labelPaint
                    )
                }
            }
        }
    }
}
