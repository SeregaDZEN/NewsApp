package ru.seregabelyi.appnewsmy.models

data class GNewsResponse(
    val totalArticles: Int,
    val articles: List<GNewsArticle>
)
