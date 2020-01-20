package com.kishe.sizuha.sizutil

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.kishe.sizuha.sizutil.io.SizHttp
import com.kishe.sizuha.sizutil.io.SizJson
import com.kishe.sizuha.sizutil.io.httpRequestAsync

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.kishe.sizuha.sizutil", appContext.packageName)

    }

    @Test
    fun sizHttpTest() {
        httpRequestAsync(SizHttp.RequestMethod.GET, "http://www.google.com/", null) { response: SizHttp.Response ->
            response.status

            val arr = response.asJsonArray
            val dic = response.asJsonDictionary

            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val intVal: Int? = arr.getInt(i)
                    val dic = arr.getJSONObject(i)
                    val subArr = arr.getJSONArray(i)
                }
            }

            if (dic != null) {
                val json = SizJson(dic)
                val strVal: String? = json.getString("key1")
                val intVal: Int? = json.getInt("key2")
            }
        }
    }
}
