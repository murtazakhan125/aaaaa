package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.local.ChatDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSession
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiInlineData
import com.example.data.model.GeminiPart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class AiRepository(
    private val chatDao: ChatDao,
    val geminiClient: GeminiClient
) {
    fun getAllSessions(): Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(title: String = "New Conversation", personaId: String = "general"): ChatSession {
        val newSession = ChatSession(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            personaId = personaId
        )
        chatDao.insertSession(newSession)
        return newSession
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) {
        val existing = chatDao.getSessionById(sessionId) ?: return
        chatDao.updateSession(existing.copy(title = title, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearAllData() {
        chatDao.clearAllMessages()
        chatDao.clearAllSessions()
    }

    suspend fun saveUserMessage(
        sessionId: String,
        text: String,
        imageBase64: String? = null
    ): Long {
        val message = ChatMessageEntity(
            sessionId = sessionId,
            sender = "user",
            text = text,
            imageBase64 = imageBase64,
            timestamp = System.currentTimeMillis()
        )
        val id = chatDao.insertMessage(message)

        // Update session timestamp and title if it's the first message
        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            val title = if (session.title == "New Conversation") {
                val clean = text.take(36).trim()
                if (clean.isNotEmpty()) clean else session.title
            } else {
                session.title
            }
            chatDao.updateSession(
                session.copy(
                    title = title,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return id
    }

    suspend fun saveAssistantMessage(
        sessionId: String,
        text: String,
        isError: Boolean = false
    ): Long {
        val message = ChatMessageEntity(
            sessionId = sessionId,
            sender = "model",
            text = text,
            timestamp = System.currentTimeMillis(),
            isError = isError
        )
        val id = chatDao.insertMessage(message)
        val session = chatDao.getSessionById(sessionId)
        if (session != null) {
            chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
        }
        return id
    }

    suspend fun buildGeminiContents(sessionId: String, newPrompt: String, newImageBase64: String?): List<GeminiContent> {
        val history = chatDao.getMessagesListForSession(sessionId)
        val contents = mutableListOf<GeminiContent>()

        // Take up to 10 latest messages for context window management
        val recentHistory = history.takeLast(10)
        for (msg in recentHistory) {
            val role = if (msg.sender == "user") "user" else "model"
            val parts = mutableListOf<GeminiPart>()
            if (!msg.imageBase64.isNullOrEmpty()) {
                parts.add(GeminiPart(inlineData = GeminiInlineData("image/jpeg", msg.imageBase64)))
            }
            if (msg.text.isNotEmpty()) {
                parts.add(GeminiPart(text = msg.text))
            }
            if (parts.isNotEmpty()) {
                contents.add(GeminiContent(role = role, parts = parts))
            }
        }

        // Add the current prompt if not already in history
        val currentParts = mutableListOf<GeminiPart>()
        if (!newImageBase64.isNullOrEmpty()) {
            currentParts.add(GeminiPart(inlineData = GeminiInlineData("image/jpeg", newImageBase64)))
        }
        currentParts.add(GeminiPart(text = newPrompt))
        contents.add(GeminiContent(role = "user", parts = currentParts))

        return contents
    }

    suspend fun executeDirectToolPrompt(
        prompt: String,
        imageBase64: String?,
        systemInstruction: String,
        model: String = GeminiClient.DEFAULT_MODEL
    ): Result<String> {
        val parts = mutableListOf<GeminiPart>()
        if (!imageBase64.isNullOrEmpty()) {
            parts.add(GeminiPart(inlineData = GeminiInlineData("image/jpeg", imageBase64)))
        }
        parts.add(GeminiPart(text = prompt))
        val contents = listOf(GeminiContent(role = "user", parts = parts))

        return geminiClient.generateContent(
            contents = contents,
            systemInstruction = systemInstruction,
            model = model
        )
    }
}
