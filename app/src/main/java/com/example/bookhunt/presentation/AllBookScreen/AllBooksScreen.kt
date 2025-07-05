package com.example.bookhunt.presentation.AllBookScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bookhunt.presentation.Effects.AnimateShimmer
import com.example.bookhunt.presentation.UIComponent.BookCart
//import com.example.bookhunt.presentation.UIComponent.BookCart
import com.example.bookhunt.presentation.ViewModel


@Composable
fun AllBooksScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.BringAllBooks()
    }

    var res = viewModel.state.value
    when{
        res.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn {
                    items(10){
                        AnimateShimmer()
                    }
                }
            }
        }

        res.error.isNotEmpty() -> {
            Text(text = res.error, modifier = modifier)
        }

        res.items.isNotEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn (
                    modifier = Modifier.fillMaxSize()
                ){
                    items(res.items){
                        BookCart(
                            author = it.bookAuthor,
                            imageUrl = it.image,
                            title = it.bookName,
                            description = it.bookDescription,
                            navHostController = navHostController,
                            bookUrl = it.bookUrl
                        )
                    }
                }
            }
        }else ->{
            Text(text = "No books Available", modifier = Modifier)
        }
    }
}