package com.example.one

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
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
                setUpControlls()
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
            if(detections!=null && detections.detectedItems.isNotEmpty()){
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)

                if(code.displayValue.startsWith("Музей")) {
                    alphaMovieView!!.post() {
                        try {
                            alphaMovieView!!.visibility = View.VISIBLE
                            alphaMovieView!!.setVideoFromAssets("blender_V20001-0168cutter.mp4")
                            alphaMovieView!!.setOnVideoEndedListener {
                                alphaMovieView!!.visibility = View.INVISIBLE
                            }
                        } catch (e: Exception) {}
                    }
                }else {
//                    textScanResult.post(){ textScanResult.text = code.displayValue }
                }
            }else{
//                textScanResult.post(){ textScanResult.text = "0" }
            }
        }
    }
}