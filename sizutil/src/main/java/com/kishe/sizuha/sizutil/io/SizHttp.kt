package com.kishe.sizuha.sizutil.io

import android.content.ContentValues
import android.os.AsyncTask
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder


private const val DEFAULT_ENCODING = "UTF-8"

fun makeHttpQueryString(params: Map<String,String>): String {
    val buff = StringBuilder()
    var first = true

    for ((key, value) in params) {
        Log.d("SizHttp","[mapToString] key = $key / value: $value")

        try {
            if (first) first = false else buff.append("&")

            buff.append(URLEncoder.encode(key, DEFAULT_ENCODING))
            buff.append("=")
            buff.append(URLEncoder.encode(value, DEFAULT_ENCODING))
        }
        catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    return buff.toString()
}

fun makeHttpQueryString(params: ContentValues): String {
    val buff = StringBuilder()
    var first = true

    for (key in params.keySet()) {
        val value = params.getAsString(key)

        try {
            if (first) first = false else buff.append("&")

            buff.append(URLEncoder.encode(key, DEFAULT_ENCODING))
            buff.append("=")
            buff.append(URLEncoder.encode(value, DEFAULT_ENCODING))
        }
        catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    return buff.toString()
}

open class SizHttp(var baseUrl: String = "") {

    enum class RequestMethod constructor(private var method: Int) {
        GET(0),
        POST(1),
        PUT(2),
        DELETE(3);

        val isNeedOutputParamMethod: Boolean
            get() = method != 0 && method != 3

        override fun toString(): String {
            return when (method) {
                1 -> "POST"
                2 -> "PUT"
                3 -> "DELETE"
                else -> "GET"
            }
        }
    }

    object StatusCodes {
        // Success: 2xx
        val OK = 200
        val CREATED = 201
        val NO_CONTENT = 204

        // Client Errors: 4xx
        val BAD_REQUEST = 400
        val UNAUTHORIZED = 401
        val PAYMENT_REQUIRED = 402
        val FORBIDDEN = 403
        val NOT_FOUND = 404
        val TIMEOUT = 408

        // Server Errors: 5xx
        val SERVER_ERROR = 500

        // nginx
        val NO_RESPONSE = 444

        // Library Errors: 1xxx
        val JSON_ERROR = 1400
        val CONNECT_ERROR = 444

        fun isSuccess(code: Int) = code in StatusCodes.OK..StatusCodes.NO_CONTENT
    }

    open class Response(val status: Int, val content: String? = null) {
        var asJsonDictionary: JSONObject? = null
            get() {
                return try {
                    JSONObject(content)
                }
                catch (e: JSONException) {
                    null
                }
            }
            private set

        var asJsonArray: JSONArray? = null
            get() {
                return try {
                    JSONArray(content)
                }
                catch (e: JSONException) {
                    null
                }
            }
            private set

        open val isSuccess: Boolean
            get() = StatusCodes.isSuccess(status)
    }

    var timeoutMs: Int = 60000

    fun request(url: String = "", params: ContentValues? = null, method: RequestMethod = RequestMethod.GET): Response {
        return request(url, if (params == null) null else makeHttpQueryString(params), method)
    }
    fun request(url: String = "", params: Map<String,String>, method: RequestMethod = RequestMethod.GET): Response {
        return request(url, makeHttpQueryString(params), method)
    }

    fun request(url: String = "", paramStr: String?, method: RequestMethod = RequestMethod.GET): Response {
        val notEmptyParam = paramStr != null && paramStr.isNotEmpty()
        var urlConn: HttpURLConnection? = null

        return try {
            val urlObj = if (!method.isNeedOutputParamMethod && notEmptyParam) {
                URL("$baseUrl$url?$paramStr")
            }
            else {
                URL("$baseUrl$url")
            }

            urlConn = urlObj.openConnection() as HttpURLConnection
            urlConn.readTimeout = timeoutMs
            urlConn.connectTimeout = timeoutMs
            urlConn.doInput = true
            urlConn.doOutput = method.isNeedOutputParamMethod
            urlConn.requestMethod = method.toString()

            if (notEmptyParam && method.isNeedOutputParamMethod) {
                val os = urlConn.outputStream
                val writer = BufferedWriter(OutputStreamWriter(os, DEFAULT_ENCODING))
                writer.write(paramStr)
                writer.flush()
                writer.close()
                os.close()
            }

            urlConn.connect()

            // HTTPレスポンスコード
            val status = urlConn.responseCode
            val content = if (StatusCodes.isSuccess(status)) {
                // 通信に成功した
                // テキストを取得する
                readAllText(urlConn.inputStream)!!
            }
            else {
                readAllText(urlConn.errorStream)!!
            }

            Response(status, content)
        }
        catch (e: MalformedURLException) {
            e.printStackTrace()
            Response(StatusCodes.BAD_REQUEST)
        }
        catch (e: IOException) {
            e.printStackTrace()
            Response(StatusCodes.CONNECT_ERROR)
        }
        finally {
            urlConn?.disconnect()
        }
    }

}

open class SizHttpAsync: AsyncTask<String, Void, SizHttp.Response>() {
    private val http = SizHttp()
    var baseUrl: String
        get() = http.baseUrl
        set(value){ http.baseUrl = value }

    var url: String = ""

    var timeoutMs: Int
        get() = http.timeoutMs
        set(value) { http.timeoutMs = value }

    var method: SizHttp.RequestMethod = SizHttp.RequestMethod.GET

    protected var paramStr: String? = null
    fun setParams(params: ContentValues) {
        paramStr = makeHttpQueryString(params)
    }
    fun setParams(params: Map<String,String>) {
        paramStr = makeHttpQueryString(params)
    }

    var onResponse: ((SizHttp.Response)->Void)? = null

    fun request(url: String = "", onResponse: ((SizHttp.Response)->Void)? = null) {
        if (url.isNotEmpty()) this.url = url
        if (onResponse != null) this.onResponse = onResponse

        super.execute()
    }

    override fun doInBackground(vararg params: String?): SizHttp.Response {
        return http.request(url, paramStr, method)
    }

    override fun onPostExecute(result: SizHttp.Response?) {
        onResponse?.invoke(result!!)
    }
}


fun httpGet(url: String, params: ContentValues? = null): SizHttp.Response {
    return SizHttp().request(url, params, SizHttp.RequestMethod.GET)
}
fun httpGet(url: String, params: Map<String,String>): SizHttp.Response {
    return SizHttp().request(url, params, SizHttp.RequestMethod.GET)
}

fun httpPost(url: String, params: ContentValues): SizHttp.Response {
    return SizHttp().request(url, params, SizHttp.RequestMethod.POST)
}
fun httpPost(url: String, params: Map<String,String>): SizHttp.Response {
    return SizHttp().request(url, params, SizHttp.RequestMethod.POST)
}

fun httpGetAsync(url: String, params: ContentValues? = null, onResponse: ((SizHttp.Response)->Void)? = null) {
    httpRequestAsync(SizHttp.RequestMethod.GET, url, params, onResponse)
}
fun httpGetAsync(url: String, params: Map<String,String>, onResponse: ((SizHttp.Response)->Void)? = null) {
    httpRequestAsync(SizHttp.RequestMethod.GET, url, params, onResponse)
}

fun httpPostAsync(url: String, params: ContentValues, onResponse: ((SizHttp.Response)->Void)? = null) {
    httpRequestAsync(SizHttp.RequestMethod.POST, url, params, onResponse)
}
fun httpPostAsync(url: String, params: Map<String,String>, onResponse: ((SizHttp.Response)->Void)? = null) {
    httpRequestAsync(SizHttp.RequestMethod.POST, url, params, onResponse)
}


fun httpRequestAsync(
    method: SizHttp.RequestMethod,
    url: String,
    params: ContentValues?,
    onResponse: ((SizHttp.Response)->Void)?
) {
    SizHttpAsync().apply {
        this.method = method
        if (params != null) setParams(params)
        request(url, onResponse)
    }.execute()
}

private fun httpRequestAsync(
    method: SizHttp.RequestMethod,
    url: String,
    params: Map<String,String>,
    onResponse: ((SizHttp.Response)->Void)?
) {
    SizHttpAsync().apply {
        this.method = method
        setParams(params)
        request(url, onResponse)
    }.execute()
}
