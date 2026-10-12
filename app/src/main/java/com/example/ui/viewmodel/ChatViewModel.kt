package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.local.AppDatabase
import com.example.data.model.AiPersona
import com.example.data.model.AiPersonas
import com.example.data.model.AiTool
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSession
import com.example.data.repository.AiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("omni_ai_prefs", Context.MODE_PRIVATE)

    private val database = AppDatabase.getDatabase(application)
    private val geminiClient = GeminiClient(prefs.getString("custom_api_key", null))
    val repository = AiRepository(database.chatDao(), geminiClient)

    val sessions: StateFlow<List<ChatSession>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    private val _streamingBuffer = MutableStateFlow<String?>(null)
    val streamingBuffer: StateFlow<String?> = _streamingBuffer.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedImageBase64 = MutableStateFlow<String?>(null)
    val selectedImageBase64: StateFlow<String?> = _selectedImageBase64.asStateFlow()

    private val _selectedPersonaId = MutableStateFlow(prefs.getString("persona_id", "general") ?: "general")
    val selectedPersonaId: StateFlow<String> = _selectedPersonaId.asStateFlow()

    private val _selectedModel = MutableStateFlow(prefs.getString("selected_model", GeminiClient.DEFAULT_MODEL) ?: GeminiClient.DEFAULT_MODEL)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _temperature = MutableStateFlow(prefs.getFloat("temperature", 0.7f))
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _activeScreen = MutableStateFlow("chat") // "chat", "tools", "history", "settings"
    val activeScreen: StateFlow<String> = _activeScreen.asStateFlow()

    private val _statusNotification = MutableStateFlow<String?>(null)
    val statusNotification: StateFlow<String?> = _statusNotification.asStateFlow()

    private var streamJob: Job? = null
    private var messageCollectionJob: Job? = null

    // TextToSpeech engine
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        initTts(application)
        viewModelScope.launch {
            sessions.collectLatest { sessionList ->
                if (_currentSessionId.value == null && sessionList.isNotEmpty()) {
                    selectSession(sessionList.first().id)
                } else if (_currentSessionId.value == null && sessionList.isEmpty()) {
                    createNewChat()
                }
            }
        }
    }

    private fun initTts(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                tts?.language = Locale.getDefault()
            }
        }
    }

    fun setScreen(screen: String) {
        _activeScreen.value = screen
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setSelectedImage(base64: String?) {
        _selectedImageBase64.value = base64
    }

    fun setPersona(personaId: String) {
        _selectedPersonaId.value = personaId
        prefs.edit().putString("persona_id", personaId).apply()
    }

    fun setModel(model: String) {
        _selectedModel.value = model
        prefs.edit().putString("selected_model", model).apply()
        showNotification("Switched to $model")
    }

    fun setTemperature(temp: Float) {
        _temperature.value = temp
        prefs.edit().putFloat("temperature", temp).apply()
    }

    fun saveCustomApiKey(key: String) {
        _customApiKey.value = key
        prefs.edit().putString("custom_api_key", key).apply()
        geminiClient.setCustomApiKey(if (key.isBlank()) null else key)
        showNotification("API Key updated")
    }

    fun showNotification(message: String) {
        _statusNotification.value = message
    }

    fun clearNotification() {
        _statusNotification.value = null
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        messageCollectionJob?.cancel()
        messageCollectionJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collectLatest {
                _messages.value = it
            }
        }
    }

    fun createNewChat(personaId: String? = null) {
        val pid = personaId ?: _selectedPersonaId.value
        viewModelScope.launch {
            val session = repository.createNewSession(personaId = pid)
            selectSession(session.id)
            setPersona(pid)
            _activeScreen.value = "chat"
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _currentSessionId.value = null
            _messages.value = emptyList()
            createNewChat()
            showNotification("All chat history cleared")
        }
    }

    fun sendMessage(explicitPrompt: String? = null, explicitImage: String? = null) {
        val prompt = (explicitPrompt ?: _inputText.value).trim()
        val imageBase64 = explicitImage ?: _selectedImageBase64.value

        if (prompt.isEmpty() && imageBase64 == null) return

        val sessionId = _currentSessionId.value ?: return

        // Clear input bar
        if (explicitPrompt == null) _inputText.value = ""
        if (explicitImage == null) _selectedImageBase64.value = null

        streamJob?.cancel()
        _isGenerating.value = true
        _streamingBuffer.value = ""

        viewModelScope.launch {
            // Save user message to Room
            repository.saveUserMessage(sessionId, prompt, imageBase64)

            val currentPersona = AiPersonas.getById(_selectedPersonaId.value)
            val geminiContents = repository.buildGeminiContents(sessionId, prompt, imageBase64)

            streamJob = launch {
                val fullResponse = StringBuilder()
                var hadError = false

                try {
                    repository.geminiClient.generateStreamContent(
                        contents = geminiContents,
                        systemInstruction = currentPersona.systemPrompt,
                        model = _selectedModel.value,
                        temperature = _temperature.value
                    ).collect { chunk ->
                        if (chunk.startsWith("Error (") || chunk.startsWith("⚠️")) {
                            hadError = true
                        }
                        fullResponse.append(chunk)
                        _streamingBuffer.value = fullResponse.toString()
                    }
                } catch (e: Exception) {
                    hadError = true
                    fullResponse.append("\n[Error: ${e.localizedMessage ?: "Generation interrupted"}]")
                } finally {
                    val finalAnswer = fullResponse.toString().trim()
                    if (finalAnswer.isNotEmpty()) {
                        repository.saveAssistantMessage(sessionId, finalAnswer, isError = hadError)
                    }
                    _streamingBuffer.value = null
                    _isGenerating.value = false
                }
            }
        }
    }

    fun stopGeneration() {
        streamJob?.cancel()
        val sessionId = _currentSessionId.value
        val partial = _streamingBuffer.value?.trim()
        if (sessionId != null && !partial.isNullOrEmpty()) {
            viewModelScope.launch {
                repository.saveAssistantMessage(sessionId, "$partial\n[Stopped by user]")
            }
        }
        _streamingBuffer.value = null
        _isGenerating.value = false
    }

    fun speakText(text: String) {
        if (!isTtsReady || tts == null) return
        tts?.stop()
        // Clean markdown syntax before speaking
        val clean = text.replace(Regex("[*#`_]"), "").take(1000)
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "omni_tts")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun launchToolIntoChat(tool: AiTool, userExtraInput: String, imageBase64: String?) {
        val fullPrompt = if (userExtraInput.isNotBlank()) {
            "${tool.defaultPromptPrefix}$userExtraInput"
        } else {
            "Use tool ${tool.title} to provide a comprehensive response."
        }
        createNewChat()
        _activeScreen.value = "chat"
        viewModelScope.launch {
            // Small delay to allow session creation to settle
            kotlinx.coroutines.delay(100)
            sendMessage(explicitPrompt = fullPrompt, explicitImage = imageBase64)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}
