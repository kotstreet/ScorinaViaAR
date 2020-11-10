package com.example.one

import SimpleOrientationListener
import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.RotateDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.alphamovie.lib.AlphaMovieView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity :AppCompatActivity() {
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector
    private var alphaMovieView: AlphaMovieView? = null
    private lateinit var mOrientationListener: SimpleOrientationListener
//    private var oldOrientation:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alphaMovieView = findViewById(R.id.video_player)

        if(ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
            )!=PackageManager.PERMISSION_GRANTED){
            askForCameraPermision()
        }
        else{
            setUpControlls()
        }

        mOrientationListener = object : SimpleOrientationListener(this@MainActivity) {
            override fun onSimpleOrientationChanged(orientation: Int) {
                alphaMovieView!!.post {
                    rotateLayoutView!!.angle =
                        when(prevOrientation){
                            1-> 90
                            2-> 180
                            3-> 270
                            else -> 0
                        }
                }
            }
        }
        mOrientationListener.enable()
    }

    private fun setUpControlls(){
        detector = BarcodeDetector.Builder(this@MainActivity).build()
        cameraSource = CameraSource.Builder(this@MainActivity, detector)
            .setAutoFocusEnabled(true)
            .build()
        cameraSurfaceView.holder.addCallback(surgaceCallBack)
        detector.setProcessor(processor)
    }

    private fun askForCameraPermision(){
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                this@MainActivity.recreate()
//                setUpControlls()
            }else{
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val surgaceCallBack = object :SurfaceHolder.Callback{

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            cameraSource.stop()
        }

        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            try{
                if(ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.CAMERA
                    )!=PackageManager.PERMISSION_GRANTED){
                    return
                }
                cameraSource.start(surfaceHolder)
            }catch (exception: Exception){
                Toast.makeText(applicationContext, "Что то пошло не так", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private  val processor = object : Detector.Processor<Barcode>{
        override fun release() {}

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            if(detections!=null && detections.detectedItems.isNotEmpty()
                && !alphaMovieView!!.isPlaying){
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)

                if(code.displayValue.startsWith("Музей Гомельского государственного университета")) {
                    alphaMovieView!!.post() {
                        try {
                            alphaMovieView!!.visibility = View.VISIBLE
                            alphaMovieView!!.setVideoFromAssets("Videos/Dobro_pozalovat_v_scoriny.mp4")
//                            alphaMovieView!!.setLooping(false)

//                            alphaMovieView!!.setOnVideoEndedListener {
//                                alphaMovieView!!.stop()
//                                alphaMovieView!!.visibility = View.INVISIBLE
//                            }
                        } catch (e: Exception) {}
                    }
                }else if(code.displayValue.startsWith("Книга - это мать")) {
                        alphaMovieView!!.post() {
                            try {
                                alphaMovieView!!.visibility = View.VISIBLE
//                                alphaMovieView!!.setLooping(false)
                                alphaMovieView!!.setVideoFromAssets("Videos/Kniga_i_poznanie.mp4")
//                                alphaMovieView!!.setOnVideoEndedListener {
//                                    alphaMovieView!!.stop()
//                                    alphaMovieView!!.visibility = View.GONE
//                                }
                            } catch (e: Exception) {
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("message","onStart")

        alphaMovieView!!.setLooping(false)
        alphaMovieView!!.setOnVideoEndedListener {
            alphaMovieView!!.stop()
            alphaMovieView!!.visibility = View.GONE
        }
    }

    override fun onStop(){
        alphaMovieView!!.visibility = View.INVISIBLE
        super.onStop()
        Log.d("message","onStop")
    }

    override fun onResume() {
        super.onResume()
        alphaMovieView!!.onResume()
        Log.d("message","onResume")
    }

    override fun onPause() {
        super.onPause()
        alphaMovieView!!.onPause()
        Log.d("message","onPause")
    }

    fun play(view: View?) {
//        alphaMovieView!!.start()
        Log.d("message","play")
    }

    fun pause(view: View?) {
        alphaMovieView!!.pause()
        Log.d("message","pause")
    }

    fun stop(view: View?) {
        alphaMovieView!!.stop()
        Log.d("message","stop")
    }

}