package com.example.one

//import android.R

import android.content.Intent
import android.content.res.Configuration
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alphamovie.lib.AlphaMovieView
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity :AppCompatActivity(),
    SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback,
    Camera.PreviewCallback, Camera.AutoFocusCallback {

    private var isMovie = false
    private var alphaMovieView: AlphaMovieView? = null
    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var preview: SurfaceView? = null
    private val shotBtn: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alphaMovieView = findViewById(R.id.video_player)
        alphaMovieView!!.setVideoFromAssets("blender_V20001-0168.mp4")
        alphaMovieView!!.setOnVideoEndedListener { Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show() }

        // если хотим, чтобы приложение постоянно имело портретную ориентацию
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // если хотим, чтобы приложение было полноэкранным
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // и без заголовка
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // наше SurfaceView имеет имя SurfaceViewCamera
        preview = findViewById(R.id.SurfaceViewCamera);

        surfaceHolder = preview!!.holder;
        surfaceHolder!!.addCallback(this);
        surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        scanQRCode()
    }

//    override fun onResume() {
//        super.onResume()
//        alphaMovieView!!.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        alphaMovieView!!.onPause()
//    }

    private fun scanQRCode(){
        val integrator = IntentIntegrator(this).apply {
            captureActivity = CaptureActivity::class.java
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt("Scanning Code")
        }
        integrator.initiateScan()
    }

    // Get the results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            else {
                if(result.contents == "Музей Гомельского государственного университета имени Франциска Скорины")
                {
                    isMovie = true
                    resume()
                }else {
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                    scanQRCode()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isMovie)
            resume()
    }

    private fun resume() {
        alphaMovieView!!.onResume()
        camera = Camera.open()
    }

    override fun onPause() {
        super.onPause()
        if (isMovie)
            pause()
    }

    private fun pause() {
        alphaMovieView!!.onPause()
        if (camera != null) {
            camera!!.setPreviewCallback(null)
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
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

}