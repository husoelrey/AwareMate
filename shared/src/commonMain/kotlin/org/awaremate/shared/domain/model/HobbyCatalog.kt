package org.awaremate.shared.domain.model

object HobbyCatalog {

    val defaultHobbies: List<Hobby> = listOf(
        // CREATIVE_ARTS
        Hobby(
            id = "hobby_watercolor_doodle",
            title = "Watercolor Doodling",
            category = HobbyCategory.CREATIVE_ARTS,
            description = "Explore fluid colors on paper without aiming for perfection. Let pigments blend naturally.",
            beginnerTip = "Start with just two colors and observe how wet paper absorbs the water softly.",
            estimatedDurationMinutes = 20,
            energyLevel = HobbyEnergyLevel.GENTLE,
            tags = listOf("Art", "Relaxing", "Color", "Quiet")
        ),
        Hobby(
            id = "hobby_clay_sculpting",
            title = "Air-Dry Clay Sculpting",
            category = HobbyCategory.CREATIVE_ARTS,
            description = "Shape small pinch pots, leaf dishes, or miniature plant companions using air-dry clay.",
            beginnerTip = "Keep a small bowl of water nearby to smooth out tiny surface cracks with your fingertip.",
            estimatedDurationMinutes = 35,
            energyLevel = HobbyEnergyLevel.MODERATE,
            tags = listOf("Tactile", "Craft", "Mindful", "Hands-on")
        ),
        Hobby(
            id = "hobby_calligraphy_letters",
            title = "Mindful Brush Lettering",
            category = HobbyCategory.CREATIVE_ARTS,
            description = "Trace deliberate strokes, breathing in on upward strokes and exhaling on thick downward strokes.",
            beginnerTip = "Hold the brush pen at a 45-degree angle to get clean contrast between light and bold lines.",
            estimatedDurationMinutes = 25,
            energyLevel = HobbyEnergyLevel.GENTLE,
            tags = listOf("Focus", "Patience", "Writing", "Art")
        ),

        // NATURE_OUTDOORS
        Hobby(
            id = "hobby_urban_nature_walk",
            title = "Sensory Nature Walk",
            category = HobbyCategory.NATURE_OUTDOORS,
            description = "Step outside with eyes attuned to green textures, birdsong, and the scent of earth after rain.",
            beginnerTip = "Leave headphones in your pocket and challenge yourself to identify 3 distinct bird or wind sounds.",
            estimatedDurationMinutes = 25,
            energyLevel = HobbyEnergyLevel.ACTIVE,
            tags = listOf("Outdoor", "Fresh Air", "Mindful", "Movement")
        ),
        Hobby(
            id = "hobby_pressed_leaf_botany",
            title = "Pressed Leaf & Flower Journal",
            category = HobbyCategory.NATURE_OUTDOORS,
            description = "Collect fallen leaves or wild petals and press them between heavy book pages for natural art.",
            beginnerTip = "Only collect fallen specimens and place parchment paper on both sides to prevent staining.",
            estimatedDurationMinutes = 30,
            energyLevel = HobbyEnergyLevel.GENTLE,
            tags = listOf("Nature", "Botany", "Memory", "Quiet")
        ),
        Hobby(
            id = "hobby_cycling_explore",
            title = "Scenic Neighborhood Cycling",
            category = HobbyCategory.NATURE_OUTDOORS,
            description = "Take a peaceful bike ride down quiet streets or park paths without tracking speed or distance.",
            beginnerTip = "Focus on the cool breeze on your face and take a new turn you've never taken before.",
            estimatedDurationMinutes = 40,
            energyLevel = HobbyEnergyLevel.ACTIVE,
            tags = listOf("Fitness", "Exploration", "Wind", "Active")
        ),

        // MINDFUL_LIFESTYLE
        Hobby(
            id = "hobby_tea_ceremony",
            title = "Herbal Tea Brewing Ritual",
            category = HobbyCategory.MINDFUL_LIFESTYLE,
            description = "Slowly brew loose-leaf tea, observing the aroma, temperature, and gentle steam rising from the cup.",
            beginnerTip = "Take three intentional breaths while the tea steeps, savoring the fragrance before your first sip.",
            estimatedDurationMinutes = 15,
            energyLevel = HobbyEnergyLevel.GENTLE,
            tags = listOf("Warmth", "Grounding", "Senses", "Ritual")
        ),
        Hobby(
            id = "hobby_houseplant_care",
            title = "Indoor Plant Propagation & Care",
            category = HobbyCategory.MINDFUL_LIFESTYLE,
            description = "Inspect leaf health, gently wipe dust, water thirsty soil, and prune small cuttings for water rooting.",
            beginnerTip = "Test soil dryness with a finger up to your knuckle before watering; most plants prefer drying out.",
            estimatedDurationMinutes = 20,
            energyLevel = HobbyEnergyLevel.GENTLE,
            tags = listOf("Greenery", "Nurturing", "Calm", "Companion")
        ),
        Hobby(
            id = "hobby_baking_artisan_bread",
            title = "Simple Artisan Bread Kneading",
            category = HobbyCategory.MINDFUL_LIFESTYLE,
            description = "Mix flour, water, yeast, and salt. Feel the dough transform under the rhythm of your hands.",
            beginnerTip = "Don't rush the rise—dough develops its best flavors when rested patiently in a draft-free spot.",
            estimatedDurationMinutes = 45,
            energyLevel = HobbyEnergyLevel.MODERATE,
            tags = listOf("Kitchen", "Scent", "Patience", "Nourish")
        ),

        // HANDS_ON_CRAFT
        Hobby(
            id = "hobby_origami_geometry",
            title = "Geometric Origami Folding",
            category = HobbyCategory.HANDS_ON_CRAFT,
            description = "Transform flat paper into intricate birds, boxes, or geometric modular figures with crisp creases.",
            beginnerTip = "Use your fingernail or a ruler edge to press each fold sharply; accuracy creates beauty.",
            estimatedDurationMinutes = 20,
            energyLevel = HobbyEnergyLevel.GENTLE,
            tags = listOf("Paper", "Geometry", "Patience", "Focus")
        ),
        Hobby(
            id = "hobby_whittling_soap_wood",
            title = "Gentle Wood or Soap Carving",
            category = HobbyCategory.HANDS_ON_CRAFT,
            description = "Carve simple shapes (a bird, an animal, a leaf) out of soft wood or a plain bar of soap.",
            beginnerTip = "Always carve outward away from your fingers, shaving small, paper-thin curls at a time.",
            estimatedDurationMinutes = 30,
            energyLevel = HobbyEnergyLevel.MODERATE,
            tags = listOf("Woodcraft", "Tactile", "Focus", "Skill")
        ),
        Hobby(
            id = "hobby_knitting_crochet_basics",
            title = "Beginner Knitting or Crochet",
            category = HobbyCategory.HANDS_ON_CRAFT,
            description = "Learn the meditative, rhythmic motion of loops and yarn to create a simple bookmark or coaster.",
            beginnerTip = "Keep your tension loose and relaxed. Tight yarn makes it harder to slide the needle or hook.",
            estimatedDurationMinutes = 35,
            energyLevel = HobbyEnergyLevel.MODERATE,
            tags = listOf("Yarn", "Rhythm", "Warmth", "Repetitive")
        ),

        // MUSIC_LITERATURE
        Hobby(
            id = "hobby_acoustic_instrument",
            title = "Acoustic Ukulele or Guitar Strumming",
            category = HobbyCategory.MUSIC_LITERATURE,
            description = "Explore three open chords without pressure to master songs. Just feel the physical vibration of strings.",
            beginnerTip = "Start with the C and F chords on a ukulele—they are gentle on fingertips and sound warm instantly.",
            estimatedDurationMinutes = 25,
            energyLevel = HobbyEnergyLevel.MODERATE,
            tags = listOf("Sound", "Vibration", "Melody", "Creativity")
        ),
        Hobby(
            id = "hobby_creative_micro_fiction",
            title = "Pen-and-Paper Micro Fiction",
            category = HobbyCategory.MUSIC_LITERATURE,
            description = "Write a complete 100-word story about an everyday object or an encounter in a physical notebook.",
            beginnerTip = "Don't edit while you write. Let the ink flow freely and see where the characters lead you.",
            estimatedDurationMinutes = 20,
            energyLevel = HobbyEnergyLevel.GENTLE,
            tags = listOf("Writing", "Imagination", "Paper", "Story")
        ),
        Hobby(
            id = "hobby_audiobook_jigsaw_puzzle",
            title = "Physical Jigsaw Puzzle Flow",
            category = HobbyCategory.MUSIC_LITERATURE,
            description = "Sort edge pieces and discover connections at an unhurried, peaceful tempo on a quiet surface.",
            beginnerTip = "Sort pieces by distinct color gradients or textures into small bowls before placing.",
            estimatedDurationMinutes = 30,
            energyLevel = HobbyEnergyLevel.GENTLE,
            tags = listOf("Spatial", "Quiet", "Satisfaction", "Pattern")
        )
    )
}
