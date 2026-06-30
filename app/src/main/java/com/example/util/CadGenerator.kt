package com.example.util

import com.example.data.CadElement
import com.example.data.Point2D

object CadGenerator {

    fun generateDxf(elements: List<CadElement>): String {
        val builder = StringBuilder()
        // DXF Header
        builder.append("0\nSECTION\n2\nHEADER\n9\n\$ACADVER\n1\nAC1006\n0\nENDSEC\n")

        // DXF Tables (Layers)
        builder.append("0\nSECTION\n2\nTABLES\n0\nTABLE\n2\nLAYER\n70\n6\n")
        
        // Define layers with colors: 1=Red (Doors), 2=Yellow (Columns), 3=Green (Text), 4=Cyan (Windows), 5=Blue (Dims), 7=White/Black (Walls)
        val layers = listOf(
            Pair("Walls", 7),
            Pair("Doors", 1),
            Pair("Windows", 4),
            Pair("Columns", 2),
            Pair("Text", 3),
            Pair("Dimensions", 5)
        )
        
        for (layer in layers) {
            builder.append("0\nLAYER\n2\n${layer.first}\n70\n64\n62\n${layer.second}\n6\nCONTINUOUS\n")
        }
        builder.append("0\nENDTAB\n0\nENDSEC\n")

        // DXF Entities
        builder.append("0\nSECTION\n2\nENTITIES\n")

        for (el in elements) {
            val layer = getLayerForType(el.type)
            when {
                el.start != null && el.end != null -> {
                    // Draw line
                    builder.append("0\nLINE\n8\n$layer\n")
                    builder.append("10\n${el.start.x}\n20\n${el.start.y}\n30\n0.0\n")
                    builder.append("11\n${el.end.x}\n21\n${el.end.y}\n31\n0.0\n")
                }
                el.bounds != null && el.bounds.size >= 2 -> {
                    // Draw consecutive lines for polygon bounds
                    for (i in 0 until el.bounds.size) {
                        val p1 = el.bounds[i]
                        val p2 = el.bounds[(i + 1) % el.bounds.size]
                        builder.append("0\nLINE\n8\n$layer\n")
                        builder.append("10\n${p1.x}\n20\n${p1.y}\n30\n0.0\n")
                        builder.append("11\n${p2.x}\n21\n${p2.y}\n31\n0.0\n")
                    }
                }
            }

            // If there's a label, add Text
            if (!el.label.isNullOrEmpty()) {
                val textX = when {
                    el.start != null && el.end != null -> (el.start.x + el.end.x) / 2
                    el.bounds != null && el.bounds.isNotEmpty() -> el.bounds.map { it.x }.average().toFloat()
                    else -> 50f
                }
                val textY = when {
                    el.start != null && el.end != null -> (el.start.y + el.end.y) / 2
                    el.bounds != null && el.bounds.isNotEmpty() -> el.bounds.map { it.y }.average().toFloat()
                    else -> 50f
                }
                builder.append("0\nTEXT\n8\nText\n")
                builder.append("10\n$textX\n20\n$textY\n30\n0.0\n")
                builder.append("40\n1.8\n") // Text height
                builder.append("1\n${el.label}\n")
            }
        }

        builder.append("0\nENDSEC\n0\nEOF\n")
        return builder.toString()
    }

    fun generateSvg(elements: List<CadElement>): String {
        val builder = StringBuilder()
        builder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"100%\" height=\"100%\" style=\"background-color: #0F172A;\">\n")
        
        // Group by layers
        val groups = elements.groupBy { getLayerForType(it.type) }
        
        // Stroke colors matching modern dark mode theme
        val colors = mapOf(
            "Walls" to "#38BDF8",       // Sky blue
            "Doors" to "#F43F5E",       // Rose red
            "Windows" to "#34D399",     // Emerald green
            "Columns" to "#FBBF24",     // Amber orange
            "Text" to "#E2E8F0",        // Slate white
            "Dimensions" to "#A78BFA"   // Purple
        )

        for ((layer, layerElements) in groups) {
            val color = colors[layer] ?: "#FFFFFF"
            val strokeWidth = if (layer == "Walls") "0.8" else "0.4"
            builder.append("  <g id=\"$layer\" stroke=\"$color\" stroke-width=\"$strokeWidth\" fill=\"none\">\n")
            
            for (el in layerElements) {
                when {
                    el.start != null && el.end != null -> {
                        builder.append("    <line x1=\"${el.start.x}\" y1=\"${el.start.y}\" x2=\"${el.end.x}\" y2=\"${el.end.y}\" />\n")
                    }
                    el.bounds != null && el.bounds.isNotEmpty() -> {
                        val pointsStr = el.bounds.joinToString(" ") { "${it.x},${it.y}" }
                        builder.append("    <polygon points=\"$pointsStr\" fill=\"none\" />\n")
                    }
                }
                
                if (!el.label.isNullOrEmpty()) {
                    val textX = when {
                        el.start != null && el.end != null -> (el.start.x + el.end.x) / 2
                        el.bounds != null && el.bounds.isNotEmpty() -> el.bounds.map { it.x }.average().toFloat()
                        else -> 50f
                    }
                    val textY = when {
                        el.start != null && el.end != null -> (el.start.y + el.end.y) / 2
                        el.bounds != null && el.bounds.isNotEmpty() -> el.bounds.map { it.y }.average().toFloat()
                        else -> 50f
                    }
                    // Text uses fill color and no stroke
                    builder.append("    <text x=\"$textX\" y=\"$textY\" fill=\"#E2E8F0\" stroke=\"none\" font-family=\"sans-serif\" font-size=\"1.5\" text-anchor=\"middle\" alignment-baseline=\"middle\">${el.label}</text>\n")
                }
            }
            builder.append("  </g>\n")
        }
        
        builder.append("</svg>")
        return builder.toString()
    }

    private fun getLayerForType(type: String): String {
        return when (type.lowercase()) {
            "wall" -> "Walls"
            "door" -> "Doors"
            "window" -> "Windows"
            "column" -> "Columns"
            "text" -> "Text"
            "dimension" -> "Dimensions"
            else -> "Walls"
        }
    }
}
