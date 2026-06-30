package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CadElement
import com.example.data.DrawingError
import com.example.ui.theme.*

@Composable
fun CadViewer2D(
    elements: List<CadElement>,
    layerToggles: Map<String, Boolean>,
    errors: List<DrawingError>,
    selectedError: DrawingError?,
    onSelectError: (DrawingError) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom and pan state
    var scale by remember { mutableStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Ruler measurement tool state
    var rulerModeEnabled by remember { mutableStateOf(false) }
    var rulerPointA by remember { mutableStateOf<com.example.data.Point2D?>(null) }
    var rulerPointB by remember { mutableStateOf<com.example.data.Point2D?>(null) }

    // Color definitions matching architectural dark canvas
    val bgColor = Color(0xFF0F172A) // Deep slate dark blue
    val wallColor = Color(0xFF38BDF8) // Bright sky blue
    val doorColor = Color(0xFFF43F5E) // Salmon rose
    val windowColor = Color(0xFF34D399) // Emerald green
    val columnColor = Color(0xFFFBBF24) // Golden amber
    val balconyColor = Color(0xFF818CF8) // Indigo
    val dimColor = Color(0xFFA78BFA) // Soft violet
    val errorColor = Color(0xFFEF4444) // Bright red

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .pointerInput(rulerModeEnabled) {
                if (rulerModeEnabled) {
                    detectTapGestures { pressOffset ->
                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()
                        
                        // Convert screen click to blueprint coordinate space
                        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                        val localPt = (pressOffset - offset - center) / scale + center
                        
                        val minDim = minOf(canvasWidth, canvasHeight)
                        val padding = minDim * 0.1f
                        val drawScale = (minDim - padding * 2) / 100f
                        
                        val bpX = (localPt.x - padding) / drawScale
                        val bpY = (localPt.y - padding) / drawScale
                        val newPoint = com.example.data.Point2D(bpX, bpY)

                        if (rulerPointA == null) {
                            rulerPointA = newPoint
                        } else if (rulerPointB == null) {
                            rulerPointB = newPoint
                        } else {
                            rulerPointA = newPoint
                            rulerPointB = null
                        }
                    }
                } else {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5.0f)
                        offset += pan
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val minDim = minOf(canvasWidth, canvasHeight)
            val padding = minDim * 0.1f
            val drawScale = (minDim - padding * 2) / 100f // Scaling percentage to pixels

            // Apply zoom/pan transform
            drawContext.transform.translate(offset.x, offset.y)
            drawContext.transform.scale(scale, scale, Offset(canvasWidth / 2, canvasHeight / 2))

            // Draw grid background
            val gridSpacing = drawScale * 10f
            val gridPaint = Paint().apply {
                color = Color(0xFF1E293B).toArgb()
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }
            
            // Render thin coordinate grid
            for (i in -20..120) {
                val pos = i * gridSpacing + padding
                // Vertical lines
                drawContext.canvas.nativeCanvas.drawLine(pos, -500f, pos, canvasHeight + 500f, gridPaint)
                // Horizontal lines
                drawContext.canvas.nativeCanvas.drawLine(-500f, pos, canvasWidth + 500f, pos, gridPaint)
            }

            // Draw elements based on toggles
            for (el in elements) {
                val isVisible = when (el.type.lowercase()) {
                    "wall" -> layerToggles["Walls"] ?: true
                    "door" -> layerToggles["Doors"] ?: true
                    "window" -> layerToggles["Windows"] ?: true
                    "column" -> layerToggles["Columns"] ?: true
                    "balcony" -> layerToggles["Doors"] ?: true
                    "stair" -> layerToggles["Walls"] ?: true
                    else -> true
                }

                if (!isVisible) continue

                when {
                    el.start != null && el.end != null -> {
                        // Convert normalized (0-100) to viewport coordinates
                        val startX = el.start.x * drawScale + padding
                        val startY = el.start.y * drawScale + padding
                        val endX = el.end.x * drawScale + padding
                        val endY = el.end.y * drawScale + padding

                        when (el.type.lowercase()) {
                            "wall" -> {
                                drawLine(
                                    color = wallColor,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 5f * drawScale / 12f
                                )
                            }
                            "door" -> {
                                // Draw Door panel
                                drawLine(
                                    color = doorColor,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 3f * drawScale / 12f
                                )
                                // Draw swing arc (quarter circle proxy line)
                                val radius = kotlin.math.sqrt(
                                    (endX - startX) * (endX - startX) + (endY - startY) * (endY - startY)
                                )
                                drawCircle(
                                    color = doorColor.copy(alpha = 0.3f),
                                    radius = radius,
                                    center = Offset(startX, startY),
                                    style = Stroke(
                                        width = 1.5f * drawScale / 12f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                                    )
                                )
                            }
                            "window" -> {
                                // Draw double lines for window representation
                                val dx = endX - startX
                                val dy = endY - startY
                                val len = kotlin.math.sqrt(dx * dx + dy * dy)
                                val ux = -dy / len * (1.2f * drawScale / 12f)
                                val uy = dx / len * (1.2f * drawScale / 12f)

                                drawLine(
                                    color = windowColor,
                                    start = Offset(startX + ux, startY + uy),
                                    end = Offset(endX + ux, endY + uy),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = windowColor,
                                    start = Offset(startX - ux, startY - uy),
                                    end = Offset(endX - ux, endY - uy),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = windowColor.copy(alpha = 0.5f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 1f
                                )
                            }
                            else -> {
                                drawLine(
                                    color = Color.Gray,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }

                    el.bounds != null && el.bounds.isNotEmpty() -> {
                        // Draw Polygon structure (columns, balconies, etc.)
                        val points = el.bounds.map { Offset(it.x * drawScale + padding, it.y * drawScale + padding) }
                        
                        when (el.type.lowercase()) {
                            "column" -> {
                                if (points.size >= 4) {
                                    drawRect(
                                        color = columnColor,
                                        topLeft = Offset(points[0].x, points[0].y),
                                        size = Size(points[2].x - points[0].x, points[2].y - points[0].y)
                                    )
                                }
                            }
                            "balcony" -> {
                                for (i in points.indices) {
                                    val next = points[(i + 1) % points.size]
                                    drawLine(
                                        color = balconyColor,
                                        start = points[i],
                                        end = next,
                                        strokeWidth = 2f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Render Room Labels if text is toggled on
                if (!el.label.isNullOrEmpty() && (layerToggles["Text"] ?: true)) {
                    val labelX = when {
                        el.start != null && el.end != null -> (el.start.x + el.end.x) / 2
                        el.bounds != null && el.bounds.isNotEmpty() -> el.bounds.map { it.x }.average().toFloat()
                        else -> 50f
                    }
                    val labelY = when {
                        el.start != null && el.end != null -> (el.start.y + el.end.y) / 2
                        el.bounds != null && el.bounds.isNotEmpty() -> el.bounds.map { it.y }.average().toFloat()
                        else -> 50f
                    }

                    // Only render if it's placed as pure annotation/room title
                    if (el.type == "text") {
                        val textPaint = Paint().apply {
                            color = Color.White.toArgb()
                            textSize = 3f * drawScale / 12f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                            textAlign = Paint.Align.CENTER
                        }
                        
                        // Transparent backdrop tag for high contrast readability
                        drawRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            topLeft = Offset((labelX - 12f) * drawScale + padding, (labelY - 2.5f) * drawScale + padding),
                            size = Size(24f * drawScale, 5f * drawScale)
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            el.label,
                            labelX * drawScale + padding,
                            labelY * drawScale + padding + (1f * drawScale / 12f),
                            textPaint
                        )
                    }
                }
            }

            // Draw Dimensions (toggled on)
            if (layerToggles["Dimensions"] ?: true) {
                // Generate a few sample horizontal and vertical dimension annotations at layout edges
                val dimPaint = Paint().apply {
                    color = dimColor.toArgb()
                    textSize = 2.0f * drawScale / 12f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    textAlign = Paint.Align.CENTER
                }
                
                // Top dimensions line
                val dY = 5f * drawScale + padding
                drawLine(
                    color = dimColor,
                    start = Offset(10f * drawScale + padding, dY),
                    end = Offset(90f * drawScale + padding, dY),
                    strokeWidth = 1f
                )
                // Dimension tick marks
                drawLine(color = dimColor, start = Offset(10f * drawScale + padding, dY - 4f), end = Offset(10f * drawScale + padding, dY + 4f), strokeWidth = 1.5f)
                drawLine(color = dimColor, start = Offset(50f * drawScale + padding, dY - 4f), end = Offset(50f * drawScale + padding, dY + 4f), strokeWidth = 1.5f)
                drawLine(color = dimColor, start = Offset(90f * drawScale + padding, dY - 4f), end = Offset(90f * drawScale + padding, dY + 4f), strokeWidth = 1.5f)
                
                drawContext.canvas.nativeCanvas.drawText("4.00 m", 30f * drawScale + padding, dY - 5f, dimPaint)
                drawContext.canvas.nativeCanvas.drawText("4.00 m", 70f * drawScale + padding, dY - 5f, dimPaint)
            }

            // Draw Diagnostic Error Markers (toggled on)
            if (layerToggles["Errors"] ?: true) {
                for (error in errors) {
                    val ex = error.x * drawScale + padding
                    val ey = error.y * drawScale + padding
                    
                    val isSelected = error == selectedError
                    val radius = if (isSelected) 14f else 8f
                    val alpha = if (isSelected) 0.8f else 0.4f

                    // Pulsing outer warning circle
                    drawCircle(
                        color = errorColor.copy(alpha = alpha),
                        radius = radius * drawScale / 12f,
                        center = Offset(ex, ey)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = (radius / 2.5f) * drawScale / 12f,
                        center = Offset(ex, ey)
                    )
                }
            }

            // Draw Ruler Points, Connection Line, and Distance Label in local coordinates
            if (rulerModeEnabled) {
                val rA = rulerPointA
                val rB = rulerPointB

                val rulerPrimaryColor = SophisticatedPrimaryLight
                val rulerSecondaryColor = SophisticatedAccent

                if (rA != null) {
                    val pAX = rA.x * drawScale + padding
                    val pAY = rA.y * drawScale + padding

                    // Draw outer pulsing circle for A
                    drawCircle(
                        color = rulerPrimaryColor.copy(alpha = 0.4f),
                        radius = 16f * drawScale / 12f,
                        center = Offset(pAX, pAY)
                    )
                    // Draw inner solid dot
                    drawCircle(
                        color = rulerPrimaryColor,
                        radius = 6f * drawScale / 12f,
                        center = Offset(pAX, pAY)
                    )

                    // Text Paint for ruler labels
                    val rulerTextPaint = Paint().apply {
                        color = Color.White.toArgb()
                        textSize = 2.5f * drawScale / 12f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        "A",
                        pAX,
                        pAY - 8f * drawScale / 12f,
                        rulerTextPaint
                    )

                    if (rB != null) {
                        val pBX = rB.x * drawScale + padding
                        val pBY = rB.y * drawScale + padding

                        // Draw outer pulsing circle for B
                        drawCircle(
                            color = rulerSecondaryColor.copy(alpha = 0.4f),
                            radius = 16f * drawScale / 12f,
                            center = Offset(pBX, pBY)
                        )
                        // Draw inner solid dot
                        drawCircle(
                            color = rulerSecondaryColor,
                            radius = 6f * drawScale / 12f,
                            center = Offset(pBX, pBY)
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            "B",
                            pBX,
                            pBY - 8f * drawScale / 12f,
                            rulerTextPaint
                        )

                        // Draw connecting line
                        drawLine(
                            color = rulerPrimaryColor,
                            start = Offset(pAX, pAY),
                            end = Offset(pBX, pBY),
                            strokeWidth = 3f * drawScale / 12f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
                        )

                        // Calculate distance (10 units = 1.0 meter)
                        val dx = rB.x - rA.x
                        val dy = rB.y - rA.y
                        val distUnits = kotlin.math.sqrt(dx * dx + dy * dy)
                        val distMeters = distUnits / 10f
                        val distFeet = distMeters * 3.28084f

                        val distanceStr = String.format(java.util.Locale.US, "%.2f m / %.2f ft", distMeters, distFeet)

                        // Draw distance label in the middle
                        val midX = (pAX + pBX) / 2
                        val midY = (pAY + pBY) / 2

                        val labelPaint = Paint().apply {
                            color = SophisticatedAccent.toArgb()
                            textSize = 2.4f * drawScale / 12f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                            textAlign = Paint.Align.CENTER
                        }

                        // Draw background rectangle for distance label text to ensure readability
                        val rectW = 28f * drawScale
                        val rectH = 5.5f * drawScale
                        drawRect(
                            color = Color(0xCC0F172A),
                            topLeft = Offset(midX - rectW / 2, midY - rectH / 2),
                            size = Size(rectW, rectH)
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            distanceStr,
                            midX,
                            midY + 1f * drawScale / 12f,
                            labelPaint
                        )
                    }
                }
            }
        }

        // --- INTERACTIVE CANVAS OVERLAY CONTROLS ---

        // 1. Zoom Scale Indicator & Quick Reset Button (Top-Left)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(SophisticatedSurface.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ZoomIn,
                contentDescription = null,
                tint = SophisticatedPrimaryLight,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${(scale * 100).toInt()}%",
                color = SophisticatedTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            IconButton(
                onClick = {
                    scale = 1.0f
                    offset = Offset.Zero
                },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset View",
                    tint = SophisticatedTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // 1b. Interactive Ruler Instruction Banner (Top-Center)
        if (rulerModeEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .background(bgColor.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, SophisticatedPrimaryLight.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when {
                        rulerPointA == null -> "Tap anywhere to place Point A"
                        rulerPointB == null -> "Tap another point for Point B"
                        else -> "Tap again to reset Point A"
                    },
                    color = SophisticatedTextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 1c. Ruler Mode Toggle Button (Top-Right)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(
                    if (rulerModeEnabled) SophisticatedPrimary else SophisticatedSurface.copy(alpha = 0.85f),
                    RoundedCornerShape(20.dp)
                )
                .border(
                    BorderStroke(1.dp, if (rulerModeEnabled) SophisticatedPrimaryLight else Color.White.copy(alpha = 0.08f)),
                    RoundedCornerShape(20.dp)
                )
                .clickable {
                    rulerModeEnabled = !rulerModeEnabled
                    if (!rulerModeEnabled) {
                        rulerPointA = null
                        rulerPointB = null
                    }
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Straighten,
                contentDescription = "Ruler Mode",
                tint = if (rulerModeEnabled) Color.White else SophisticatedPrimaryLight,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = if (rulerModeEnabled) "RULER ON" else "MEASURE",
                color = if (rulerModeEnabled) Color.White else SophisticatedTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // 2. Control Cluster (Bottom-Right): Zoom In/Out & Precise Pan D-Pad
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface.copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Zoom +/- Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { scale = (scale / 1.15f).coerceIn(0.5f, 5.0f) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(SophisticatedSurfaceAlt, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = SophisticatedTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { scale = (scale * 1.15f).coerceIn(0.5f, 5.0f) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(SophisticatedSurfaceAlt, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = SophisticatedTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Separation line
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.08f))
                    )

                    // Directional D-pad for ultra-precise manual panning
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Up button
                        IconButton(
                            onClick = { offset = offset.copy(y = offset.y + 40f) },
                            modifier = Modifier
                                .size(28.dp)
                                .background(SophisticatedSurfaceAlt, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Pan Up",
                                tint = SophisticatedPrimaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Middle row: Left, Center-recenter, Right
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { offset = offset.copy(x = offset.x + 40f) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(SophisticatedSurfaceAlt, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Pan Left",
                                    tint = SophisticatedPrimaryLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Precise reset indicator/button
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(SophisticatedPrimary.copy(alpha = 0.15f), CircleShape)
                                    .border(BorderStroke(1.dp, SophisticatedPrimaryLight.copy(alpha = 0.3f)), CircleShape)
                                    .clickable {
                                        scale = 1.0f
                                        offset = Offset.Zero
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SophisticatedPrimaryLight, CircleShape)
                                )
                            }

                            IconButton(
                                onClick = { offset = offset.copy(x = offset.x - 40f) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(SophisticatedSurfaceAlt, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Pan Right",
                                    tint = SophisticatedPrimaryLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Down button
                        IconButton(
                            onClick = { offset = offset.copy(y = offset.y - 40f) },
                            modifier = Modifier
                                .size(28.dp)
                                .background(SophisticatedSurfaceAlt, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Pan Down",
                                tint = SophisticatedPrimaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
