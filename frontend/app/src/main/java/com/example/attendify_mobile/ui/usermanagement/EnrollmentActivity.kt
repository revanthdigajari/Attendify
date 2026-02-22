package com.example.attendify_mobile.ui.usermanagement

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.attendify.R
import com.example.attendify_mobile.api.RetrofitClient
import com.example.attendify_mobile.api.models.Student
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Student Onboarding Activity.
 * Supports uploading a facial image from the Gallery only.
 */
class EnrollmentActivity : AppCompatActivity() {
    private var capturedFaceData: String? = null
    private lateinit var ivFacePreview: ImageView

    // Gallery Launcher - only keep image upload
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let { uri ->
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    processAndDisplayImage(bitmap)
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = "Student Onboarding"

        val etName = findViewById<EditText>(R.id.etStudentName)
        val etRoll = findViewById<EditText>(R.id.etRollNumber)
        val btnUploadImage = findViewById<Button>(R.id.btnRegisterFace)
        val btnEnroll = findViewById<Button>(R.id.btnSaveStudent)
        ivFacePreview = findViewById(R.id.ivFacePreview)

        // Updated button text to reflect upload action
        btnUploadImage.text = "Upload Facial Image (Optional)"
        btnEnroll.text = "Complete Onboarding"

        // Direct gallery pick instead of showing a dialog
        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnEnroll.setOnClickListener {
            val name = etName.text.toString()
            val roll = etRoll.text.toString()

            if (name.isNotEmpty() && roll.isNotEmpty()) {
                val student = Student(
                    name = name,
                    rollNumber = roll,
                    sectionId = 1,
                    faceTemplate = capturedFaceData
                )
                enrollStudent(student)
            } else {
                Toast.makeText(this, "Name and Roll Number are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processAndDisplayImage(bitmap: Bitmap) {
        ivFacePreview.setImageBitmap(bitmap)
        ivFacePreview.visibility = View.VISIBLE
        capturedFaceData = encodeImageToBase64(bitmap)
        Toast.makeText(this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show()
    }

    private fun enrollStudent(student: Student) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.adminService.addStudent(student)
                if (response.isSuccessful) {
                    Toast.makeText(this@EnrollmentActivity, "Student Onboarded Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EnrollmentActivity, "Onboarding Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EnrollmentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
