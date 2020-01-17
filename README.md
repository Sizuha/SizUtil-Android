# SizUtil-Android
Utility Library for Android(Kotlin)

開発中... Now Developing

注意：開発中のライブラリーなので、まだ充分なテストができていません。

## build.gradle (App)
~~~
dependencies {
  implementation 'com.kishe.sizuha.sizutil:sizutil:0.1.1@aar'
  // . . .
}
~~~

## HTTP

```kotlin

val params = ContentValues().apply { ... }
httpRequestAsync(SizHttp.RequestMethod.POST, "http://...", params) { response: SizHttp.Response ->
  // Arrayの場合
  val arr = response.asJsonArray
  if (arr != null) {
    for (i in 0 until arr.length()) {
      val intVal = arr.getInt(i)
      val dic = arr.getJSONObject(i)
      val subArr = arr.getJSONArray(i)
    }
  }

  // Dictionaryの場合
  val dic = response.asJsonDictionary
  if (dic != null) {
    val json = SizJson(dic)
    val strVal: String? = json.getString("key1")
    val intVal: Int? = json.getInt("key2")
  }
}
```
