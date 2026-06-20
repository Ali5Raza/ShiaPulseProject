package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.network.Content
import com.example.network.GeminiClient
import com.example.network.GenerateContentRequest
import com.example.network.Part
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

class AIChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var lastMessageTime: Long = 0
    private val COOLDOWN_MS = 3000L // 3 seconds cooldown between messages

    init {
        // Initial greeting
        _messages.value = listOf(
            ChatMessage(
                text = "Salaam! I am your Shia Pulse AI Companion. You can ask me anything about Islam, Ahlul Bayt (as), jurisprudence, or any other topic. How can I help you today?",
                isUser = false
            )
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMessageTime < COOLDOWN_MS) {
            _messages.value = _messages.value + ChatMessage(
                text = "Please wait a few seconds before sending another message. (Spam Protection)",
                isUser = false,
                isError = true
            )
            return
        }
        lastMessageTime = currentTime

        val userMessage = ChatMessage(text = text, isUser = true)
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Build history
                val contents = _messages.value.mapNotNull { msg ->
                    if (msg.isError) return@mapNotNull null
                    Content(
                        role = if (msg.isUser) "user" else "model",
                        parts = listOf(Part(text = msg.text))
                    )
                }

                val systemInstruction = Content(
                    role = "user",
                    parts = listOf(Part("You are a helpful and knowledgeable Islamic assistant, specializing in Shia Islam. You answer questions accurately based on the teachings of Ahlul Bayt (as). Be respectful, concise, and helpful. Start the conversation with a greeting like Salaam! and provide information related to Islamic teachings, history, jurisprudence, and daily practices."))
                )

                val request = GenerateContentRequest(
                    contents = contents,
                    systemInstruction = systemInstruction
                )

                val response = GeminiClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "No response from AI."

                _messages.value = _messages.value + ChatMessage(text = replyText, isUser = false)
            } catch (e: HttpException) {
                val errorMsg = if (e.code() == 429) {
                    "System is highly active right now due to many users. Please try again in a minute."
                } else {
                    "Error connecting to AI Server (${e.code()}). Please try again later."
                }
                _messages.value = _messages.value + ChatMessage(text = errorMsg, isUser = false, isError = true)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    text = "Sorry, there was an error connecting to the AI. Please check your internet.",
                    isUser = false,
                    isError = true
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
