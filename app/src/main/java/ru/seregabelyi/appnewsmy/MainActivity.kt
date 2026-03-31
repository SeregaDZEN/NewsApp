package ru.seregabelyi.appnewsmy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.seregabelyi.appnewsmy.models.GNewsArticle
import ru.seregabelyi.appnewsmy.network.GNewsRetrofitClient
import ru.seregabelyi.appnewsmy.ui.theme.AppNewsMyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNewsMyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var articles by remember { mutableStateOf<List<GNewsArticle>>(emptyList()) }
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
                                    onSuccess = { newList ->
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
    onSuccess: (List<GNewsArticle>) -> Unit,
    onError: (String) -> Unit
) {
    Log.d("GNews", "loadNews вызвана!")
    onStart()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("GNews", "Начинаем запрос к GNews")

            val response = GNewsRetrofitClient.apiService.getTopHeadlines(
                country = "us",
                token = BuildConfig.GNEWS_API_KEY
            ).execute()

            Log.d("GNews", "Код ответа: ${response.code()}")

            if (response.isSuccessful) {
                val newList = response.body()?.articles ?: emptyList()
                if (newList.isNotEmpty()) {
                    Log.d("GNews", "=== ПЕРВАЯ НОВОСТЬ ===")
                    Log.d("GNews", "Заголовок: ${newList[0].title}")
                    Log.d("GNews", "Картинка URL: ${newList[0].image}")
                } else {
                    Log.d("GNews", "Список новостей ПУСТ!")
                }
                Log.d("GNews", "Успех! Количество новостей: ${newList.size}")
                withContext(Dispatchers.Main) {
                    onSuccess(newList)
                }
            } else {
                Log.e("GNews", "Ошибка сервера: ${response.code()}")
                withContext(Dispatchers.Main) {
                    onError("Ошибка ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("GNews", "Исключение: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onError("Ошибка: ${e.message}")
            }
        }
    }
}

@Composable
fun NewsCard(article: GNewsArticle) {
    val context = LocalContext.current  // ← получаем контекст

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.image))
                context.startActivity(intent)  // ← используем context
            }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            val imageUrl = article.image
            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        error = painterResource(android.R.drawable.ic_menu_gallery)  // ← иконка, если не загрузилось
                    ),
                    contentDescription = "News image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Text(text = article.title)
            Text(text = article.description ?: "")
            Text(text = article.source.name)
        }
    }
}