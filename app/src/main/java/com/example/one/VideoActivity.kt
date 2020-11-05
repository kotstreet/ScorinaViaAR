package com.example.one

import android.content.res.Configuration
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.alphamovie.lib.AlphaMovieView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class VideoActivity : AppCompatActivity(),
    SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback,
    Camera.PreviewCallback, Camera.AutoFocusCallback {

    private var alphaMovieView: AlphaMovieView? = null
    private var preview: SurfaceView? = null
    private var camera: Camera? = null
    private val shotBtn: Button? = null
    private var surfaceHolder: SurfaceHolder? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        alphaMovieView = findViewById(R.id.video_player)
        alphaMovieView!!.setVideoFromAssets("blender_V20001-0168.mp4")
//        alphaMovieView!!.setOnVideoEndedListener { Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show() }

        // если хотим, чтобы приложение постоянно имело портретную ориентацию
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // если хотим, чтобы приложение было полноэкранным
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // и без заголовка
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // наше SurfaceView имеет имя SurfaceViewCamera
//        preview = findViewById(R.id.SurfaceViewCamera);

        surfaceHolder = preview!!.holder;
        surfaceHolder!!.addCallback(this);
        surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        Toast.makeText(this, "ddd", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        alphaMovieView?.onResume()
        camera = Camera.open()
    }

    override fun onPause() {
        super.onPause()
        alphaMovieView?.onPause()
        if (camera != null) {
            camera!!.setPreviewCallback(null)
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
    }

    override fun onStop() {
        super.onStop()
        alphaMovieView?.stop()
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera!!.setPreviewDisplay(holder)
            camera!!.setPreviewCallback(this)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val previewSize = camera!!.parameters.previewSize
        val aspect = previewSize.width.toFloat() / previewSize.height
        val previewSurfaceWidth = preview!!.width
        val previewSurfaceHeight = preview!!.height
        val lp = preview!!.layoutParams

        // здесь корректируем размер отображаемого preview, чтобы не было искажений
        if (this.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            // портретный вид
            camera!!.setDisplayOrientation(90)
            lp.height = previewSurfaceHeight + 100
            lp.width = ((previewSurfaceHeight + 100)/ aspect).toInt()
        } else {
            // ландшафтный
            camera!!.setDisplayOrientation(0)
            lp.width = previewSurfaceWidth
            lp.height = (previewSurfaceWidth / aspect).toInt()
        }
        preview!!.layoutParams = lp
        camera!!.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}
    override fun onClick(v: View) {
        if (v === shotBtn) {
            // либо делаем снимок непосредственно здесь
            // 	либо включаем обработчик автофокуса

            //camera.takePicture(null, null, null, this);
            camera!!.autoFocus(this)
        }
    }

    override fun onPictureTaken(paramArrayOfByte: ByteArray, paramCamera: Camera) {
        // сохраняем полученные jpg в папке /sdcard/CameraExample/
        // имя файла - System.currentTimeMillis()
        try {
            val saveDir = File("/sdcard/CameraExample/")
            if (!saveDir.exists()) {
                saveDir.mkdirs()
            }
            val os = FileOutputStream(String.format("/sdcard/CameraExample/%d.jpg", System.currentTimeMillis()))
            os.write(paramArrayOfByte)
            os.close()
        } catch (e: Exception) {
        }

        // после того, как снимок сделан, показ превью отключается. необходимо включить его
        paramCamera.startPreview()
    }

    override fun onAutoFocus(paramBoolean: Boolean, paramCamera: Camera) {
        if (paramBoolean) {
            // если удалось сфокусироваться, делаем снимок
            paramCamera.takePicture(null, null, null, this)
        }
    }

    override fun onPreviewFrame(paramArrayOfByte: ByteArray, paramCamera: Camera) {
        // здесь можно обрабатывать изображение, показываемое в preview
    }

    fun back(view: View) {
        onBackPressed()
    }
}