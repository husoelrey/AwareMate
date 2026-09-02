package org.awaremate.shared.domain.usecase.growth

import org.awaremate.shared.domain.model.Hobby
import org.awaremate.shared.domain.model.HobbyCategory
import org.awaremate.shared.domain.model.HobbyEnergyLevel

class GetPersonalizedHobbiesUseCase {

    /**
     * Filters and orders hobbies according to the user's current energy level (1-5),
     * optional preferred category, and whether hobbies are bookmarked.
     */
    operator fun invoke(
        allHobbies: List<Hobby>,
        currentEnergyLevel: Int? = null,
        preferredCategory: HobbyCategory? = null,
        limit: Int = 5
    ): List<Hobby> {
        if (allHobbies.isEmpty()) return emptyList()

        val matchingEnergy: HobbyEnergyLevel? = when (currentEnergyLevel) {
            1, 2 -> HobbyEnergyLevel.GENTLE
            3 -> HobbyEnergyLevel.MODERATE
            4, 5 -> HobbyEnergyLevel.ACTIVE
            else -> null
        }

        return allHobbies
            .sortedWith(
                compareByDescending<Hobby> { it.isBookmarked }
                    .thenByDescending { preferredCategory != null && it.category == preferredCategory }
                    .thenByDescending { matchingEnergy != null && it.energyLevel == matchingEnergy }
                    .thenBy { it.sessionsCompleted }
            )
            .take(limit)
    }
}
