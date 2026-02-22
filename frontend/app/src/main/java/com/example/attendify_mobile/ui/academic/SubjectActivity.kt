package com.example.attendify_mobile.ui.academic

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.attendify.R
import com.example.attendify_mobile.api.RetrofitClient
import com.example.attendify_mobile.api.models.Subject
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SubjectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Bind Views
        val etSubjectName = findViewById<EditText>(R.id.etSubjectName)
        val etSubjectCode = findViewById<EditText>(R.id.etSubjectCode)
        val etDepartment = findViewById<EditText>(R.id.etDepartment)
        val etCredits = findViewById<EditText>(R.id.etCredits)
        val btnSaveSubject = findViewById<Button>(R.id.btnSaveSubject)

        btnSaveSubject.setOnClickListener {
            val name = etSubjectName.text.toString().trim()
            val code = etSubjectCode.text.toString().trim()
            val dept = etDepartment.text.toString().trim()
            val creditsStr = etCredits.text.toString().trim()

            if (name.isEmpty() || code.isEmpty() || dept.isEmpty() || creditsStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credits = creditsStr.toIntOrNull() ?: 0

            // Create Subject Object (without ID, as subject_code is the likely identifier)
            val subject = Subject(
                subjectCode = code,
                subjectName = name,
                department = dept,
                credits = credits
            )

            saveSubject(subject)
        }
    }

    private fun saveSubject(subject: Subject) {
        lifecycleScope.launch {
            try {
                // Make the API Call
                val response = RetrofitClient.adminService.createSubject(subject)

                if (response.isSuccessful) {
                    Toast.makeText(this@SubjectActivity, "Subject Created: ${subject.subjectName}", Toast.LENGTH_SHORT).show()
                    finish() // Close activity on success
                } else {
                    // Log the error body to see exactly what went wrong
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Code: ${response.code()} Body: $errorBody")
                    Toast.makeText(this@SubjectActivity, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this@SubjectActivity, "Network Error. Check connection.", Toast.LENGTH_SHORT).show()
                Log.e("API_ERROR", "Network error", e)
            } catch (e: HttpException) {
                Toast.makeText(this@SubjectActivity, "Server Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SubjectActivity, "Unknown Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("API_ERROR", "Unknown error", e)
            }
        }
    }
}
