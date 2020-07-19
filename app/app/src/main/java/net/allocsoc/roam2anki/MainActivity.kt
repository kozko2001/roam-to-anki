package net.allocsoc.roam2anki

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.findViewById<Button>(R.id.btnAction).setOnClickListener { addCard() }
        ensurePermissions()
    }

    private fun addCard() {
            Anki(this).getModels()
    }

    private fun ensurePermissions() {
        if (shouldRequestPermission()) {
            requestPermissions();
        }
    }

    private fun shouldRequestPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return ContextCompat.checkSelfPermission(this, com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION) != PackageManager.PERMISSION_GRANTED;
    }

    private fun requestPermissions() {
        val permissions = arrayOf(com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION)
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}