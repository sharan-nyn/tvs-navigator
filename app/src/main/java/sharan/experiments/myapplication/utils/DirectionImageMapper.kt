package sharan.experiments.myapplication.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.jetbrains.annotations.NotNull
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.internal.Intrinsics


class DirectionImageMapper(private val context: Context) {
    private val newModels: MutableMap<String, Bitmap> = LinkedHashMap()

    init {
        loadModelsFromAssets()
    }

    private fun loadModelsFromAssets() {
        val assetManager: AssetManager = context.assets
        val newModelsDir = "new_models"
        val newModelsList = assetManager.list(newModelsDir)

        if (newModelsList != null) {
            for (model in newModelsList) {
                val modelName = toModelName(model)

                val modelPath = newModelsDir + File.separator + model
                val bitmap = getBitmapFromAsset(modelPath)

                if (bitmap != null) {
                    newModels[modelName] = bitmap
                }
            }
        }
    }

    private fun toModelName(modelName: String): String {
        return modelName.substring(1, modelName.lastIndexOf('$'))
    }

    private fun getBitmapFromAsset(str: String): Bitmap? {
        val bitmap: Bitmap? = try {
            val open: InputStream = context.assets.open(str)
            BitmapFactory.decodeStream(open)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
        if (bitmap == null) {
            Intrinsics.throwNpe()
        }
        return bitmap
    }

    private fun compareBitmaps(bitmap: Bitmap, bitmap2: Bitmap): Float {
        val iArr = IntArray(14400)
        val iArr2 = IntArray(14400)
        bitmap.getPixels(iArr, 0, 120, 0, 0, 120, 120)
        bitmap2.getPixels(iArr2, 0, 120, 0, 0, 120, 120)
        var i = 0
        for (i2 in 0..14399) {
            if (iArr[i2] != iArr2[i2]) {
                i++
            }
        }
        return i.toFloat() / 14400.toFloat() * 100.toFloat()
    }

    private fun resizeToDefaultSize(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 120, 120, false)
    }

    fun getDirectionFromImage(@NotNull bitmap: Bitmap): String {
        val resizeToDefaultSize: Bitmap = resizeToDefaultSize(bitmap)
        var num: Int? = null
        var str: String? = null

        for ((key, value) in this.newModels.entries) {
            val compareBitmaps2 = compareBitmaps(resizeToDefaultSize, resizeToDefaultSize(value))
            if (num == null || compareBitmaps2.compareTo(num.toInt().toFloat()) < 0) {
                num = Integer.valueOf(compareBitmaps2.toInt())
                str = key
            }
        }
        Log.d("DIRECTION_RESULT", "$str $num")
        if (str != null) {
            if (num!!.toInt().toDouble() <= 35.0) {
                return str
            }
        }
        return "UNKNOWN"
    }

}