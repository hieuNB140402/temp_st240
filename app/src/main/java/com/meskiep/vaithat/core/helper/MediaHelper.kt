package com.meskiep.vaithat.core.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.meskiep.vaithat.R
import com.meskiep.vaithat.core.utils.key.ValueKey
import com.meskiep.vaithat.core.utils.state.HandleState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL
import androidx.core.graphics.scale
import com.meskiep.vaithat.core.extension.dLog
import com.meskiep.vaithat.core.extension.eLog
import com.meskiep.vaithat.core.utils.key.AssetsKey
import com.meskiep.vaithat.core.utils.key.DomainKey
import com.meskiep.vaithat.core.utils.state.SaveState
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedOutputStream
import java.io.OutputStream

object MediaHelper {
    // Sort file (folder)
    fun sortAsset(listFiles: Array<String>?): List<String>? {
        val sortedFiles = listFiles?.sortedWith(compareBy { fileName ->
            val matchResult = Regex("\\d+").find(fileName)
            matchResult?.value?.toIntOrNull() ?: Int.MAX_VALUE
        })
        return sortedFiles
    }

    // Get file from internal
    fun getImageInternal(context: Context, album: String): ArrayList<String> {
        val imagePaths = ArrayList<String>()
        val targetDir = File(context.filesDir, album)

        if (targetDir.exists() && targetDir.isDirectory) {
            targetDir.listFiles()?.filter { isImageFile(it) }?.sortedByDescending { it.lastModified() }
                ?.forEach { file ->
                    imagePaths.add(file.absolutePath)
                }
        }
        return imagePaths
    }

    // is file?
    fun isImageFile(file: File): Boolean {
        val imageExtensions = listOf("jpg", "jpeg", "png", "bmp", "webp")
        val extension = file.extension.lowercase()
        return file.isFile && imageExtensions.contains(extension)
    }

    fun deleteFileByPath(pathList: ArrayList<String>): Flow<HandleState> = flow {
        emit(HandleState.LOADING)
        try {
            for (i in 0 until pathList.size) {
                val file = File(pathList[i])
                if (file.exists()) {
                    file.delete()
                }
            }
            emit(HandleState.SUCCESS)
        } catch (e: Exception) {
            emit(HandleState.FAIL)
        }
    }.flowOn(Dispatchers.IO)

    fun deleteFileByPathNotFlow(pathList: ArrayList<String>) {
        try {
            for (i in 0 until pathList.size) {
                val file = File(pathList[i])
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("nbhieu", "deleteFileByPathNotFlow: $e")
        }
    }

    suspend fun downloadVideoCompat(context: Context, videoUrl: String): HandleState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadUsingMediaStore(context, videoUrl)
        } else {
            downloadUsingDownloadManager(context, videoUrl)
        }
    }

    private suspend fun downloadUsingMediaStore(
        context: Context, videoUrl: String
    ): HandleState {
        return try {
            val resolver = context.contentResolver
            val fileName = StringHelper.generateRandomVideoFileName()
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(
                    MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/" + ValueKey.DOWNLOAD_ALBUM
                )
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            val videoUri =
                resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return HandleState.FAIL

            URL(videoUrl).openStream().use { input ->
                resolver.openOutputStream(videoUri)?.use { output ->
                    input.copyTo(output)
                } ?: return HandleState.FAIL
            }

            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(videoUri, contentValues, null, null)
            HandleState.SUCCESS
        } catch (e: Exception) {
            Log.e("nbhieu", "downloadUsingMediaStore: ${e.message}")
            HandleState.FAIL
        }
    }


    private suspend fun downloadUsingDownloadManager(
        context: Context, videoUrl: String
    ): HandleState {
        return try {
            val fileName = StringHelper.generateRandomVideoFileName()
            val request = DownloadManager.Request(videoUrl.toUri()).apply {
                setTitle(context.getString(R.string.downloading))
                setDescription(fileName)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_MOVIES, "/${ValueKey.DOWNLOAD_ALBUM}/$fileName"
                )
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            HandleState.SUCCESS
        } catch (e: Exception) {
            HandleState.FAIL
        }
    }

    inline fun <reified T> writeListToFile(context: Context, fileName: String, list: List<T>) {
        try {
            val json = Gson().toJson(list)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inline fun <reified T> readListFromFile(context: Context, fileName: String): List<T> {
        return try {
            val json = context.openFileInput(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<T>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: FileNotFoundException) {
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    inline fun <reified T> writeModelToFile(
        context: Context,
        folder: String,
        fileName: String,
        model: T
    ): String {
        return try {
            val json = Gson().toJson(model)

            // Tạo folder trong internal storage
            val dir = File(context.filesDir, folder)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file = File(dir, fileName)

            file.outputStream().use { output ->
                output.write(json.toByteArray())
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            eLog("writeModelToFile: ${e.message}")
            ""
        }
    }

    inline fun <reified T> readModelFromFile(context: Context, fileName: String): T? {
        return try {
            context.openFileInput(fileName).use { input ->
                val json = input.bufferedReader().readText()
                Gson().fromJson(json, T::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun checkFileInternal(context: Context, fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists() || file.length() > 0
    }

    suspend fun downloadVideoToCache(context: Context, videoUrl: String): File? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir


            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".mp4")) {
                    file.delete()
                }
            }

            val fileName = "wallpaper_${System.currentTimeMillis()}.mp4"
            val file = File(cacheDir, fileName)

            val url = URL(videoUrl)
            url.openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun Activity.saveVideoToInternalStorage(album: String, videoUrl: String): String? {
        val fileName = StringHelper.generateRandomVideoFileName()

        return try {
            val directory = File(filesDir, album)
            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, fileName)

            val url = URL(videoUrl)
            url.openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun Activity.saveBitmapToCache(bitmap: Bitmap): File {
        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "shared_image.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    suspend fun saveBitmapToInternalStorage(
        context: Context,
        album: String,
        bitmap: Bitmap
    ): Flow<SaveState> = flow {
        emit(SaveState.Loading)

        try {
            val name = StringHelper.generateRandomImageFileName()
            val directory = File(context.filesDir, album)

            if (!directory.exists()) {
                directory.mkdir()
            }

            // 👉 Resize về 585x559
            val resizedBitmap = Bitmap.createScaledBitmap(
                bitmap,
                585,
                559,
                true // filter (smooth)
            )

            val file = File(directory, name)

            FileOutputStream(file).use { output ->
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                output.flush()
            }

            // recycle bitmap cũ nếu không dùng nữa
            bitmap.recycle()
            resizedBitmap.recycle()

            val abs = file.absolutePath
            val result = abs.substringAfter("/files/")

            emit(SaveState.Success(result))
        } catch (e: Exception) {
            emit(SaveState.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    fun saveBitmapToInternalStorageZip(
        context: Context,
        album: String,
        bitmap: Bitmap,
        oldFileName: String = ""
    ): Flow<SaveState> = flow {
        emit(SaveState.Loading)
        val name = if (oldFileName == "") {
            StringHelper.generateRandomImageFileName()
        } else {
            oldFileName.split("/").last()
        }
        val resizedBitmap = bitmap.scale(512, 512)
        try {
            val directory = File(context.filesDir, album)

            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, name)

            if (oldFileName != "" && file.exists()) {
                file.delete()
            }

            val fileOutputStream = FileOutputStream(file)

            var quality = 100
            do {
                fileOutputStream.flush()
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutputStream)
                quality -= 5 // Giảm chất lượng sau mỗi lần nén
            } while (file.length() > 512 * 1024 && quality > 5) // 512 KB và chất lượng không dưới 5%

            fileOutputStream.flush()
            fileOutputStream.close()

            resizedBitmap.recycle()

            emit(SaveState.Success(file.absolutePath))
        } catch (e: Exception) {
            emit(SaveState.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    fun Activity.saveBitmapToInternalStorageZip(bitmap: Bitmap): String? {
        val name = StringHelper.generateRandomImageFileName()
        // Giảm kích thước ảnh xuống 512x512 px
        val resizedBitmap = bitmap.scale(512, 512)

        return try {
            val directory = File(filesDir, ValueKey.DOWNLOAD_ALBUM)

            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, "$name.png")

            val fileOutputStream = FileOutputStream(file)

            var quality = 100
            do {
                fileOutputStream.flush()
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, quality, fileOutputStream)
                quality -= 5 // Giảm chất lượng sau mỗi lần nén
            } while (file.length() > 512 * 1024 && quality > 5) // 512 KB và chất lượng không dưới 5%

            fileOutputStream.flush()
            fileOutputStream.close()

            resizedBitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadPartsToExternal(activity: Activity, pathList: List<String>): Flow<HandleState> = flow {
        emit(HandleState.LOADING)

        if (pathList.isEmpty()) {
            emit(HandleState.FAIL)
            return@flow
        }

        val bitmapList = if (pathList.first().contains(AssetsKey.ASSET_MANAGER)) {
            listOf(AssetHelper.getBitmapFromAsset(activity, pathList.first())!!)
        } else {
            BitmapHelper.convertPathsToBitmaps(activity, pathList)
        }


        if (bitmapList.size == 1) {
            emitAll(saveBitmapToExternal(activity, bitmapList.first()))
        } else {
            var allSuccess = true
            for (bitmap in bitmapList) {
                val state = saveBitmapToExternal(activity, bitmap).last()
                if (state == HandleState.FAIL) {
                    allSuccess = false
                    break
                }
            }
            emit(if (allSuccess) HandleState.SUCCESS else HandleState.FAIL)
        }
    }

    // bitmap -> external storage
    fun saveBitmapToExternal(activity: Activity, bitmap: Bitmap): Flow<HandleState> = flow {
        emit(HandleState.LOADING)

        val state = withContext(Dispatchers.IO) {
            try {
                val resolver = activity.contentResolver
                val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val contentValues = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.png"
                    )
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${ValueKey.DOWNLOAD_ALBUM}"
                        )
                    } else {
                        val directory = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            ValueKey.DOWNLOAD_ALBUM
                        )
                        if (!directory.exists()) {
                            directory.mkdirs()
                        }
                        val filePath = File(directory, "image_${System.currentTimeMillis()}.png").absolutePath
                        put(MediaStore.Images.Media.DATA, filePath)
                    }
                }

                val imageUri = resolver.insert(imageCollection, contentValues) ?: return@withContext HandleState.FAIL

                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    val isSaved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    if (isSaved) HandleState.SUCCESS else HandleState.FAIL
                } ?: HandleState.FAIL

            } catch (e: Exception) {
                e.printStackTrace()
                HandleState.FAIL
            }
        }

        emit(state)
    }

    // get image external storage
    @SuppressLint("Recycle")
    fun getAllImages(context: Context): List<Uri> {
        val images = mutableListOf<Uri>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                images.add(contentUri)
            }
        }

        return images
    }

    suspend fun moveFolderInternal(context: Context, filePath: String, targetFolder: String): Boolean {
        //fileName: /data/user/0/com.meskiep.vaithat/files/Cache Album/IMG_214515262189.png
        //targetFolder: Shirt

        val sourceFile = File(filePath)

        if (!sourceFile.exists()) return false

        val destFolder = File(context.filesDir, targetFolder)
        if (!destFolder.exists()) {
            destFolder.mkdirs()
        }

        val destFile = File(destFolder, sourceFile.name)

        return try {
            sourceFile.copyTo(destFile, overwrite = true)
            sourceFile.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearFolder(context: Context, folderName: String) {
        val folder = File(context.filesDir, folderName)

        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }

    suspend fun moveInternalFile(context: Context, fromPath: String, toPath: String): String {
        return try {
            val fromFile = File(context.filesDir, fromPath)
            val toFile = File(context.filesDir, toPath)

            if (!fromFile.exists()) return ""

            // đảm bảo folder đích tồn tại
            toFile.parentFile?.let {
                if (!it.exists()) it.mkdirs()
            }

            // 1. thử rename (nhanh nhất)
            if (fromFile.renameTo(toFile)) {
                val abs = toFile.absolutePath
                val result = abs.substringAfter("/files/")
                return result
            }

            // 2. fallback: copy + delete
            fromFile.inputStream().use { input ->
                toFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            fromFile.delete()
            val abs = toFile.absolutePath
            val result = abs.substringAfter("/files/")
            return result

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun downloadAllToExternal(context: Context, paths: List<String>, folderName: String): Boolean =
        withContext(Dispatchers.IO) {

            val result = mutableListOf<String>()

            paths.forEach { path ->
                try {
                    val fileName = path.substringAfterLast("/")
                    val finalName = generateUniqueFileName(context, fileName, folderName)

                    val outputStream = openOutputStreamSmart(context, finalName, folderName)
                        ?: return@forEach

                    val tempStream = BufferedOutputStream(outputStream)

                    when {
                        path.startsWith(DomainKey.HTTP) -> {
                            URL(path).openStream().use { input ->
                                input.copyTo(tempStream)
                            }
                        }

                        path.startsWith(AssetsKey.ASSET_MANAGER) -> {
                            val assetPath = path.removePrefix("${AssetsKey.ASSET_MANAGER}/")
                            context.assets.open(assetPath).use { input ->
                                input.copyTo(tempStream)
                            }
                        }

                        else -> {
                            val internalFile = File(path)
                            if (!internalFile.exists()) return@forEach

                            internalFile.inputStream().use { input ->
                                input.copyTo(tempStream)
                            }
                        }
                    }

                    tempStream.flush()
                    tempStream.close()

                    result.add(finalName)

                } catch (e: Exception) {
                    e.printStackTrace()
                    eLog("Download error: $e")
                }
            }

            result.forEachIndexed { index, string ->
                dLog("Download[$index]: $string")
            }
            result.size == paths.size
        }

    fun getUniqueFile(file: File): File {
        if (!file.exists()) return file

        var index = 1
        val name = file.nameWithoutExtension
        val ext = file.extension

        var newFile: File
        do {
            val newName = if (ext.isNotEmpty()) {
                "$name($index).$ext"
            } else {
                "$name($index)"
            }
            newFile = File(file.parent, newName)
            index++
        } while (newFile.exists())

        return newFile
    }

    fun openOutputStreamSmart(
        context: Context,
        fileName: String,
        folderName: String
    ): OutputStream? {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(fileName))
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$folderName"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val uri = resolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return null

            val stream = resolver.openOutputStream(uri)

            // mark done
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            stream

        } else {
            // Android 7–9
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "$folderName/$fileName"
            )

            file.parentFile?.mkdirs()
            FileOutputStream(file)
        }
    }

    fun generateUniqueFileName(
        context: Context,
        fileName: String,
        folderName: String
    ): String {

        var name = fileName
        var index = 1

        while (fileExists(context, name, folderName)) {
            val base = fileName.substringBeforeLast(".")
            val ext = fileName.substringAfterLast(".", "")
            name = if (ext.isNotEmpty()) {
                "$base($index).$ext"
            } else {
                "$base($index)"
            }
            index++
        }

        return name
    }

    fun fileExists(
        context: Context,
        fileName: String,
        folderName: String
    ): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=?"
            val selectionArgs = arrayOf(fileName)

            context.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                cursor.count > 0
            } ?: false

        } else {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "$folderName/$fileName"
            )
            file.exists()
        }
    }

    fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast(".", "").lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "glb" -> "model/gltf-binary"
            else -> "application/octet-stream"
        }
    }
}