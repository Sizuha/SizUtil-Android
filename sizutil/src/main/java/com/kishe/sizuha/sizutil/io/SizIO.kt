package com.kishe.sizuha.sizutil.io

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import java.io.*

fun isValidFileName(filenameWithoutPath: String): Boolean {
    val reservedChars = arrayOf("|", "\\", "?", "*", "<", "\"", ":", ">")

    if (filenameWithoutPath.trim { it <= ' ' }.isEmpty())
        return false

    for (c in reservedChars) {
        if (filenameWithoutPath.contains(c)) return false
    }

    return true
}

fun copyStream(input: InputStream, os: OutputStream) {
    val bufferSize = 1024
    try {
        val bytes = ByteArray(bufferSize)
        while (true) {
            val count = input.read(bytes, 0, bufferSize)
            if (count == -1)
                break
            os.write(bytes, 0, count)
        }
    }
    catch (ignored: Exception) {}
}

fun makeDirs(fileWithPath: String) {
    val lastIdxSlash = fileWithPath.lastIndexOf("/")
    val dirPath = fileWithPath.substring(0, lastIdxSlash + 1)

    val f = File(dirPath)
    f.mkdirs()
}


fun getFilenameFormUrl(url_str: String): String {
    val lastIdxSlash = url_str.lastIndexOf("/")
    return if (lastIdxSlash < 0) url_str else url_str.substring(lastIdxSlash + 1)
}

fun getOnlyFilename(filepath: String): String {
    val filepath = getFilenameFormUrl(filepath)

    val lastIdx = filepath.lastIndexOf(".")
    return if (lastIdx < 0) filepath else filepath.substring(0, lastIdx)
}

fun getFileExt(filepath: String): String {
    val filepath = getFilenameFormUrl(filepath)

    val lastIdx = filepath.lastIndexOf(".")
    return if (lastIdx < 0) "" else filepath.substring(lastIdx + 1)
}

fun getContentUri(c: Context, path: String): Uri {
    val file = File(path)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        return Uri.fromFile(file)
    }

    val authority = c.applicationContext.packageName + ".provider"
    return FileProvider.getUriForFile(c, authority, file)
}

/**
 * Get a file path from a Uri. This will get the the path for Storage Access
 * Framework Documents, as well as the _data field for the MediaStore and
 * other file-based ContentProviders.
 *
 * @param context The context.
 * @param uri The Uri to query.
 * @author paulburke
 */
fun getPath(context: Context, uri: Uri): String? {
    // DocumentProvider
    if (DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }

            // TODO handle non-primary volumes
        }
        else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
            )

            return getDataColumn(context, contentUri, null, null)
        }
        else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            val contentUri: Uri? = when (type) {
                "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])

            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    }
    else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
        return getDataColumn(context, uri, null, null)
    }
    else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
        return uri.path
    }

    return null
}

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context The context.
 * @param uri The Uri to query.
 * @param selection (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(columnIndex)
        }
    }
    finally {
        cursor?.close()
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

fun isJpegFile(filePath: String): Boolean {
    val src = filePath.toLowerCase()
    return src.endsWith(".jpg") || src.endsWith(".jpeg")
}

fun readAllText(input: InputStream, encoding: String = "UTF-8"): String? {
    try {
        val inReader = InputStreamReader(input, encoding)
        val bufReader = BufferedReader(inReader)

        val result = StringBuilder()

        // 1行ずつテキストを読み込む
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bufReader.lines().forEach { line ->
                result.append(line)
            }
        }
        else {
            var line: String?
            do {
                line = bufReader.readLine()
                if (line != null) {
                    result.append(line)
                }
            } while (line != null)
        }

        bufReader.close()
        inReader.close()
        input.close()

        return result.toString()
    }
    catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}


fun loadAssetText(context: Context, asset_path: String): String? {
    var input: BufferedReader? = null
    try {
        val buf = StringBuilder()
        val source = context.assets.open(asset_path)
        input = BufferedReader(InputStreamReader(source))

        var isFirst = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            input.lines().forEach { line ->
                if (isFirst) isFirst = false else buf.append('\n')
                buf.append(line)
            }
        }
        else {
            var str = input.readLine()
            while (str != null) {
                if (isFirst) isFirst = false else buf.append('\n')
                buf.append(str)
                str = input.readLine()
            }
        }

        return buf.toString()
    }
    catch (e: IOException) {
        Log.e("SizIO","Error opening asset $asset_path")
    }
    finally {
        if (input != null) {
            try {
                input.close()
            }
            catch (e: IOException) {
                Log.e("SizIO","Error closing asset $asset_path")
            }

        }
    }

    return null
}

// ネットワーク接続確認
fun isNetworkEnabled(context: Context): Boolean {
    val cm = (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        context.getSystemService(ConnectivityManager::class.java)
    }
    else {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }) ?: return false

    @SuppressLint("MissingPermission")
    val info = cm.activeNetworkInfo
    return info != null && info.isConnected
}
