package com.example.bookhunt.data

import com.example.bookhunt.BuildConfig
import com.example.bookhunt.common.BookModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Calls Gemini with the book catalog already present in Book Hunt. */
class GeminiBookRecommender {
    suspend fun recommend(preferences: String, books: List<BookModel>): String =
        withContext(Dispatchers.IO) {
            require(BuildConfig.GEMINI_API_KEY.isNotBlank()) {
                "Add GEMINI_API_KEY to local.properties to enable AI Picks."
            }
            require(books.isNotEmpty()) {
                "The book catalog is empty. Load books and try again."
            }

            val catalog = books.joinToString("\n") { book ->
                "Title: ${book.bookName}; Author: ${book.bookAuthor}; " +
                        "Category: ${book.category}; Description: ${book.bookDescription}"
            }

            val prompt = """
                You are Book Hunt's book recommendation assistant.
                Recommend up to 3 books ONLY from the catalog below. Never invent a title.
                For every recommendation, include the exact book title copied from the catalog
                and briefly explain why it fits the reader's request.

                Reader request: $preferences

                Book catalog:
                $catalog
            """.trimIndent()

            val requestBody = JSONObject()
                .put(
                    "contents",
                    org.json.JSONArray().put(
                        JSONObject().put(
                            "parts",
                            org.json.JSONArray().put(
                                JSONObject().put("text", prompt)
                            )
                        )
                    )
                )
                .put("generationConfig", JSONObject().put("temperature", 0.4))

            val connection = (
                    URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent")
                        .openConnection() as HttpURLConnection
                    ).apply {
                    requestMethod = "POST"
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
                }

            try {
                connection.outputStream.use {
                    it.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                }

                val status = connection.responseCode
                val body = (
                        if (status in 200..299) connection.inputStream
                        else connection.errorStream
                        )?.bufferedReader()?.use { it.readText() }.orEmpty()

                if (status !in 200..299) {
                    val detail = runCatching {
                        JSONObject(body)
                            .getJSONObject("error")
                            .getString("message")
                    }.getOrNull()

                    error(detail ?: "Gemini request failed (HTTP $status). Check the key and try again.")
                }

                JSONObject(body)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } finally {
                connection.disconnect()
            }
        }
}