package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CadViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: CadViewModel,
    modifier: Modifier = Modifier
) {
    val isKeyConfigured = viewModel.isApiKeyConfigured

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SophisticatedBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Title in UPPERCASE tracking style
        Text(
            text = "AI CAD SYSTEM",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SophisticatedTextDim,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Analyzer Configuration",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = SophisticatedTextPrimary,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- GEMINI API STATUS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val keyColor = if (isKeyConfigured) Color(0xFF10B981) else SophisticatedPrimaryLight
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(keyColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = keyColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Google Gemini API Integration",
                            fontWeight = FontWeight.SemiBold,
                            color = SophisticatedTextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isKeyConfigured) "Status: Connected & Configured" else "Status: Sandbox/Demo Mode Active",
                            fontSize = 11.sp,
                            color = if (isKeyConfigured) Color(0xFF10B981) else SophisticatedTextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isKeyConfigured) {
                        "Your Gemini API Key is successfully loaded via AI Studio Secrets. Fully functional AI floor plan scanning is unlocked!"
                    } else {
                        "No custom API Key detected in AI Studio Secrets. The application will gracefully guide you using high-fidelity pre-rendered floor plan playgrounds."
                    },
                    fontSize = 12.sp,
                    color = SophisticatedTextMuted,
                    lineHeight = 18.sp
                )
                if (!isKeyConfigured) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "To unlock custom plans, open the AI Studio Build sidebar, select the 'Secrets' panel, and add your GEMINI_API_KEY.",
                        fontSize = 11.sp,
                        color = SophisticatedPrimaryLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- EXPORT ENGINE PARAMETERS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CAD Export Engine Specifications",
                    fontWeight = FontWeight.SemiBold,
                    color = SophisticatedTextPrimary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DXF File Version", color = SophisticatedTextMuted, fontSize = 12.sp)
                    Text("AutoCAD R12 (AC1006)", color = SophisticatedTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SVG Coordinate Standard", color = SophisticatedTextMuted, fontSize = 12.sp)
                    Text("W3C SVG 1.1 Responsive", color = SophisticatedTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("JSON Report Struct", color = SophisticatedTextMuted, fontSize = 12.sp)
                    Text("RFC 8259 Standard", color = SophisticatedTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DEVELOPER CREDITS & BUILD INFO ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurfaceAlt),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SophisticatedPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SophisticatedPrimaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AI CAD Floor Plan Analyzer & CAD Generator",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SophisticatedTextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Version 1.2.0 (Stable Build)\nPowered by Gemini 3.5 Flash",
                    fontSize = 11.sp,
                    color = SophisticatedTextDim,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Designed for high-performance architectural preview, material costing, and structural design checks directly on mobile form-factors.",
                    fontSize = 11.sp,
                    color = SophisticatedTextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
