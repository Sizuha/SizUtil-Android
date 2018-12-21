package com.kishe.sizuha.sizutil.image

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.os.Build
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Log
import android.widget.ImageView
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

fun changeColor(drawable: Drawable, color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP): Drawable {
    val d = drawable.mutate()
    d.setColorFilter(color, mode)
    return d
}

fun loadBitmapFromAssets(context: Context, path: String): Bitmap? {
    var stream: InputStream? = null
    try {
        stream = context.assets.open(path)
        return BitmapFactory.decodeStream(stream)
    }
    catch (ignored: Exception) {
    }
    finally {
        try {
            stream?.close()
        }
        catch (ignored: Exception) { }
    }

    return null
}

fun loadDrawableFromAssets(context: Context, path: String): Drawable? {
    //Logger.d("init a Drawable: " + path);

    var stream: InputStream? = null
    try {
        stream = context.assets.open(path)
        return Drawable.createFromStream(stream, null)
    }
    catch (ignored: Exception) {
    }
    finally {
        try {
            stream?.close()
        } catch (ignored: Exception) { }
    }

    return null
}

fun scaleBitmap(`is`: InputStream, os: OutputStream, newWidth: Float, newHeight: Float) {
    val src_bitmap = BitmapFactory.decodeStream(`is`)
    val scaled_bitmap = scaleBitmap(src_bitmap, newWidth, newHeight)
    src_bitmap.recycle()

    scaled_bitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, os)
    scaled_bitmap.recycle()
}

fun scaleBitmap(bitmapToScale: Bitmap?, newWidth: Float, newHeight: Float): Bitmap? {
    if (bitmapToScale == null)
        return null
    //get the original width and height
    val width = bitmapToScale.width
    val height = bitmapToScale.height
    // create a matrix for the manipulation
    val matrix = Matrix()

    // resize the bit map
    matrix.postScale(newWidth / width, newHeight / height)

    // recreate the new Bitmap and set it back
    return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.width, bitmapToScale.height, matrix, true)
}


fun getCroppedCircleBitmap(bitmap: Bitmap?): Bitmap? {
    if (bitmap == null) return null

    val img_height = bitmap.height
    val img_width = bitmap.width
    val min_size = if (img_height > img_width) img_width else img_height


    val output = Bitmap.createBitmap(min_size, min_size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle((min_size / 2).toFloat(), (min_size / 2).toFloat(), (min_size / 2).toFloat(), paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, Rect(0, 0, min_size, min_size), paint)
    return output
}

fun setCircleCropped(img: ImageView, imageBitmap: Bitmap) {
    if (Build.VERSION.SDK_INT >= 22) {
        //get bitmap of the image
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(img.context.resources, imageBitmap)

        //setting radius
        roundedBitmapDrawable.isCircular = true
        roundedBitmapDrawable.setAntiAlias(true)
        img.setImageDrawable(roundedBitmapDrawable)
    } else {
        img.setImageBitmap(getCroppedCircleBitmap(imageBitmap))
    }
}

fun createCircleCroppedDrawable(c: Context, filepath: String): Drawable {
    if (Build.VERSION.SDK_INT >= 22) {
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(c.resources, filepath)
        roundedBitmapDrawable.isCircular = true
        roundedBitmapDrawable.setAntiAlias(true)
        return roundedBitmapDrawable
    }

    val bmp = BitmapFactory.decodeFile(filepath)
    return BitmapDrawable(c.resources, getCroppedCircleBitmap(bmp))
}

fun getDrawableFromResource(context: Context, res_id: Int): Drawable? {
    val drawable: Drawable?

    if (android.os.Build.VERSION.SDK_INT < 21) {
        drawable = context.resources.getDrawable(res_id)
    } else {
        drawable = context.getDrawable(res_id)
    }

    return drawable
}

fun recycleBitmap(imageView: ImageView?) {
    if (imageView == null) return

    val drawable = imageView.drawable
    imageView.setImageDrawable(null)
    recycleBitmap(drawable)
}

fun recycleBitmap(drawable: Drawable?) {
    if (drawable != null && drawable is BitmapDrawable) {
        val bitmapDrawable = drawable as BitmapDrawable?
        val bitmap = bitmapDrawable!!.bitmap

        if (!bitmap.isRecycled) {
            try {
                bitmap.recycle()
            } catch (e: Exception) {
            }

        }
    }
}

fun getExifRotateDegree(jepgFilePath: String): Int {
    var degree: Int
    try {
        val exifInterface = ExifInterface(jepgFilePath)
        val orientation =
            exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        Log.d("SizImage","JPEG path: $jepgFilePath")
        Log.d("SizImage","JPEG EXIF orientation: $orientation")

        degree = exifOrientationToDegree(orientation)
    }
    catch (e: IOException) {
        degree = 0
        e.printStackTrace()
    }

    return degree
}

fun exifOrientationToDegree(orientation: Int): Int {
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}

fun getExifOrientation(jpegFilePath: String): Int {
    try {
        val exifInterface = ExifInterface(jpegFilePath)
        val orientation =
            exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        Log.d("SizImage", "JPEG path: $jpegFilePath")
        Log.d("SizImage", "JPEG EXIF orientation: $orientation")
        return orientation
    }
    catch (e: IOException) {
        e.printStackTrace()
    }

    return 0
}

class Vector2i {
    var x: Int = 0
    var y: Int = 0

    constructor() {}
    constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    companion object {

        fun parse(from: String?): Vector2i? {
            if (from != null && !from.isEmpty()) {
                val result = Vector2i()

                val parts = from.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size > 1) {
                    result.x = Integer.parseInt(parts[0])
                    result.y = Integer.parseInt(parts[1])

                    return result
                }
            }

            return null
        }
    }
}

class Vector2f {
    var x: Float = 0.toFloat()
    var y: Float = 0.toFloat()

    constructor() {}
    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}


fun getImageSize(filepath: String): Vector2i {
    return getImageSize(filepath, true)
}

fun getImageSize(filepath: String, applyOrientation: Boolean): Vector2i {
    val result = Vector2i()
    var orientation = 0

    try {
        val exifInterface = ExifInterface(filepath)
        orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        result.x = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1)
        result.y = exifInterface.getAttributeInt("ImageHeight", -1)

        if (result.x <= 0) {
            result.x = exifInterface.getAttributeInt("ExifImageWidth", -1)
        }
        if (result.y <= 0) {
            result.y = exifInterface.getAttributeInt("ExifImageHeight", -1)
        }
    }
    catch (e: IOException) {
        e.printStackTrace()

        result.x = -1
        result.y = -1
    }

    if (result.x <= 0 || result.y <= 0) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeFile(filepath, options)
        result.x = options.outWidth
        result.y = options.outHeight
    }

    if (applyOrientation)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_ROTATE_270 -> {
                val backupX = result.x
                result.x = result.y
                result.y = backupX
            }
        }

    return result
}
