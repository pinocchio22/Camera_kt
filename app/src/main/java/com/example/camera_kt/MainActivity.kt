package com.example.camera_kt

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileOutputStream
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {

    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val STORAGE_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val FLAG_PERM_CAMERA = 98
    val FLAG_PERM_STORAGE = 99

    val FLAG_REQ_CAMERA = 101
    val FLAG_REQ_GALLARY = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener{
            if(isPermitted(CAMERA_PERMISSION)){
            openCamera()
            } else{
                ActivityCompat.requestPermissions(this, CAMERA_PERMISSION,FLAG_PERM_CAMERA)
            }
        }

        button2.setOnClickListener {
            if(isPermitted(STORAGE_PERMISSION)){
                openGallary()
            } else{
                ActivityCompat.requestPermissions(this, STORAGE_PERMISSION,FLAG_PERM_STORAGE)
            }
        }
    }

    fun isPermitted(permissions:Array<String>) : Boolean {
        for(permission in permissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, FLAG_REQ_CAMERA)

    }

    fun openGallary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, FLAG_REQ_GALLARY)
    }

    fun saveImageFile(filename:String, mimeType:String, bitmap:Bitmap) : Uri? {
        var values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        try {
            if (uri != null) {
                var descriptor = contentResolver.openFileDescriptor(uri, "w")   //쓰기모드로 파일을 연다
                if (descriptor != null) {
                    val fos = FileOutputStream(descriptor.fileDescriptor)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()
                    return uri
                }
            }

        } catch (e: Exception) {
            Log.e("Camera", "${e.localizedMessage}")
        }

        return null
    }

    fun newFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())

        return "$filename.jpg"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                FLAG_REQ_CAMERA -> {
                    if(data?.extras?.get("data") != null) {
                        val bitmap = data?.extras?.get("data") as Bitmap
                        val filename = newFileName()
                        val uri = saveImageFile(filename, "image/jpg", bitmap)
                        imageView.setImageURI(uri)
                    }
                }
                FLAG_REQ_GALLARY -> {
                    val uri = data?.data
                    imageView.setImageURI(uri)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            FLAG_PERM_CAMERA -> {
                var checked = true
                for(grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED){
                        checked = false
                        break
                    }
                }
                if(checked) {
                    openCamera()    //카메라 호출
                }
            }
        }
    }
}