package com.example.one

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureActivity


class MainActivity :AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanQRCode()
    }

    override fun onStart() {
        super.onStart()
    }

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
            if (result.contents == null){

//                return
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
//                onBackPressed()
            }
            else {
                if(result.contents.startsWith("Музей")) {
                    val intent = Intent(this, VideoActivity::class.java)
                    startActivity(intent)
                    return
                }
                else{
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
//                return
                }
            }
        } else {
//            return
//            Toast.makeText(this, "Cancelled2", Toast.LENGTH_LONG).show()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}