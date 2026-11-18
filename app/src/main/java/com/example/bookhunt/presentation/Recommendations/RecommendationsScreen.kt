package com.example.bookhunt.presentation.Recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bookhunt.data.GeminiBookRecommender
import com.example.bookhunt.presentation.ViewModel
import kotlinx.coroutines.launch
import com.example.bookhunt.presentation.UIComponent.BookCart
import androidx.compose.ui.Alignment


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    navHostController: NavHostController,
    viewModel: ViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var request by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    val recommendedBooks = state.items.filter { book ->
        book.bookName.isNotBlank() &&
                answer.contains(book.bookName, ignoreCase = true)
    }

    var error by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val recommender = remember { GeminiBookRecommender() }

    LaunchedEffect(Unit) {
        viewModel.BringAllBooks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Book Picks") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Tell us what you like, and get suggestions from the books in Book Hunt.")

            OutlinedTextField(
                value = request,
                onValueChange = { request = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("What would you like to read?") },
                placeholder = { Text("I like physics and want something beginner friendly") },
                minLines = 3
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isThinking && request.isNotBlank() && state.items.isNotEmpty(),
                onClick = {
                    isThinking = true
                    answer = ""
                    error = ""

                    scope.launch {
                        try {
                            answer = recommender.recommend(request.trim(), state.items)
                        } catch (exception: Exception) {
                            error = exception.localizedMessage
                                ?: "Couldn't get recommendations. Please try again."
                        } finally {
                            isThinking = false
                        }
                    }
                }
            ) {
                Text("Find my books")
            }

            if (state.isLoading) Text("Loading the Book Hunt catalog…")
            if (state.error.isNotBlank()) Text("Could not load books: ${state.error}")
            if (isThinking) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            if (error.isNotBlank()) {
                Text(
                    error,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
            if (answer.isNotBlank()) {
                Text(
                    "Recommendations",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
                Text(answer)
                recommendedBooks.forEach { book ->
                    BookCart(
                        author = book.bookAuthor,
                        imageUrl = book.image,
                        title = book.bookName,
                        description = book.bookDescription,
                        navHostController = navHostController,
                        bookUrl = book.bookUrl
                    )
                }
            }
        }
    }
}