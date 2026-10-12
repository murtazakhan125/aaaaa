package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiGenerationConfig
import com.example.data.model.GeminiInlineData
import com.example.data.model.GeminiPart
import com.example.data.model.GeminiRequest
import com.example.data.model.GeminiResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient(
    private var customApiKey: String? = null
) {
    companion object {
        private const val TAG = "GeminiClient"
        const val DEFAULT_MODEL = "gemini-3.5-flash"
        const val PRO_MODEL = "gemini-3.1-pro-preview"
        private const val BASE_URL = "https://generativelanguage.googleapis.com"
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    fun setCustomApiKey(key: String?) {
        customApiKey = key?.trim()
    }

    fun getEffectiveApiKey(): String {
        val custom = customApiKey
        if (!custom.isNullOrBlank()) return custom
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateContent(
        contents: List<GeminiContent>,
        systemInstruction: String? = null,
        model: String = DEFAULT_MODEL,
        temperature: Float = 0.7f
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException(
                    "Gemini API key is not configured. Please set your GEMINI_API_KEY in the AI Studio Secrets panel, or enter your key in Settings."
                )
            )
        }

        val requestPayload = GeminiRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(temperature = temperature),
            systemInstruction = systemInstruction?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            }
        )

        val jsonString = requestAdapter.toJson(requestPayload)
        val requestBody = jsonString.toRequestBody("application/json".toMediaType())

        val url = "$BASE_URL/v1beta/models/$model:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(bodyString)
                    errJson.optJSONObject("error")?.optString("message") ?: bodyString
                } catch (e: Exception) {
                    bodyString
                }
                return@withContext Result.failure(Exception("API Error (${response.code}): $errorMsg"))
            }

            val parsed = responseAdapter.fromJson(bodyString)
            val candidateText = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (candidateText != null) {
                Result.success(candidateText)
            } else {
                Result.failure(Exception("No content returned in candidate response."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed", e)
            Result.failure(e)
        }
    }

    fun generateStreamContent(
        contents: List<GeminiContent>,
        systemInstruction: String? = null,
        model: String = DEFAULT_MODEL,
        temperature: Float = 0.7f
    ): Flow<String> = flow {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            emit("⚠️ Gemini API key is not configured. Please set GEMINI_API_KEY in the AI Studio Secrets panel, or enter your key in App Settings.")
            return@flow
        }

        val requestPayload = GeminiRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(temperature = temperature),
            systemInstruction = systemInstruction?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            }
        )

        val jsonString = requestAdapter.toJson(requestPayload)
        val requestBody = jsonString.toRequestBody("application/json".toMediaType())

        val url = "$BASE_URL/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                val errorMsg = try {
                    JSONObject(errorBody).optJSONObject("error")?.optString("message") ?: errorBody
                } catch (e: Exception) {
                    errorBody
                }
                emit("Error (${response.code}): $errorMsg")
                return@flow
            }

            val body = response.body ?: return@flow
            body.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim() ?: continue
                    if (!rawLine.startsWith("data:")) continue
                    val jsonPayload = rawLine.removePrefix("data:").trim()
                    if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

                    try {
                        val json = JSONObject(jsonPayload)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                val text = parts.getJSONObject(0).optString("text", "")
                                if (text.isNotEmpty()) {
                                    emit(text)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Chunk parse warning", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Stream failed", e)
            emit("\n[Connection Error: ${e.localizedMessage ?: "Unknown"}]")
        }
    }.flowOn(Dispatchers.IO)
}
