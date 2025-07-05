package com.example.bookhunt.domain.Repo

import com.example.bookhunt.common.BookCategoryModel
import com.example.bookhunt.common.BookModel
import com.example.bookhunt.common.ResultState
import kotlinx.coroutines.flow.Flow



interface AllBooksRepo {
    fun getAllBooks() : Flow<ResultState<List<BookModel>>>
    fun getAllCategory() : Flow<ResultState<List<BookCategoryModel>>>
    fun getBookByCategory(category : String) : Flow<ResultState<List<BookModel>>>

}