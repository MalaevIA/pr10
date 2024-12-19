package com.example.pr10

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.work.*
import com.example.pr10.ui.theme.Pr10Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pr10Theme {
                val imageList = remember { mutableStateListOf<Bitmap>() }
                MainApp(imageList)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(imageList: MutableList<Bitmap>) {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Image Downloader") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "download",
            Modifier.padding(paddingValues)
        ) {
            composable("download") { DownloadScreen(imageList) }
            composable("history") { HistoryScreen(imageList) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomAppBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Download, contentDescription = "Download") },
            label = { Text("Download") },
            selected = false,
            onClick = { navController.navigate("download") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.History, contentDescription = "History") },
            label = { Text("History") },
            selected = false,
            onClick = { navController.navigate("history") }
        )
    }
}

@Composable
fun DownloadScreen(imageList: MutableList<Bitmap>) {
    val context = LocalContext.current
    var url by remember { mutableStateOf("https://sun1-13.userapi.com/impg/YBSqa8ohZvZifBvip8GArdhYjDu9PSNSDNR7yg/XfQyuC12nlw.jpg?size=900x1027&quality=96&sign=f2ada84e5649c1ba66dc60c484671220&type=album") }
    var filename by remember { mutableStateOf("downloaded_image.png") }
    val workManager = WorkManager.getInstance(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Enter Image URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = filename,
            onValueChange = { filename = it },
            label = { Text("Enter File Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val workRequest = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
                .setInputData(workDataOf("IMAGE_URL" to url, "FILE_NAME" to filename))
                .build()

            workManager.enqueue(workRequest)
            workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { info ->
                info?.let {
                    if (it.state == WorkInfo.State.SUCCEEDED) {
                        Toast.makeText(context, "Image downloaded successfully", Toast.LENGTH_SHORT).show()
                        val filePath = it.outputData.getString("FILE_PATH")
                        filePath?.let { path ->
                            val bitmap = BitmapFactory.decodeFile(path)
                            imageList.add(bitmap)
                        }
                    }
                }
            }
        }) {
            Text("Download Image")
        }
    }
}

@Composable
fun HistoryScreen(imageList: List<Bitmap>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(imageList) { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Downloaded Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Pr10Theme {
        MainApp(remember { mutableStateListOf() })
    }
}
