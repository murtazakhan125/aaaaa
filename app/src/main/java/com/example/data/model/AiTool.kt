package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

data class AiTool(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val placeholderPrompt: String,
    val systemInstruction: String,
    val defaultPromptPrefix: String
)

object AiTools {
    val ALL = listOf(
        AiTool(
            id = "vision",
            title = "Vision & Image Inspector",
            description = "Analyze photos, diagrams, math, handwriting, and screenshots",
            icon = Icons.Default.ImageSearch,
            placeholderPrompt = "Attach an image and ask what to explain, extract, or solve...",
            systemInstruction = "You are a multimodal image analyst. Thoroughly analyze uploaded images. Extract any text (OCR), explain diagram components, solve visible math problems, describe objects in detail, and answer user questions accurately.",
            defaultPromptPrefix = "Please analyze this image and detail everything visible, key insights, and answer: "
        ),
        AiTool(
            id = "code_fixer",
            title = "Code Debugger & Explainer",
            description = "Find bugs, optimize complexity, and explain code step-by-step",
            icon = Icons.Default.Code,
            placeholderPrompt = "Paste your code snippet or error message here...",
            systemInstruction = "You are a code debugging and optimization specialist. Analyze code for syntax, logic, security, and performance issues. Provide fixed code in cleanly formatted blocks and explain the root cause clearly.",
            defaultPromptPrefix = "Please analyze and fix this code, explain any bugs, and suggest improvements:\n\n"
        ),
        AiTool(
            id = "translator",
            title = "Universal Translator",
            description = "Translate between English, Urdu, Roman Urdu, Arabic, and more",
            icon = Icons.Default.Translate,
            placeholderPrompt = "Enter text to translate and specify target language...",
            systemInstruction = "You are a master polyglot translator. Provide natural, idiomatically accurate translations. When translating to Urdu, provide both Urdu script and Roman Urdu transliteration if helpful.",
            defaultPromptPrefix = "Please translate the following text with nuance, accuracy, and natural tone:\n\n"
        ),
        AiTool(
            id = "summarizer",
            title = "Smart Summarizer",
            description = "Condense long articles, reports, or transcripts into key points",
            icon = Icons.Default.Description,
            placeholderPrompt = "Paste long article, email, or meeting notes here...",
            systemInstruction = "You are an executive summarizer. Produce an executive summary followed by crisp bullet points highlighting actionable insights and key takeaways.",
            defaultPromptPrefix = "Summarize the following text with an executive overview and key bullet points:\n\n"
        ),
        AiTool(
            id = "email_writer",
            title = "Professional Email Crafter",
            description = "Draft polished corporate, freelance, or casual communications",
            icon = Icons.Default.Mail,
            placeholderPrompt = "Describe the email goal, recipient, and tone...",
            systemInstruction = "You are an executive communication specialist. Draft persuasive, courteous, and crisp emails with a strong subject line.",
            defaultPromptPrefix = "Draft a professional email based on these details:\n\n"
        ),
        AiTool(
            id = "text_enhancer",
            title = "Tone & Grammar Polisher",
            description = "Refine drafts into eloquent, engaging, and error-free copy",
            icon = Icons.Default.AutoFixHigh,
            placeholderPrompt = "Paste your rough draft here to polish...",
            systemInstruction = "You are an expert copy editor. Rewrite the user's text for maximum clarity, eloquence, and grammatical perfection without losing the original voice.",
            defaultPromptPrefix = "Polish and elevate this writing for clarity, flow, and elegance:\n\n"
        )
    )
}
