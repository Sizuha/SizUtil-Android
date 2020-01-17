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
  // ...
}
```
