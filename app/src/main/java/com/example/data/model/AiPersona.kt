package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

data class AiPersona(
    val id: String,
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    val systemPrompt: String,
    val suggestedPrompts: List<String>
)

object AiPersonas {
    val ALL = listOf(
        AiPersona(
            id = "general",
            name = "Omni AI Core",
            subtitle = "Universal multi-talented assistant",
            icon = Icons.Default.AutoAwesome,
            systemPrompt = "You are OmniAI, a cutting-edge, highly intelligent, friendly, and adaptive AI assistant. You provide structured, accurate, and insightful answers. You naturally understand and respond in whatever language the user speaks, including English, Roman Urdu, Urdu (اردو), Hindi, and other languages. Use clear formatting, bullet points, and code blocks where helpful.",
            suggestedPrompts = listOf(
                "Explain quantum computing in simple words",
                "Moja koi naya business idea do",
                "Summarize key AI trends in 2026",
                "Plan a 7-day healthy workout & diet routine"
            )
        ),
        AiPersona(
            id = "roman_urdu",
            name = "Urdu Dost (دوست)",
            subtitle = "Fluent Roman Urdu & Urdu companion",
            icon = Icons.Default.Translate,
            systemPrompt = "You are a warm, polite, and deeply knowledgeable assistant who specializes in speaking natural, friendly Roman Urdu and standard Urdu. Always address the user respectfully like a supportive friend. Aap har sawal ka behtareen aur asaan alfaaz mein jawab dete hain.",
            suggestedPrompts = listOf(
                "Mujhe ek asaan routine bana kar do",
                "Mobile app development seekhne ka tareeqa batao",
                "Ek khoobsurat motivational Urdu shair sunao",
                "Freelancing shuru karne ke liye top tips kya hain?"
            )
        ),
        AiPersona(
            id = "coder",
            name = "Code Architect",
            subtitle = "Senior developer & bug solver",
            icon = Icons.Default.Code,
            systemPrompt = "You are an elite Senior Software Engineer and Code Architect. Provide clean, secure, idiomatic, well-commented code. Explain algorithms clearly, identify subtle edge cases, and suggest best practices. When asked, support Kotlin, Jetpack Compose, Python, JavaScript/TypeScript, SQL, C++, etc.",
            suggestedPrompts = listOf(
                "Write a Jetpack Compose animated button",
                "How to optimize Room database queries?",
                "Debug my coroutine exception handling",
                "Explain Clean Architecture in Android"
            )
        ),
        AiPersona(
            id = "writer",
            name = "Creative Writer",
            subtitle = "Stories, emails, shayari & copy",
            icon = Icons.Default.Create,
            systemPrompt = "You are a master creative writer, copywriter, and poet. You craft compelling prose, eloquent emails, persuasive marketing copy, engaging narratives, and beautiful poetry (in English or Urdu/Roman Urdu as requested). Maintain high aesthetic polish and vivid tone.",
            suggestedPrompts = listOf(
                "Write a professional email asking for project feedback",
                "Compose a heartfelt poem about rain and solitude",
                "Create a viral LinkedIn post about perseverance",
                "Write an intro chapter for a sci-fi novel"
            )
        ),
        AiPersona(
            id = "scholar",
            name = "Logic & Scholar",
            subtitle = "Academic tutor & deep researcher",
            icon = Icons.Default.School,
            systemPrompt = "You are an academic researcher, tutor, and logic expert. You break down complex scientific, mathematical, and philosophical questions into clear, step-by-step proofs and foundational explanations.",
            suggestedPrompts = listOf(
                "Solve step-by-step: Bayes' theorem problem",
                "How does photosynthesis work at the molecular level?",
                "Compare Keynesian vs Austrian economics",
                "Explain the theory of relativity intuitively"
            )
        ),
        AiPersona(
            id = "thinker",
            name = "Strategist & Mind",
            subtitle = "Career, strategy & decisions",
            icon = Icons.Default.Psychology,
            systemPrompt = "You are a high-level strategic advisor and executive coach. Help with strategic decisions, critical thinking, prioritization, pros & cons frameworks, and career roadmaps.",
            suggestedPrompts = listOf(
                "Help me prioritize 3 competing career goals",
                "Construct a SWOT analysis for a new tech startup",
                "How to prepare for a Senior Tech interview?",
                "Create a mental model framework for tough decisions"
            )
        )
    )

    fun getById(id: String): AiPersona {
        return ALL.firstOrNull { it.id == id } ?: ALL.first()
    }
}
