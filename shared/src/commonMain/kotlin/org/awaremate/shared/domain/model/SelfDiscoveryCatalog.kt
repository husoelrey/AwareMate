package org.awaremate.shared.domain.model

object SelfDiscoveryCatalog {

    /**
     * Curated starter pool of 10 Self-Discovery prompts.
     * Guaranteed rules:
     * 1. ZERO fabricated or unsourced statistics (no "%X of people..." claims).
     * 2. ZERO comparison, test, or pass/fail framing (no "can you do this", no quiz feeling).
     * 3. 100% neutral, curious, observational tone inviting mindful awareness of subtle daily reflexes.
     */
    val starterPrompts: List<SelfDiscoveryPrompt> = listOf(
        SelfDiscoveryPrompt(
            id = "sd_pocket_reflex",
            category = "Pocket Reflex",
            question = "When reaching into your pocket or bag for your phone, what was your mind experiencing in the moments right before?",
            curiosityHint = "Notice whether there was an impulse to find specific information, or simply an urge to fill a quiet pause."
        ),
        SelfDiscoveryPrompt(
            id = "sd_waiting_spaces",
            category = "Waiting Spaces",
            question = "While waiting for an elevator, bus, or tea kettle, what textures and ambient sounds around you become noticeable when your hands stay still?",
            curiosityHint = "Notice how the physical room feels when attention is not anchored to a screen."
        ),
        SelfDiscoveryPrompt(
            id = "sd_thumb_autopilot",
            category = "Unconscious Navigation",
            question = "When you unlock your phone without a set purpose, which app icon does your thumb naturally move toward first?",
            curiosityHint = "Observe this muscle memory with curiosity, treating it as an interesting pattern rather than something to judge."
        ),
        SelfDiscoveryPrompt(
            id = "sd_body_rhythm",
            category = "Body Rhythm",
            question = "Next time you browse a social feed or email inbox, what subtle shifts occur in your breathing depth or shoulder tension?",
            curiosityHint = "Curiously observe whether your body naturally softens or tightens, without any expectation to change it."
        ),
        SelfDiscoveryPrompt(
            id = "sd_soundscape",
            category = "Soundscape",
            question = "When moving through your living space, what sounds are present in the room when no headphones or background media are playing?",
            curiosityHint = "Notice how your thoughts settle into the natural acoustic stillness of the room."
        ),
        SelfDiscoveryPrompt(
            id = "sd_memory_capture",
            category = "Memory & Savoring",
            question = "When a moment feels visually striking, does the impulse to document it arrive before or after soaking it in with your eyes?",
            curiosityHint = "Curiously explore what happens when you drink in a scene for a few quiet breaths before taking a picture."
        ),
        SelfDiscoveryPrompt(
            id = "sd_mealtime_presence",
            category = "Sensory Focus",
            question = "During a meal or warm beverage, how does the experience of flavor shift when no device is resting within view on the table?",
            curiosityHint = "Observe warmth, texture, and aroma with full sensory presence."
        ),
        SelfDiscoveryPrompt(
            id = "sd_digital_impression",
            category = "Digital Reflection",
            question = "Among the various posts, videos, or articles encountered today, which single idea or image left a genuine impression?",
            curiosityHint = "Notice how the mind naturally filters for depth while letting fleeting stimulation float away."
        ),
        SelfDiscoveryPrompt(
            id = "sd_nighttime_transition",
            category = "Nighttime Transitions",
            question = "As you get ready for sleep, what marks the transition between interacting with a screen and resting in darkness?",
            curiosityHint = "Observe the sensation of placing the device down and letting your eyes adjust to nighttime stillness."
        ),
        SelfDiscoveryPrompt(
            id = "sd_notification_pace",
            category = "Notification Response",
            question = "When an alert or vibration sounds, what physical sensation or thought arises before you decide whether to look?",
            curiosityHint = "Notice whether curiosity, urgency, or calm greets the notification sound."
        )
    )
}
