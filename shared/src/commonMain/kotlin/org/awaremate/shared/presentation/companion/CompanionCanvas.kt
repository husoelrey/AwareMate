package org.awaremate.shared.presentation.companion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.awaremate.shared.domain.model.CompanionEmotion
import org.awaremate.shared.domain.model.CompanionStage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure Compose Canvas rendering engine for the AwareMate Companion.
 * Renders stages: SEED, SPROUT, BLOOM, TREE, ANCIENT_TREE.
 * Renders emotions: PEACEFUL, CURIOUS, CHEERFUL, TIRED, RESTING.
 * Includes gentle breathing, leaf sway, ambient particles, and interactive bounce.
 */
@Composable
fun CompanionCanvas(
    stage: CompanionStage,
    emotion: CompanionEmotion,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    // Tap bounce animation
    var bounceTrigger by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (bounceTrigger) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        finishedListener = { bounceTrigger = false },
        label = "BounceScale"
    )

    // Gentle continuous breathing and sway animations
    val infiniteTransition = rememberInfiniteTransition(label = "CompanionLivingAnimations")

    val breathingBreath by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (emotion == CompanionEmotion.TIRED) 4000 else 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathing"
    )

    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sway"
    )

    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Particles"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .semantics {
                contentDescription = "Interactive Companion Canvas: ${stage.name.lowercase().replace('_', ' ')} stage, feeling ${emotion.name.lowercase()}"
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                bounceTrigger = true
                onTap()
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val groundY = height * 0.78f

            scale(scaleX = bounceScale * breathingBreath, scaleY = bounceScale * breathingBreath, pivot = Offset(centerX, groundY)) {
                // 1. Draw glowing background aura
                drawAura(centerX, groundY * 0.65f, width * 0.42f, emotion)

                // 2. Draw soil mound / planter base
                drawSoilBase(centerX, groundY, width * 0.38f)

                // 3. Stage-specific plant body
                when (stage) {
                    CompanionStage.SEED -> drawSeedStage(centerX, groundY, swayAngle, emotion)
                    CompanionStage.SPROUT -> drawSproutStage(centerX, groundY, swayAngle, emotion)
                    CompanionStage.BLOOM -> drawBloomStage(centerX, groundY, swayAngle, emotion)
                    CompanionStage.TREE -> drawTreeStage(centerX, groundY, swayAngle, emotion)
                    CompanionStage.ANCIENT_TREE -> drawAncientTreeStage(centerX, groundY, swayAngle, emotion)
                }

                // 4. Ambient particles (pollen sparkles for cheerful, sleepy Zs for resting, floating fireflies for ancient tree)
                drawAmbientParticles(centerX, groundY, particlePhase, emotion, stage)
            }
        }
    }
}

private fun DrawScope.drawAura(centerX: Float, centerY: Float, radius: Float, emotion: CompanionEmotion) {
    val auraColor = when (emotion) {
        CompanionEmotion.CHEERFUL -> Color(0xFFFEF3C7) // Golden sunlight
        CompanionEmotion.PEACEFUL -> Color(0xFFD8F3DC) // Soft fresh mint
        CompanionEmotion.CURIOUS -> Color(0xFFE0F2FE)  // Sky blue
        CompanionEmotion.TIRED -> Color(0xFFF1F5F9)    // Calm twilight
        CompanionEmotion.RESTING -> Color(0xFFE2E8F0)  // Deep restful indigo/slate
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(auraColor.copy(alpha = 0.55f), auraColor.copy(alpha = 0.15f), Color.Transparent),
            center = Offset(centerX, centerY),
            radius = radius
        ),
        radius = radius,
        center = Offset(centerX, centerY)
    )
}

private fun DrawScope.drawSoilBase(centerX: Float, groundY: Float, moundWidth: Float) {
    // Earthen soil mound
    val soilPath = Path().apply {
        moveTo(centerX - moundWidth, groundY + 20f)
        quadraticTo(
            centerX, groundY - 14f,
            centerX + moundWidth, groundY + 20f
        )
        lineTo(centerX + moundWidth - 10f, groundY + 36f)
        quadraticTo(
            centerX, groundY + 28f,
            centerX - moundWidth + 10f, groundY + 36f
        )
        close()
    }

    drawPath(
        path = soilPath,
        color = Color(0xFF5D4037) // Rich warm earth brown
    )

    // Soil texture accents
    drawCircle(
        color = Color(0xFF795548),
        radius = 4f,
        center = Offset(centerX - 35f, groundY + 12f)
    )
    drawCircle(
        color = Color(0xFF4E342E),
        radius = 5f,
        center = Offset(centerX + 25f, groundY + 15f)
    )
    drawCircle(
        color = Color(0xFF8D6E63),
        radius = 3f,
        center = Offset(centerX + 4f, groundY + 18f)
    )
}

private fun DrawScope.drawSeedStage(centerX: Float, groundY: Float, swayAngle: Float, emotion: CompanionEmotion) {
    val seedCenterY = groundY - 26f

    rotate(degrees = swayAngle * 0.5f, pivot = Offset(centerX, groundY)) {
        // Seed body
        val seedPath = Path().apply {
            moveTo(centerX, seedCenterY - 32f)
            cubicTo(
                centerX + 28f, seedCenterY - 15f,
                centerX + 28f, seedCenterY + 20f,
                centerX, seedCenterY + 26f
            )
            cubicTo(
                centerX - 28f, seedCenterY + 20f,
                centerX - 28f, seedCenterY - 15f,
                centerX, seedCenterY - 32f
            )
            close()
        }

        drawPath(
            path = seedPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037))
            )
        )

        // Seed sprout crack with glowing green tip
        val crackPath = Path().apply {
            moveTo(centerX - 3f, seedCenterY - 32f)
            lineTo(centerX + 2f, seedCenterY - 18f)
            lineTo(centerX - 1f, seedCenterY - 8f)
        }
        drawPath(
            path = crackPath,
            color = Color(0xFF74C69D),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // Emergent green leaf tip
        drawCircle(
            color = Color(0xFF52B788),
            radius = 6f,
            center = Offset(centerX, seedCenterY - 34f)
        )

        // Facial expression on seed
        drawCompanionFace(centerX, seedCenterY + 4f, scale = 0.8f, emotion = emotion)
    }
}

private fun DrawScope.drawSproutStage(centerX: Float, groundY: Float, swayAngle: Float, emotion: CompanionEmotion) {
    rotate(degrees = swayAngle, pivot = Offset(centerX, groundY)) {
        val stemTopY = groundY - 110f

        // Curved green stem
        val stemPath = Path().apply {
            moveTo(centerX, groundY)
            cubicTo(
                centerX - 8f, groundY - 45f,
                centerX + 8f, groundY - 80f,
                centerX, stemTopY
            )
        }
        drawPath(
            path = stemPath,
            color = Color(0xFF40916C),
            style = Stroke(width = 9f, cap = StrokeCap.Round)
        )

        // Left leaf
        val leftLeaf = Path().apply {
            moveTo(centerX - 2f, stemTopY + 18f)
            cubicTo(
                centerX - 42f, stemTopY + 5f,
                centerX - 50f, stemTopY - 28f,
                centerX - 12f, stemTopY - 16f
            )
            close()
        }
        drawPath(path = leftLeaf, color = Color(0xFF52B788))

        // Right leaf
        val rightLeaf = Path().apply {
            moveTo(centerX + 2f, stemTopY + 12f)
            cubicTo(
                centerX + 42f, stemTopY - 2f,
                centerX + 52f, stemTopY - 34f,
                centerX + 14f, stemTopY - 22f
            )
            close()
        }
        drawPath(path = rightLeaf, color = Color(0xFF74C69D))

        // Center head bud
        drawCircle(
            color = Color(0xFF40916C),
            radius = 16f,
            center = Offset(centerX, stemTopY)
        )

        // Facial expression
        drawCompanionFace(centerX, stemTopY, scale = 0.9f, emotion = emotion)
    }
}

private fun DrawScope.drawBloomStage(centerX: Float, groundY: Float, swayAngle: Float, emotion: CompanionEmotion) {
    rotate(degrees = swayAngle * 0.8f, pivot = Offset(centerX, groundY)) {
        val bloomCenterY = groundY - 135f

        // Sturdy stem
        val stemPath = Path().apply {
            moveTo(centerX, groundY)
            cubicTo(
                centerX - 6f, groundY - 50f,
                centerX + 6f, groundY - 95f,
                centerX, bloomCenterY
            )
        }
        drawPath(
            path = stemPath,
            color = Color(0xFF2D6A4F),
            style = Stroke(width = 11f, cap = StrokeCap.Round)
        )

        // Lower leaves
        val leaf1 = Path().apply {
            moveTo(centerX - 4f, groundY - 50f)
            quadraticTo(centerX - 52f, groundY - 65f, centerX - 18f, groundY - 80f)
            close()
        }
        drawPath(leaf1, Color(0xFF40916C))

        val leaf2 = Path().apply {
            moveTo(centerX + 4f, groundY - 45f)
            quadraticTo(centerX + 54f, groundY - 58f, centerX + 18f, groundY - 76f)
            close()
        }
        drawPath(leaf2, Color(0xFF52B788))

        // Flower petals (5 coral / warm peach petals radiating in a circle)
        val petalCount = 5
        val petalDistance = 34f
        val petalRadius = 22f
        for (i in 0 until petalCount) {
            val angleRad = (i * 2 * PI / petalCount) - PI / 2.0
            val px = centerX + (petalDistance * cos(angleRad)).toFloat()
            val py = bloomCenterY + (petalDistance * sin(angleRad)).toFloat()
            drawCircle(
                color = Color(0xFFF4A261), // Warm Peach Blossom
                radius = petalRadius,
                center = Offset(px, py)
            )
        }

        // Flower core center disk
        drawCircle(
            color = Color(0xFFFEF08A), // Warm Sunflower Center
            radius = 26f,
            center = Offset(centerX, bloomCenterY)
        )

        // Face in center
        drawCompanionFace(centerX, bloomCenterY, scale = 1.0f, emotion = emotion)
    }
}

private fun DrawScope.drawTreeStage(centerX: Float, groundY: Float, swayAngle: Float, emotion: CompanionEmotion) {
    val treeCenterY = groundY - 145f

    // Tree Trunk
    val trunkPath = Path().apply {
        moveTo(centerX - 18f, groundY + 5f)
        lineTo(centerX - 10f, treeCenterY + 25f)
        lineTo(centerX + 10f, treeCenterY + 25f)
        lineTo(centerX + 18f, groundY + 5f)
        close()
    }
    drawPath(path = trunkPath, color = Color(0xFF6D4C41))

    rotate(degrees = swayAngle * 0.6f, pivot = Offset(centerX, treeCenterY + 20f)) {
        // Overlapping lush foliage canopy masses
        // Bottom canopy puffs
        drawCircle(
            color = Color(0xFF2D6A4F),
            radius = 48f,
            center = Offset(centerX - 42f, treeCenterY + 5f)
        )
        drawCircle(
            color = Color(0xFF2D6A4F),
            radius = 48f,
            center = Offset(centerX + 42f, treeCenterY + 5f)
        )

        // Middle canopy puffs
        drawCircle(
            color = Color(0xFF40916C),
            radius = 56f,
            center = Offset(centerX, treeCenterY - 20f)
        )
        drawCircle(
            color = Color(0xFF52B788),
            radius = 42f,
            center = Offset(centerX - 28f, treeCenterY - 34f)
        )
        drawCircle(
            color = Color(0xFF74C69D),
            radius = 38f,
            center = Offset(centerX + 26f, treeCenterY - 36f)
        )

        // Fruit accents
        drawCircle(Color(0xFFE76F51), radius = 6f, center = Offset(centerX - 35f, treeCenterY - 10f))
        drawCircle(Color(0xFFE76F51), radius = 6f, center = Offset(centerX + 38f, treeCenterY - 12f))
        drawCircle(Color(0xFFE76F51), radius = 5f, center = Offset(centerX + 5f, treeCenterY - 55f))

        // Tree Face in the center foliage
        drawCompanionFace(centerX, treeCenterY - 15f, scale = 1.15f, emotion = emotion)
    }
}

private fun DrawScope.drawAncientTreeStage(centerX: Float, groundY: Float, swayAngle: Float, emotion: CompanionEmotion) {
    val treeCenterY = groundY - 165f

    // Wide ancient trunk with root branches
    val trunkPath = Path().apply {
        moveTo(centerX - 32f, groundY + 12f)
        quadraticTo(centerX - 16f, groundY - 45f, centerX - 14f, treeCenterY + 30f)
        lineTo(centerX + 14f, treeCenterY + 30f)
        quadraticTo(centerX + 16f, groundY - 45f, centerX + 32f, groundY + 12f)
        close()
    }
    drawPath(path = trunkPath, color = Color(0xFF4E342E))

    // Root arches
    drawPath(
        path = Path().apply {
            moveTo(centerX - 30f, groundY + 10f)
            quadraticTo(centerX - 50f, groundY + 2f, centerX - 62f, groundY + 18f)
        },
        color = Color(0xFF4E342E),
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )
    drawPath(
        path = Path().apply {
            moveTo(centerX + 30f, groundY + 10f)
            quadraticTo(centerX + 50f, groundY + 2f, centerX + 62f, groundY + 18f)
        },
        color = Color(0xFF4E342E),
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )

    rotate(degrees = swayAngle * 0.4f, pivot = Offset(centerX, treeCenterY + 20f)) {
        // Majestic glowing canopy
        drawCircle(
            color = Color(0xFF1B4332),
            radius = 65f,
            center = Offset(centerX - 52f, treeCenterY + 10f)
        )
        drawCircle(
            color = Color(0xFF1B4332),
            radius = 65f,
            center = Offset(centerX + 52f, treeCenterY + 10f)
        )
        drawCircle(
            color = Color(0xFF2D6A4F),
            radius = 72f,
            center = Offset(centerX, treeCenterY - 15f)
        )
        drawCircle(
            color = Color(0xFF52B788),
            radius = 54f,
            center = Offset(centerX - 35f, treeCenterY - 42f)
        )
        drawCircle(
            color = Color(0xFF74C69D),
            radius = 52f,
            center = Offset(centerX + 35f, treeCenterY - 44f)
        )
        drawCircle(
            color = Color(0xFFD8F3DC),
            radius = 36f,
            center = Offset(centerX, treeCenterY - 60f)
        )

        // Ancient golden aura ring
        drawCircle(
            color = Color(0xFFFBBF24).copy(alpha = 0.4f),
            radius = 85f,
            center = Offset(centerX, treeCenterY - 15f),
            style = Stroke(width = 3f)
        )

        // Face in crown
        drawCompanionFace(centerX, treeCenterY - 12f, scale = 1.3f, emotion = emotion)
    }
}

/**
 * Draws the expressive, compassionate face of the companion.
 * Eyes and mouth dynamically reflect the current emotion.
 */
private fun DrawScope.drawCompanionFace(
    centerX: Float,
    centerY: Float,
    scale: Float,
    emotion: CompanionEmotion
) {
    val eyeSpacing = 16f * scale
    val eyeY = centerY - 3f * scale
    val eyeRadius = 3.5f * scale
    val featureColor = Color(0xFF1B1B1B)

    when (emotion) {
        CompanionEmotion.PEACEFUL -> {
            // Peaceful upward curved smiling arcs `⌒ ⌒`
            drawArc(
                color = featureColor,
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(centerX - eyeSpacing - 7f * scale, eyeY - 4f * scale),
                size = Size(14f * scale, 8f * scale),
                style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round)
            )
            drawArc(
                color = featureColor,
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(centerX + eyeSpacing - 7f * scale, eyeY - 4f * scale),
                size = Size(14f * scale, 8f * scale),
                style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round)
            )
            // Soft smile
            drawArc(
                color = featureColor,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(centerX - 6f * scale, eyeY + 6f * scale),
                size = Size(12f * scale, 7f * scale),
                style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round)
            )
        }

        CompanionEmotion.CHEERFUL -> {
            // Big sparkling open eyes `● ●` with white catchlights
            drawCircle(
                color = featureColor,
                radius = eyeRadius * 1.35f,
                center = Offset(centerX - eyeSpacing, eyeY)
            )
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.45f,
                center = Offset(centerX - eyeSpacing - 1.5f * scale, eyeY - 1.5f * scale)
            )

            drawCircle(
                color = featureColor,
                radius = eyeRadius * 1.35f,
                center = Offset(centerX + eyeSpacing, eyeY)
            )
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.45f,
                center = Offset(centerX + eyeSpacing - 1.5f * scale, eyeY - 1.5f * scale)
            )

            // Rosy blush cheeks
            drawCircle(
                color = Color(0xFFFF8A80).copy(alpha = 0.65f),
                radius = 5.5f * scale,
                center = Offset(centerX - eyeSpacing - 6f * scale, eyeY + 6f * scale)
            )
            drawCircle(
                color = Color(0xFFFF8A80).copy(alpha = 0.65f),
                radius = 5.5f * scale,
                center = Offset(centerX + eyeSpacing + 6f * scale, eyeY + 6f * scale)
            )

            // Wide joyous smile `◡`
            drawArc(
                color = featureColor,
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(centerX - 8f * scale, eyeY + 5f * scale),
                size = Size(16f * scale, 10f * scale),
                style = Stroke(width = 2.5f * scale, cap = StrokeCap.Round)
            )
        }

        CompanionEmotion.CURIOUS -> {
            // Slightly offset inquisitive eyes
            drawCircle(
                color = featureColor,
                radius = eyeRadius * 1.15f,
                center = Offset(centerX - eyeSpacing, eyeY)
            )
            drawCircle(
                color = featureColor,
                radius = eyeRadius * 1.35f,
                center = Offset(centerX + eyeSpacing, eyeY - 2f * scale)
            )
            // One raised eyebrow
            drawLine(
                color = featureColor,
                start = Offset(centerX + eyeSpacing - 5f * scale, eyeY - 7f * scale),
                end = Offset(centerX + eyeSpacing + 5f * scale, eyeY - 9f * scale),
                strokeWidth = 2f * scale,
                cap = StrokeCap.Round
            )
            // Little "o" mouth
            drawCircle(
                color = featureColor,
                radius = 2.5f * scale,
                center = Offset(centerX, eyeY + 9f * scale)
            )
        }

        CompanionEmotion.TIRED -> {
            // Downward curved droopy eyelids
            drawArc(
                color = featureColor,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(centerX - eyeSpacing - 6f * scale, eyeY - 2f * scale),
                size = Size(12f * scale, 6f * scale),
                style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round)
            )
            drawArc(
                color = featureColor,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(centerX + eyeSpacing - 6f * scale, eyeY - 2f * scale),
                size = Size(12f * scale, 6f * scale),
                style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round)
            )
            // Soft tiny resting mouth line
            drawLine(
                color = featureColor,
                start = Offset(centerX - 4f * scale, eyeY + 8f * scale),
                end = Offset(centerX + 4f * scale, eyeY + 8f * scale),
                strokeWidth = 2f * scale,
                cap = StrokeCap.Round
            )
        }

        CompanionEmotion.RESTING -> {
            // Sleeping straight calm dashes `— —`
            drawLine(
                color = featureColor,
                start = Offset(centerX - eyeSpacing - 5f * scale, eyeY),
                end = Offset(centerX - eyeSpacing + 5f * scale, eyeY),
                strokeWidth = 2.4f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = featureColor,
                start = Offset(centerX + eyeSpacing - 5f * scale, eyeY),
                end = Offset(centerX + eyeSpacing + 5f * scale, eyeY),
                strokeWidth = 2.4f * scale,
                cap = StrokeCap.Round
            )
            // Gentle tiny sleeping curve
            drawArc(
                color = featureColor,
                startAngle = 30f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(centerX - 4f * scale, eyeY + 6f * scale),
                size = Size(8f * scale, 4f * scale),
                style = Stroke(width = 2f * scale, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Draws floating ambient particles suited for the emotion/stage.
 */
private fun DrawScope.drawAmbientParticles(
    centerX: Float,
    groundY: Float,
    phase: Float,
    emotion: CompanionEmotion,
    stage: CompanionStage
) {
    when (emotion) {
        CompanionEmotion.CHEERFUL -> {
            // Cheerful floating sparkle stars / pollen
            val offsets = listOf(
                Offset(centerX - 70f, groundY - 140f - phase * 50f),
                Offset(centerX + 65f, groundY - 120f - ((phase + 0.5f) % 1f) * 60f),
                Offset(centerX - 35f, groundY - 170f - ((phase + 0.25f) % 1f) * 45f),
                Offset(centerX + 40f, groundY - 180f - ((phase + 0.75f) % 1f) * 50f)
            )
            offsets.forEach { pos ->
                drawCircle(
                    color = Color(0xFFFBBF24).copy(alpha = (1f - (pos.y % 40f) / 40f).coerceIn(0.2f, 0.8f)),
                    radius = 3.5f,
                    center = pos
                )
            }
        }

        CompanionEmotion.RESTING -> {
            // Soft floating sleepy Z particles
            val zY = groundY - 110f - phase * 55f
            val zX = centerX + 45f + sin(phase * PI.toFloat() * 2) * 8f
            val alpha = (1f - phase).coerceIn(0f, 1f)

            // Draw little Z shape
            val zPath = Path().apply {
                moveTo(zX - 5f, zY - 5f)
                lineTo(zX + 5f, zY - 5f)
                lineTo(zX - 5f, zY + 5f)
                lineTo(zX + 5f, zY + 5f)
            }
            drawPath(
                path = zPath,
                color = Color(0xFF64748B).copy(alpha = alpha),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }

        else -> {
            if (stage == CompanionStage.ANCIENT_TREE) {
                // Mystical ambient golden fireflies
                val fireflies = listOf(
                    Offset(centerX - 80f + sin(phase * 4f) * 15f, groundY - 80f - phase * 70f),
                    Offset(centerX + 85f - cos(phase * 4f) * 15f, groundY - 100f - ((phase + 0.5f) % 1f) * 80f),
                    Offset(centerX - 25f + sin(phase * 3f) * 10f, groundY - 190f - ((phase + 0.3f) % 1f) * 40f)
                )
                fireflies.forEach { pt ->
                    drawCircle(
                        color = Color(0xFFFEF08A).copy(alpha = 0.7f),
                        radius = 4f,
                        center = pt
                    )
                }
            }
        }
    }
}
