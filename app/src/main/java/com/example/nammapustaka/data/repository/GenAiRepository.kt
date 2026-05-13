package com.example.nammapustaka.data.repository

import com.example.nammapustaka.BuildConfig
import com.example.nammapustaka.data.model.Book
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GenAiRepository {

    suspend fun askAssistant(
        question: String,
        books: List<Book>
    ): String {
        val endpoint = BuildConfig.GENAI_ASSISTANT_URL.trim()

        return if (endpoint.isBlank()) {
            askWithFirebaseAi(question, books)
        } else {
            askWithBackend(endpoint, question, books)
        }
    }

    private suspend fun askWithFirebaseAi(
        question: String,
        books: List<Book>
    ): String = withContext(Dispatchers.IO) {
        try {
            val requestedModel = BuildConfig.GENAI_MODEL_NAME.ifBlank { DEFAULT_MODEL }
            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(requestedModel)

            val response = model.generateContent(buildPrompt(question, books))
            response.text?.ifBlank { null } ?: "No answer returned from Firebase AI."
        } catch (error: Exception) {
            askWithFallbackModel(question, books, error)
        }
    }

    private suspend fun askWithFallbackModel(
        question: String,
        books: List<Book>,
        originalError: Exception
    ): String {
        if (BuildConfig.GENAI_MODEL_NAME == FALLBACK_MODEL) {
            return aiFailureMessage(originalError)
        }

        return try {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(FALLBACK_MODEL)

            val response = model.generateContent(buildPrompt(question, books))
            response.text?.ifBlank { null } ?: "No answer returned from Firebase AI."
        } catch (fallbackError: Exception) {
            aiFailureMessage(fallbackError)
        }
    }

    private suspend fun askWithBackend(
        endpoint: String,
        question: String,
        books: List<Book>
    ): String = withContext(Dispatchers.IO) {
        try {
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val payload = JSONObject()
                .put("question", question)
                .put(
                    "books",
                    JSONArray(
                        books.take(MAX_BOOKS_IN_PROMPT).map {
                            JSONObject()
                                .put("title", it.title)
                                .put("author", it.author)
                                .put("category", it.category)
                                .put("availableCopies", it.availableCopies)
                        }
                    )
                )

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload.toString())
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val answer = JSONObject(response).optString("answer", response)
            answer.ifBlank { "No answer returned from AI backend." }
        } catch (error: Exception) {
            "AI request failed: ${error.message ?: error::class.java.simpleName}"
        }
    }

    private fun buildPrompt(question: String, books: List<Book>): String {
        val catalog = books.take(MAX_BOOKS_IN_PROMPT).joinToString(separator = "\n") { book ->
            "- ${book.title} by ${book.author}; category: ${book.category}; available: ${book.availableCopies}/${book.totalCopies}"
        }.ifBlank {
            "No books are currently loaded in the catalog."
        }

        return """
            You are the NammaPustaka library assistant.
            Be as capable and helpful as a modern Gemini-style assistant: answer general questions,
            explain concepts, help with studying, writing, planning, translation, summaries,
            troubleshooting, and library guidance.

            When the user asks about books, availability, issuing, returning, categories, or the
            NammaPustaka catalog, use the catalog snapshot below as the source of truth. Do not invent
            availability for books not listed. If the question is unrelated to the catalog, answer it
            normally and clearly.

            Catalog:
            $catalog

            User question:
            $question
        """.trimIndent()
    }

    private companion object {
        const val DEFAULT_MODEL = "gemini-2.5-pro"
        const val FALLBACK_MODEL = "gemini-2.5-flash"
        const val MAX_BOOKS_IN_PROMPT = 200

        fun aiFailureMessage(error: Exception): String {
            return "AI request failed: ${error.message ?: error::class.java.simpleName}. Make sure Firebase AI Logic is enabled for the Firebase project and App Check is configured before production use."
        }
    }
}
