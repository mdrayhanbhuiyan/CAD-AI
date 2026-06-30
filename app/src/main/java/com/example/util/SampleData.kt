package com.example.util

import com.example.data.*

object SampleData {

    val sample1: FloorPlanAnalysis by lazy {
        val elements = mutableListOf<CadElement>()
        
        // --- OUTER WALLS ---
        elements.add(CadElement("wall", Point2D(10f, 10f), Point2D(90f, 10f))) // Top Wall
        elements.add(CadElement("wall", Point2D(90f, 10f), Point2D(90f, 90f))) // Right Wall
        elements.add(CadElement("wall", Point2D(90f, 90f), Point2D(10f, 90f))) // Bottom Wall
        elements.add(CadElement("wall", Point2D(10f, 90f), Point2D(10f, 10f))) // Left Wall
        
        // --- INNER DIVISION WALLS ---
        elements.add(CadElement("wall", Point2D(10f, 50f), Point2D(50f, 50f))) // Bed/Bath divider horizontally
        elements.add(CadElement("wall", Point2D(50f, 10f), Point2D(50f, 90f))) // Central vertical hallway wall
        elements.add(CadElement("wall", Point2D(50f, 55f), Point2D(90f, 55f))) // Kitchen/Living divider horizontally
        
        // --- COLUMNS (Corners & Intersections) ---
        elements.add(CadElement("column", bounds = listOf(Point2D(9.5f, 9.5f), Point2D(11.5f, 9.5f), Point2D(11.5f, 11.5f), Point2D(9.5f, 11.5f))))
        elements.add(CadElement("column", bounds = listOf(Point2D(88.5f, 9.5f), Point2D(90.5f, 9.5f), Point2D(90.5f, 11.5f), Point2D(88.5f, 11.5f))))
        elements.add(CadElement("column", bounds = listOf(Point2D(49f, 9.5f), Point2D(51f, 9.5f), Point2D(51f, 11.5f), Point2D(49f, 11.5f))))
        elements.add(CadElement("column", bounds = listOf(Point2D(9.5f, 49f), Point2D(11.5f, 49f), Point2D(11.5f, 51f), Point2D(9.5f, 51f))))
        elements.add(CadElement("column", bounds = listOf(Point2D(49f, 49f), Point2D(51f, 49f), Point2D(51f, 51f), Point2D(49f, 51f))))
        elements.add(CadElement("column", bounds = listOf(Point2D(9.5f, 88.5f), Point2D(11.5f, 88.5f), Point2D(11.5f, 90.5f), Point2D(9.5f, 90.5f))))
        elements.add(CadElement("column", bounds = listOf(Point2D(88.5f, 88.5f), Point2D(90.5f, 88.5f), Point2D(90.5f, 90.5f), Point2D(88.5f, 90.5f))))
        elements.add(CadElement("column", bounds = listOf(Point2D(49f, 88.5f), Point2D(51f, 88.5f), Point2D(51f, 90.5f), Point2D(49f, 90.5f))))

        // --- DOORS ---
        elements.add(CadElement("door", Point2D(50f, 20f), Point2D(50f, 26f), label = "D1")) // Master Bed Door
        elements.add(CadElement("door", Point2D(50f, 75f), Point2D(50f, 81f), label = "D2")) // Bathroom Door
        elements.add(CadElement("door", Point2D(25f, 90f), Point2D(31f, 90f), label = "MD")) // Main Entrance Door
        elements.add(CadElement("door", Point2D(70f, 55f), Point2D(76f, 55f), label = "D3")) // Kitchen Door
        
        // --- WINDOWS ---
        elements.add(CadElement("window", Point2D(10f, 25f), Point2D(10f, 35f), label = "W1")) // Bedroom Window
        elements.add(CadElement("window", Point2D(90f, 30f), Point2D(90f, 42f), label = "W2")) // Living Window
        elements.add(CadElement("window", Point2D(90f, 70f), Point2D(90f, 78f), label = "W3")) // Kitchen Window
        elements.add(CadElement("window", Point2D(30f, 10f), Point2D(36f, 10f), label = "W4")) // Bath Vent
        
        // --- STAIRS & BALCONIES (Extra elements) ---
        // Let's add a Balcony at the bottom right
        elements.add(CadElement("balcony", bounds = listOf(Point2D(65f, 90f), Point2D(85f, 90f), Point2D(85f, 96f), Point2D(65f, 96f)), label = "Balcony"))
        
        // --- ROOM LABELS (Center coordinates) ---
        elements.add(CadElement("text", Point2D(30f, 30f), Point2D(30f, 30f), label = "Master Bedroom"))
        elements.add(CadElement("text", Point2D(30f, 70f), Point2D(30f, 70f), label = "Bathroom"))
        elements.add(CadElement("text", Point2D(70f, 32f), Point2D(70f, 32f), label = "Living Room"))
        elements.add(CadElement("text", Point2D(70f, 75f), Point2D(70f, 75f), label = "Kitchen"))

        val stats = ProjectStats(
            projectName = "Modern 1BHK Apartment",
            floorCount = 1,
            flatCount = 1,
            roomCount = 4,
            bedroomCount = 1,
            bathroomCount = 1,
            livingCount = 1,
            diningCount = 0,
            kitchenCount = 1,
            balconyCount = 1,
            stairCount = 0,
            liftCount = 0,
            corridorCount = 0,
            wallLengthMeters = 48.5f,
            wallAreaSqMeters = 14.55f,
            builtUpAreaSqMeters = 72.0f,
            carpetAreaSqMeters = 61.2f,
            superBuiltUpAreaSqMeters = 86.4f,
            columnCount = 8,
            estimatedFlatSizeSqMeters = 72.0f
        )

        val rooms = listOf(
            RoomDetail("Living Room", "4.0m x 4.5m", 18.0f),
            RoomDetail("Master Bedroom", "4.0m x 4.0m", 16.0f),
            RoomDetail("Kitchen", "4.0m x 3.5m", 14.0f),
            RoomDetail("Bathroom", "2.0m x 4.0m", 8.0f),
            RoomDetail("Balcony", "2.0m x 0.6m", 1.2f)
        )

        val openings = listOf(
            OpeningDetail("Door (MD)", "1.0m x 2.1m", 1),
            OpeningDetail("Door (D1-D3)", "0.9m x 2.1m", 3),
            OpeningDetail("Window (W1-W3)", "1.5m x 1.2m", 3),
            OpeningDetail("Window (W4)", "0.6m x 0.6m", 1)
        )

        val errors = listOf(
            DrawingError("room_mismatch", "The Kitchen lacks direct ventilation output on the specified boundary.", "medium", 70f, 75f),
            DrawingError("missing_door", "Potential missing entrance transition or door threshold at Balcony edge.", "low", 75f, 90f)
        )

        val boq = listOf(
            BoqItem("Concrete (M25 Grade)", "Structure", 18.5f, "m3", 110f, 2035f),
            BoqItem("TMT Steel Rebars", "Structure", 1450f, "kg", 1.2f, 1740f),
            BoqItem("Red Clay Bricks", "Masonry", 4200f, "units", 0.15f, 630f),
            BoqItem("Cement Plastering", "Masonry", 310f, "m2", 4.5f, 1395f),
            BoqItem("Acrylic Emulsion Paint", "Finishing", 120f, "liters", 8.0f, 960f),
            BoqItem("Vitrified Floor Tiles", "Finishing", 65f, "m2", 22.0f, 1430f),
            BoqItem("Teakwood Main Door", "Fixtures", 1f, "units", 350f, 350f),
            BoqItem("UPVC Windows", "Fixtures", 4f, "units", 120f, 480f)
        )

        val suggestions = listOf(
            AiSuggestion("Efficiency", "Rearrange kitchen counters into an L-shape to improve work triangle efficiency.", "Saves 20% traversal distance during meal prep."),
            AiSuggestion("Lighting", "Increase Master Bedroom window width to 1.8m for better natural morning lighting.", "Enhances diurnal rhythm sync and reduces electrical usage."),
            AiSuggestion("Safety", "Relocate structural column at Bathroom partition 10cm west to keep standard wall thickness.", "Ensures consistent seismic loading across the central hallway.")
        )

        FloorPlanAnalysis(stats, rooms, openings, elements, errors, boq, suggestions)
    }

    val sample2: FloorPlanAnalysis by lazy {
        val elements = mutableListOf<CadElement>()
        
        // Villa Boundaries (Large structure 100x100)
        elements.add(CadElement("wall", Point2D(5f, 5f), Point2D(95f, 5f)))
        elements.add(CadElement("wall", Point2D(95f, 5f), Point2D(95f, 95f)))
        elements.add(CadElement("wall", Point2D(95f, 95f), Point2D(5f, 95f)))
        elements.add(CadElement("wall", Point2D(5f, 95f), Point2D(5f, 5f)))
        
        // Inner divisions for a duplex villa
        elements.add(CadElement("wall", Point2D(5f, 45f), Point2D(95f, 45f))) // Horizontal split
        elements.add(CadElement("wall", Point2D(50f, 5f), Point2D(50f, 95f))) // Vertical split
        
        // Rooms
        elements.add(CadElement("text", Point2D(27f, 25f), Point2D(27f, 25f), label = "Grand Living"))
        elements.add(CadElement("text", Point2D(72f, 25f), Point2D(72f, 25f), label = "Dining & Kitchen"))
        elements.add(CadElement("text", Point2D(27f, 70f), Point2D(27f, 70f), label = "Suite Bedroom"))
        elements.add(CadElement("text", Point2D(72f, 70f), Point2D(72f, 70f), label = "Media/Playroom"))

        // Add stairs
        elements.add(CadElement("stair", Point2D(47f, 35f), Point2D(53f, 55f), label = "U-Staircase"))

        // Doors
        elements.add(CadElement("door", Point2D(5f, 22f), Point2D(5f, 30f), label = "MD"))
        elements.add(CadElement("door", Point2D(50f, 20f), Point2D(50f, 26f), label = "D1"))
        elements.add(CadElement("door", Point2D(50f, 70f), Point2D(50f, 76f), label = "D2"))

        // Windows
        elements.add(CadElement("window", Point2D(25f, 5f), Point2D(35f, 5f), label = "W1"))
        elements.add(CadElement("window", Point2D(75f, 5f), Point2D(85f, 5f), label = "W2"))
        elements.add(CadElement("window", Point2D(25f, 95f), Point2D(35f, 95f), label = "W3"))
        elements.add(CadElement("window", Point2D(75f, 95f), Point2D(85f, 95f), label = "W4"))

        val stats = ProjectStats(
            projectName = "Luxury 2BHK Duplex Villa",
            floorCount = 2,
            flatCount = 1,
            roomCount = 6,
            bedroomCount = 2,
            bathroomCount = 3,
            livingCount = 1,
            diningCount = 1,
            kitchenCount = 1,
            balconyCount = 2,
            stairCount = 1,
            liftCount = 0,
            corridorCount = 1,
            wallLengthMeters = 112.0f,
            wallAreaSqMeters = 33.6f,
            builtUpAreaSqMeters = 180.0f,
            carpetAreaSqMeters = 153.0f,
            superBuiltUpAreaSqMeters = 216.0f,
            columnCount = 16,
            estimatedFlatSizeSqMeters = 180.0f
        )

        val rooms = listOf(
            RoomDetail("Grand Living Room", "6.0m x 8.0m", 48.0f),
            RoomDetail("Dining Room", "4.0m x 5.0m", 20.0f),
            RoomDetail("Suite Bedroom", "5.0m x 6.0m", 30.0f),
            RoomDetail("Media Room", "4.0m x 5.0m", 20.0f),
            RoomDetail("Kitchen", "4.0m x 3.5m", 14.0f),
            RoomDetail("Bathrooms (x3)", "2.0m x 3.0m each", 18.0f),
            RoomDetail("Balconies (x2)", "3.0m x 1.0m each", 6.0f)
        )

        val openings = listOf(
            OpeningDetail("French Entry Door", "1.8m x 2.4m", 1),
            OpeningDetail("Internal Flush Doors", "0.9m x 2.1m", 6),
            OpeningDetail("Double Glazed Windows", "1.8m x 1.5m", 5),
            OpeningDetail("Ventilator Slats", "0.6m x 0.6m", 3)
        )

        val errors = listOf(
            DrawingError("overlapping_walls", "Structural overlapping walls detected near staircase intersection.", "high", 50f, 45f)
        )

        val boq = listOf(
            BoqItem("Concrete (M30 High Grade)", "Structure", 38.0f, "m3", 130f, 4940f),
            BoqItem("Corrosion Resistant Steel", "Structure", 3100f, "kg", 1.4f, 4340f),
            BoqItem("Flyash Lightweight Blocks", "Masonry", 8500f, "units", 0.18f, 1530f),
            BoqItem("Italian Marble Flooring", "Finishing", 120f, "m2", 45.0f, 5400f),
            BoqItem("Structural Glass Railings", "Fixtures", 12f, "units", 150f, 1800f)
        )

        val suggestions = listOf(
            AiSuggestion("Safety", "Add cross-bracing steel near the grand stairs for enhanced seismic shear resistance.", "Significantly boosts the building stiffness."),
            AiSuggestion("Energy", "Incorporate Low-E glass coating on the French Entry Door to cut heat gain.", "Saves estimated 12% HVAC monthly electrical costs.")
        )

        FloorPlanAnalysis(stats, rooms, openings, elements, errors, boq, suggestions)
    }

    val sample3: FloorPlanAnalysis by lazy {
        val elements = mutableListOf<CadElement>()
        
        // Studio Boundaries (Compact 80x80)
        elements.add(CadElement("wall", Point2D(15f, 15f), Point2D(85f, 15f)))
        elements.add(CadElement("wall", Point2D(85f, 15f), Point2D(85f, 85f)))
        elements.add(CadElement("wall", Point2D(85f, 85f), Point2D(15f, 85f)))
        elements.add(CadElement("wall", Point2D(15f, 85f), Point2D(15f, 15f)))
        
        // Inner divider
        elements.add(CadElement("wall", Point2D(15f, 50f), Point2D(55f, 50f)))
        elements.add(CadElement("wall", Point2D(55f, 15f), Point2D(55f, 50f)))
        
        // Rooms
        elements.add(CadElement("text", Point2D(35f, 32f), Point2D(35f, 32f), label = "Executive Office"))
        elements.add(CadElement("text", Point2D(70f, 45f), Point2D(70f, 45f), label = "Co-working Studio"))
        elements.add(CadElement("text", Point2D(35f, 68f), Point2D(35f, 68f), label = "Lounge & Reception"))

        // Doors
        elements.add(CadElement("door", Point2D(15f, 68f), Point2D(15f, 74f), label = "MD"))
        elements.add(CadElement("door", Point2D(35f, 50f), Point2D(41f, 50f), label = "D1"))
        elements.add(CadElement("door", Point2D(55f, 30f), Point2D(55f, 36f), label = "D2"))

        // Windows
        elements.add(CadElement("window", Point2D(50f, 15f), Point2D(65f, 15f), label = "W1"))
        elements.add(CadElement("window", Point2D(85f, 40f), Point2D(85f, 55f), label = "W2"))

        val stats = ProjectStats(
            projectName = "Compact Studio Office",
            floorCount = 1,
            flatCount = 1,
            roomCount = 3,
            bedroomCount = 0,
            bathroomCount = 1,
            livingCount = 0,
            diningCount = 0,
            kitchenCount = 0,
            balconyCount = 0,
            stairCount = 0,
            liftCount = 0,
            corridorCount = 1,
            wallLengthMeters = 38.0f,
            wallAreaSqMeters = 11.4f,
            builtUpAreaSqMeters = 49.0f,
            carpetAreaSqMeters = 42.0f,
            superBuiltUpAreaSqMeters = 58.8f,
            columnCount = 6,
            estimatedFlatSizeSqMeters = 49.0f
        )

        val rooms = listOf(
            RoomDetail("Executive Office", "3.0m x 3.5m", 10.5f),
            RoomDetail("Co-working Studio", "4.0m x 5.0m", 20.0f),
            RoomDetail("Lounge & Reception", "3.0m x 3.5m", 10.5f),
            RoomDetail("Restroom", "1.5m x 1.5m", 2.25f)
        )

        val openings = listOf(
            OpeningDetail("Glass Pivot Entrance", "1.2m x 2.4m", 1),
            OpeningDetail("Wood Flush Doors", "0.9m x 2.1m", 2),
            OpeningDetail("Acoustic Windows", "1.5m x 1.2m", 2)
        )

        val errors = listOf(
            DrawingError("unsupported_span", "Large clear horizontal wall span lacks intermediate pillar supports.", "medium", 70f, 85f)
        )

        val boq = listOf(
            BoqItem("Concrete Mix", "Structure", 12.0f, "m3", 110f, 1320f),
            BoqItem("Acoustic Drywall Board", "Masonry", 45f, "units", 25.0f, 1125f),
            BoqItem("Commercial Carpet Tile", "Finishing", 45f, "m2", 18.0f, 810f),
            BoqItem("Frameless Glass Pivot Door", "Fixtures", 1f, "units", 600f, 600f)
        )

        val suggestions = listOf(
            AiSuggestion("Efficiency", "Use acoustic glass partitions instead of solid drywalls to share ambient natural daylight.", "Increases felt spatial volume and improves illumination values.")
        )

        FloorPlanAnalysis(stats, rooms, openings, elements, errors, boq, suggestions)
    }
}
