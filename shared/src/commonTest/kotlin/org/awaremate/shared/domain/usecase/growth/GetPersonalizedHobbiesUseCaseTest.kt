package org.awaremate.shared.domain.usecase.growth

import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCategory
import org.awaremate.shared.domain.model.HobbyEnergyLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPersonalizedHobbiesUseCaseTest {

    private val useCase = GetPersonalizedHobbiesUseCase()

    private val sampleHobbies = listOf(
        Hobby(
            id = "h1",
            title = "Watercolor",
            category = HobbyCategory.CREATIVE_ARTS,
            description = "Painting",
            beginnerTip = "Tip",
            energyLevel = HobbyEnergyLevel.GENTLE,
            isBookmarked = false
        ),
        Hobby(
            id = "h2",
            title = "Cycling",
            category = HobbyCategory.NATURE_OUTDOORS,
            description = "Biking",
            beginnerTip = "Tip",
            energyLevel = HobbyEnergyLevel.ACTIVE,
            isBookmarked = false
        ),
        Hobby(
            id = "h3",
            title = "Tea Brewing",
            category = HobbyCategory.MINDFUL_LIFESTYLE,
            description = "Tea",
            beginnerTip = "Tip",
            energyLevel = HobbyEnergyLevel.GENTLE,
            isBookmarked = true // Bookmarked
        ),
        Hobby(
            id = "h4",
            title = "Bread Kneading",
            category = HobbyCategory.MINDFUL_LIFESTYLE,
            description = "Baking",
            beginnerTip = "Tip",
            energyLevel = HobbyEnergyLevel.MODERATE,
            isBookmarked = false
        )
    )

    @Test
    fun testBookmarkedHobbiesRankFirst() {
        val results = useCase(
            allHobbies = sampleHobbies,
            currentEnergyLevel = 5,
            limit = 4
        )

        // Bookmarked hobby should come first regardless of energy match
        assertEquals("h3", results.first().id)
        assertTrue(results.first().isBookmarked)
    }

    @Test
    fun testLowEnergyRecommendsGentleHobbies() {
        val results = useCase(
            allHobbies = sampleHobbies.map { it.copy(isBookmarked = false) },
            currentEnergyLevel = 1, // Low energy
            limit = 2
        )

        // Should prioritize gentle hobbies (Watercolor or Tea)
        assertEquals(HobbyEnergyLevel.GENTLE, results.first().energyLevel)
    }

    @Test
    fun testHighEnergyRecommendsActiveHobbies() {
        val results = useCase(
            allHobbies = sampleHobbies.map { it.copy(isBookmarked = false) },
            currentEnergyLevel = 5, // High energy
            limit = 2
        )

        assertEquals("h2", results.first().id)
        assertEquals(HobbyEnergyLevel.ACTIVE, results.first().energyLevel)
    }
}
