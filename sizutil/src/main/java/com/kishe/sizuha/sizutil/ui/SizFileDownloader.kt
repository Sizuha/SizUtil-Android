package com.kishe.sizuha.sizutil.ui

import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import com.kishe.sizuha.sizutil.io.getFilenameFormUrl
import com.kishe.sizuha.sizutil.io.makeDirs
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL


/**
 * URLからファイルをダウンロードする
 *
 *
 * param1: URL
 * param2: out file (option)
 *
 */
class FileDownloader : AsyncTask<String, Int, Boolean>() {

    private val BUFFER_SIZE = 4096

    private var storagePath: String? = ""
    private var file_size: Int = 0
    private var limit_filesize: Long = 0
    private var progressDlg: ProgressDialog? = null
    private var downloadEventRecv: IOnDownloadEvent? = null

    var overwrite = true

    private var out_filename: String? = null
    private var out_filenameWithPath: String? = null
    fun setStagePath(path: String) {
        storagePath = path
    }

    fun setLimitDownloadSize(size_bytes: Long) {
        limit_filesize = size_bytes
    }

    fun setProgressDialog(dlg: ProgressDialog) {
        progressDlg = dlg
    }


    interface IOnDownloadEvent {
        fun onFileDownProgress(file: String?, curr: Int, max: Int)
        fun onFileDownloadEnd(filenameWithPath: String?, succeed: Boolean, size: Int)
    }

    fun setOnDownloadEvent(listener: IOnDownloadEvent) {
        downloadEventRecv = listener
    }

    fun checkFileExist(downurl: String): Boolean {
        val st_file = storagePath + (if (storagePath!!.endsWith("/")) "" else "/") + getFilenameFormUrl(downurl)
        val outFile = File(st_file)

        return outFile.exists()
    }

    override fun onPreExecute() {
        super.onPreExecute()

        if (progressDlg != null) {
            progressDlg!!.show()
        }
    }

    override fun doInBackground(vararg params: String): Boolean? {
        val url_str = params[0]
        if (storagePath == null) {
            storagePath = ""
        }

        Log.i("SizFileDownloader","download from: $url_str")

        // Escape early if cancel() is called
        if (isCancelled) {
            Log.i("SizFileDownloader","download cancelled")
            return false
        }

        var outFile: File? = null

        try {
            val url = URL(url_str)

            out_filename = null
            if (params.size >= 2) {
                out_filename = params[1]

            }

            if (out_filename == null) {
                out_filename = getFilenameFormUrl(url_str)
            }

            out_filenameWithPath = storagePath +
                    (if (storagePath!!.isEmpty() || storagePath!!.endsWith("/")) "" else "/") + out_filename
            Log.d("SizFileDownloader","download to: " + out_filenameWithPath!!)

            outFile = File(out_filenameWithPath)
            if (!overwrite && outFile!!.exists()) {
                return true
            }

            makeDirs(out_filenameWithPath!!)

            val connection = url.openConnection()
            connection.connect()
            file_size = connection.getContentLength()
            Log.d("SizFileDownloader","download target file size: $file_size")

            if (limit_filesize > 0 && file_size > limit_filesize) {
                file_size = 0
                return false
            }

            val input = BufferedInputStream(url.openStream(), BUFFER_SIZE)
            val output = FileOutputStream(outFile)

            val data = ByteArray(BUFFER_SIZE)


            var read_bytes: Int
            var progress_size = 0
            var cancelled = false

            Log.i("SizFileDownloader","start download --------")

            read_bytes = 0
            do {
                read_bytes = input.read(data)
                if (read_bytes != -1) {
                    cancelled = isCancelled
                    if (cancelled) break

                    output.write(data, 0, read_bytes)
                    progress_size += read_bytes

                    if (downloadEventRecv != null) {
                        publishProgress(progress_size, file_size)
                    }
                }
            } while (read_bytes != -1)

            Log.i("SizFileDownloader","end download --------")


            if (!cancelled) {
                output.flush()
            }

            output.close()
            input.close()

            if (cancelled) {
                Log.i("SizFileDownloader","download cancelled")
                return false
            }
        }
        catch (e: MalformedURLException) {
            return false
        }
        catch (e: IOException) {
            outFile!!.delete()
            return false
        }

        return true
    }

    override fun onProgressUpdate(vararg values: Int?) {
        val progress_size = values[0]
        val file_size = values[1]

        if (progressDlg != null) {
            progressDlg!!.max = file_size!!
            progressDlg!!.progress = progress_size!!
        }

        downloadEventRecv!!.onFileDownProgress(out_filename, progress_size!!, file_size!!)
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(ok: Boolean?) {
        if (progressDlg != null) {
            progressDlg!!.dismiss()
        }

        if (downloadEventRecv != null) {
            downloadEventRecv!!.onFileDownloadEnd(out_filenameWithPath, ok!!, file_size)
        }
    }

}