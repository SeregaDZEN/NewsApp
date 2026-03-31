package ru.seregabelyi.appnewsmy.models



data class GNewsArticle(
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,
    val image: String?,  // ← Картинка тут!
    val publishedAt: String,
    val source: GNewsSource
)
