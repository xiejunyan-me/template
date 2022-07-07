package com.android.aschat.util

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import java.io.*


object ImageUtil {

    /**
     * 把图片uri变成bitmap
     */
    fun decodeUriToBitmap(context: Context, sendUri: Uri): Bitmap? {
        var getBitmap: Bitmap? = null
        try {
            val imageStream: InputStream
            try {
                imageStream = context.contentResolver.openInputStream(sendUri)!!
                getBitmap = BitmapFactory.decodeStream(imageStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return getBitmap
    }

    /**
     * 得到资源文件中图片的Uri
     * @param context 上下文对象
     * @param id 资源id
     * @return Uri
     */
    fun getUriFromDrawableRes(context: Context, id: Int): Uri? {
        val resources = context.resources
        val path = (ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(id) + "/"
                + resources.getResourceTypeName(id) + "/"
                + resources.getResourceEntryName(id))
        return Uri.parse(path)
    }

    /**
     * 根据图片路径压缩图片并返回压缩后图片路径，包含比例压缩，质量压缩，旋转问题解决
     * @param path
     * @return
     */
    fun compress(path: String?): String? {
        var bm: Bitmap = getimage(path) ?: return null //压缩照片
        val degree = readPictureDegree(path) //读取照片旋转角度
        if (degree > 0) {
            bm = rotateBitmap(bm, degree) //旋转照片
        }
        val file = (Environment.getExternalStorageDirectory().absolutePath
                + File.separator + "temp" + File.separator)
        val f = File(file)
        if (!f.exists()) {
            f.mkdirs()
        }
        val picName = System.currentTimeMillis().toString() + ".jpg"
        val resultFilePath = file + picName
        try {
            val out = FileOutputStream(resultFilePath)
            bm!!.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultFilePath
    }

    /**
     * 图片按比例大小压缩方法（根据路径获取图片并压缩）：
     * @param srcPath
     * @return
     */
    private fun getimage(srcPath: String?): Bitmap? {
        val newOpts = BitmapFactory.Options()
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeFile(srcPath, newOpts) //此时返回bm为空
        newOpts.inJustDecodeBounds = false
        val w = newOpts.outWidth
        val h = newOpts.outHeight
        val hh = 800f //这里设置高度为800f
        val ww = 480f //这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        var be = 1 //be=1表示不缩放
        if (w > h && w > ww) { //如果宽度大的话根据宽度固定大小缩放
            be = (newOpts.outWidth / ww).toInt()
        } else if (w < h && h > hh) { //如果高度高的话根据宽度固定大小缩放
            be = (newOpts.outHeight / hh).toInt()
        }
        if (be <= 0) be = 1
        newOpts.inSampleSize = be //设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts)
        return compressImage(bitmap) //压缩好比例大小后再进行质量压缩
    }

    /**
     * 质量压缩
     * @param image
     * @return
     */
    private fun compressImage(image: Bitmap): Bitmap? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 99 //这个只能是0-100之间不包括0和100不然会报异常
        while (baos.toByteArray().size / 1024 > 100 && options > 0) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset() //重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos) //这里压缩options%，把压缩后的数据存放到baos中
            options -= 10 //每次都减少10
        }
        val isBm =
            ByteArrayInputStream(baos.toByteArray()) //把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null)
    }

    /**
     * 读取照片的旋转角度
     * @param path
     * @return
     */
    private fun readPictureDegree(path: String?): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    /**
     * 旋转照片
     * @param bitmap
     * @param rotate
     * @return
     */
    private fun rotateBitmap(bitmap: Bitmap, rotate: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true)
    }
}