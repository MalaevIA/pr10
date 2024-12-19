package com.example.pr10

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ImageDownloadWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val imageUrl = inputData.getString("IMAGE_URL") ?: return Result.failure()
        val fileName = inputData.getString("FILE_NAME") ?: "downloaded_image.png"
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(imageUrl).build()
            val response = client.newCall(request).execute()

            val bitmap = BitmapFactory.decodeStream(response.body?.byteStream())
            val file = File(applicationContext.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            Result.success(workDataOf("FILE_PATH" to file.absolutePath))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
