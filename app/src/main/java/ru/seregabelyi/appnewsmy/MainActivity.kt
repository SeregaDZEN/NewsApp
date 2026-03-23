package ru.seregabelyi.appnewsmy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.seregabelyi.appnewsmy.models.Article
import ru.seregabelyi.appnewsmy.network.RetrofitClient
import ru.seregabelyi.appnewsmy.ui.theme.AppNewsMyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNewsMyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var articles by remember { mutableStateOf<List<Article>>(emptyList()) }
                    var isLoading by remember { mutableStateOf(false) }
                         var error by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {

                        Button(
                            onClick = {
                                loadNews(
                                    onStart = { isLoading = true },
                                    onSuccess = { newList->
                                        articles = newList
                                        isLoading = false
                                    },
                                    onError = { errorMessage ->
                                        error = errorMessage
                                        isLoading = false
                                    }
                                )
                            },
                            enabled = !isLoading
                        ) {
                            Text(if (isLoading) "Загружаем..." else "Загрузить новости")
                        }
                        LazyColumn {
                            items(articles) { article ->
                                NewsCard(article)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun loadNews(
    onStart: () -> Unit,
    onSuccess: (List<Article>) -> Unit,
    onError: (String) -> Unit
) {
    onStart()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.getTopHeadlines(
                country = "us",
                apiKey = BuildConfig.NEWS_API_KEY
            ).execute()

            if (response.isSuccessful) {
                val newList = response.body()?.articles ?: emptyList()
                withContext(Dispatchers.Main) {
                    onSuccess(newList)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Ошибка ${response.code()}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Ошибка: ${e.message}")
            }
        }
    }
}

@Composable
fun NewsCard(article: Article) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text=article.title)
            Text(text = article.description ?: "")
            Text(text = article.source.name)
        }
    }
}