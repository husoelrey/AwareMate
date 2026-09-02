package org.awaremate.shared.presentation.companion

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.awaremate.shared.domain.model.Companion
import org.awaremate.shared.domain.model.CompanionCategory
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionEvent
import org.awaremate.shared.domain.usecase.companion.AddExperienceUseCase
import org.awaremate.shared.domain.usecase.companion.CalculateGrowthStageUseCase
import org.awaremate.shared.domain.usecase.companion.GetCompanionUseCase
import org.awaremate.shared.domain.usecase.companion.SaveCompanionUseCase
import org.awaremate.shared.domain.usecase.companion.UpdateCompanionEmotionUseCase

class CompanionScreenModel(
    private val getCompanionUseCase: GetCompanionUseCase,
    private val calculateGrowthStageUseCase: CalculateGrowthStageUseCase,
    private val addExperienceUseCase: AddExperienceUseCase,
    private val updateCompanionEmotionUseCase: UpdateCompanionEmotionUseCase,
    private val saveCompanionUseCase: SaveCompanionUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(CompanionState(isLoading = true))
    val state: StateFlow<CompanionState> = _state.asStateFlow()

    init {
        observeCompanion()
    }

    private fun observeCompanion() {
        screenModelScope.launch {
            getCompanionUseCase().collect { companionOrNull ->
                val comp = companionOrNull ?: Companion()
                val metrics = calculateGrowthStageUseCase(comp)
                _state.update {
                    it.copy(
                        companion = comp,
                        growthMetrics = metrics,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun handleIntent(intent: CompanionIntent) {
        when (intent) {
            CompanionIntent.LoadCompanion -> observeCompanion()

            is CompanionIntent.WaterCompanion -> {
                screenModelScope.launch {
                    val result = addExperienceUseCase(
                        amount = 10,
                        category = intent.category
                    )
                    if (result.isSuccess) {
                        updateCompanionEmotionUseCase(CompanionEvent.BreathExerciseCompleted)
                        val name = _state.value.companion.name
                        _state.update {
                            it.copy(interactionMessage = "You watered $name! $name feels happy and energized (+10 XP) 🌱✨")
                        }
                    }
                }
            }

            is CompanionIntent.SetEmotion -> {
                screenModelScope.launch {
                    val updated = _state.value.companion.copy(emotion = intent.emotion)
                    saveCompanionUseCase(updated)
                }
            }

            is CompanionIntent.RenameCompanion -> {
                screenModelScope.launch {
                    val updated = _state.value.companion.copy(name = intent.newName)
                    saveCompanionUseCase(updated)
                }
            }

            CompanionIntent.ClearInteractionMessage -> {
                _state.update { it.copy(interactionMessage = null) }
            }
        }
    }
}
