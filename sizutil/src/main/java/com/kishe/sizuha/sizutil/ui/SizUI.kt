package com.kishe.sizuha.sizutil.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.kishe.sizuha.sizutil.io.getContentUri
import java.io.File
import java.util.ArrayList


fun openUrl(from: Activity, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)

    from.startActivity(intent)
}

fun getColor(c: Context, color_res_id: Int): Int {
    return ContextCompat.getColor(c, color_res_id)
}

fun openImageBrowser(from: Activity, request_code: Int) {
    val chooseIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    chooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
    chooseIntent.type = MediaStore.Images.Media.CONTENT_TYPE
    from.startActivityForResult(chooseIntent, request_code)
}

fun openImageCapture(from: Activity, request_code: Int, output: String) {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    if (output.isNotEmpty()) {
        val uri = getContentUri(from, output)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
    }

    if (takePictureIntent.resolveActivity(from.packageManager) != null) {
        from.startActivityForResult(takePictureIntent, request_code)
    }
}


fun openVideoCapture(from: Activity, request_code: Int, output: String) {
    val takePictureIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

    if (output.isNotEmpty()) {
        val uri = getContentUri(from, output)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
    }

    //        takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, );
    //        takePictureIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, );
    //        takePictureIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, );

    if (takePictureIntent.resolveActivity(from.packageManager) != null) {
        from.startActivityForResult(takePictureIntent, request_code)
    }
}

fun getFilepathFromMediaStore(context: Context, data: Intent): String? {
    val uri = data.data

    val filepath = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri!!, filepath, null, null, null)
    cursor?.use {
        it.moveToFirst()

        val columnIndex = it.getColumnIndex(filepath[0])
        val filePath = it.getString(columnIndex)

        Log.i("SizUI","data: " + data.data!!.toString())
        Log.i("SizUI","image file path: $filePath")

        return filePath
    }
    return null
}

fun getRealPathFromURI_API19(context: Context, uri: Uri): String? {
    if ("com.android.providers.downloads.documents" == uri.authority) {
        // ダウンロードからの場合
        val id = DocumentsContract.getDocumentId(uri)
        val docUri =
            ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))

        context.contentResolver.query(docUri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use {
            if (it.moveToFirst()) {
                val file = File(it.getString(0))
                return file.absolutePath
            }
        } ?: return null
    }

    var filePath = ""
    val wholeID: String
    val id: String

    try {
        wholeID = DocumentsContract.getDocumentId(uri)
        Log.d("SizUI","wholeID: $wholeID")

        val result = wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        id = result[1]
    }
    catch (e: Exception) {
        Log.d("SizUI",e.toString())
        return null
    }

    val column = arrayOf(MediaStore.Images.Media.DATA)

    // where user_idx is equal to
    val sel = MediaStore.Images.Media._ID + "=?"

    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        column, sel, arrayOf(id), null
    )?.use {
        val columnIndex = it.getColumnIndex(column[0])
        if (it.moveToFirst()) {
            filePath = it.getString(columnIndex)
        }
        return filePath
    } ?: return null
}

fun getFileNameFromImagePickResult(c: Context, data: Intent): List<String> {
    val result: MutableList<String>

    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val selectedImages = data.clipData

    if (selectedImages == null) {
        result = ArrayList(1)
        val cur = c.contentResolver.query(data.data!!, projection, null, null, null)
        if (cur != null) {
            if (cur.moveToFirst()) {
                val filepath = cur.getString(0)
                Log.d("SizUI","+ ImagePick: $filepath")

                result.add(filepath)
            }
            cur.close()
        }
    }
    else {
        result = ArrayList(selectedImages.itemCount)

        for (i in 0 until selectedImages.itemCount) {
            val item = selectedImages.getItemAt(i)
            val cur = c.contentResolver.query(item.uri, projection, null, null, null)
            if (cur != null) {
                if (cur.moveToFirst()) {
                    val filepath = cur.getString(0)
                    Log.d("SizUI","+ ImagePick: $filepath")

                    result.add(filepath)
                }
                cur.close()
            }
        }
    }

    return result
}

fun makeUnderlineText(v: TextView) {
    v.paintFlags = v.paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun removeUnderline(v: TextView) {
    v.paintFlags = v.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
}

fun makeUnderlineText(b: Button) {
    b.paintFlags = b.paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun removeUnderline(b: Button) {
    b.paintFlags = b.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
}

fun hideKeyboard(a: Activity) {
    val view = a.currentFocus
    if (view != null) {
        val imm = a.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}


// Find the child view that was touched (perform a hit test)
fun getTouchedItemInListView(lv: ListView, motionEvent: MotionEvent): View? {
    var result: View? = null

    val rect = Rect()
    val childCount = lv.childCount
    val listViewCoords = IntArray(2)
    lv.getLocationOnScreen(listViewCoords)
    val x = motionEvent.rawX.toInt() - listViewCoords[0]
    val y = motionEvent.rawY.toInt() - listViewCoords[1]

    var child: View
    for (i in 0 until childCount) {
        child = lv.getChildAt(i)
        child.getHitRect(rect)
        if (rect.contains(x, y)) {
            result = child // This is your down view
            break
        }
    }

    return result
}

fun setKeepScreenOn(a: Activity) {
    a.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}


//------ Alert Dialog
/**
 *
 * エラーや警告、お知らせなどに使うダイアログを表示。
 *
 */
object SizAlert {

    /**
     * エラーメッセージを表示
     * @param c
     * @param message
     * @param onClick
     * @return
     */
    fun showError(c: Context, message: String, onClick: DialogInterface.OnClickListener?): AlertDialog {
        val dlg = AlertDialog.Builder(c)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok, onClick)
            .setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onClick?.onClick(dialog, 0)
                    return@OnKeyListener true
                }

                false
            })
            .create()

        dlg.setCanceledOnTouchOutside(false)
        dlg.show()
        return dlg
    }

    /**
     * ユーザーに確認を得る
     * @param c
     * @param message
     * @param onOk
     * @return
     */
    fun confirm(c: Context, message: String, onOk: DialogInterface.OnClickListener): AlertDialog {
        val dlg = AlertDialog.Builder(c)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok, onOk)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dlg.setCanceledOnTouchOutside(false)
        dlg.show()
        return dlg
    }

    fun popup(c: Context, message: String, onOk: DialogInterface.OnClickListener?): AlertDialog {
        val dlg = AlertDialog.Builder(c)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(android.R.string.ok, onOk)
            .setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onOk?.onClick(dialog, 0)
                    return@OnKeyListener true
                }

                false
            })
            .create()

        dlg.setCanceledOnTouchOutside(false)
        dlg.show()
        return dlg
    }

}
