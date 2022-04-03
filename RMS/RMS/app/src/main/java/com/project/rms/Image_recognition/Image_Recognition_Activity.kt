package com.project.rms.Image_recognition

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.project.rms.databinding.YsjImageRecognitionBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat

class Image_Recognition_Activity: AppCompatActivity() {

    // ViewBinding
    lateinit var binding : YsjImageRecognitionBinding

    // Permisisons
    val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    val PERMISSIONS_REQUEST = 100

    // Request Code
    private val BUTTON4 = 400

    private var photoUri: Uri? = null
    //private lateinit var img : ImageView //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = YsjImageRecognitionBinding.inflate(layoutInflater)
        val view = binding.root

        val server = retrofit.create(retrofit_interface::class.java) //api

        setContentView(view)

        checkPermissions(PERMISSIONS, PERMISSIONS_REQUEST)

        binding.camera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = File(
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image").apply {
                    if (!this.exists()) {
                        this.mkdirs()
                    }
                },
                newJpgFileName()
            )
            photoUri = FileProvider.getUriForFile(
                this,
                "com.blacklog.takepicture.fileprovider",
                photoFile
            )
            takePictureIntent.resolveActivity(packageManager)?.also {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, BUTTON4)
            }
        }

        binding.btnUpload.setOnClickListener{
            val file = File("/storage/emulated/0/Android/data/com.project.rms/files/Pictures/image/test.jpg")
            val requestFile = RequestBody.create(MediaType.parse("application/pdf"), file)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            server.sendFile(body).enqueue(object : Callback<ResponseData> {
                override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                    if(response.isSuccessful){
                        // 정상적으로 통신이 성고된 경우
                        var result: ResponseData? = response.body()
                        Log.d("YMC", "onResponse 성공: " + result?.toString());
                        binding.result.setText(result?.toString())
                    }else{
                        // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                        Log.d("YMC", "onResponse 실패")
                    }
                }

                override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                    // 통신 실패 (인터넷 끊킴, 예외 발생 등 시스템적인 이유)
                    Log.d("YMC", "onFailure 에러: " + t.message.toString());
                }
            })
        }
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {

                BUTTON4 -> {
                    val imageBitmap = photoUri?.let { ImageDecoder.createSource(this.contentResolver, it) }
                    binding.ysjimageView.setImageBitmap(imageBitmap?.let { ImageDecoder.decodeBitmap(it) })
                    Toast.makeText(this, photoUri?.path, Toast.LENGTH_LONG).show()
                }

            }
        }

    }

    private fun newJpgFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = "test"
        //val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }
    private fun saveBitmapAsJPGFile(bitmap: Bitmap) {
        val path = File(filesDir, "image")
        if(!path.exists()){
            path.mkdirs()
        }
        val file = File(path, newJpgFileName())
        var imageFile: OutputStream? = null
        try{
            file.createNewFile()
            imageFile = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageFile)
            imageFile.close()
            Toast.makeText(this, file.absolutePath, Toast.LENGTH_LONG).show()
        }catch (e: Exception){
            null
        }
    }

    private fun checkPermissions(permissions: Array<String>, permissionsRequest: Int): Boolean {
        val permissionList : MutableList<String> = mutableListOf()
        for(permission in permissions){
            val result = ContextCompat.checkSelfPermission(this, permission)
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission)
            }
        }
        if(permissionList.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), PERMISSIONS_REQUEST)
            return false
        }
        return true
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for(result in grantResults){
            if(result != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "권한 승인 부탁드립니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}