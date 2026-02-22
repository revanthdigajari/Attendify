package com.example.attendify.models

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    // 1. The big percentage number (e.g., 50.0)
    @SerializedName("attendance_percentage")
    val attendancePercentage: Double,

    // 2. Count of students present today
    @SerializedName("present_count")
    val presentCount: Int,

    // 3. Total students registered
    @SerializedName("total_students")
    val totalStudents: Int,

    // 4. The list of students (currently empty from backend, but we prepare for it)
    @SerializedName("directory")
    val directory: List<StudentPreview>
)

// A small helper class for the directory list items
data class StudentPreview(
    val id: Int? = null,
    val name: String? = null
)