package com.example.attendify_mobile.ui.usermanagement

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.attendify.R
import com.example.attendify_mobile.api.RetrofitClient
import com.example.attendify_mobile.api.models.Student
import com.example.attendify_mobile.api.models.Teacher
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: UserAdapter
    private lateinit var spinnerFilter: Spinner
    private lateinit var etSearch: TextInputEditText
    
    private var currentTab = 0 // 0 for Students, 1 for Teachers
    private var allUsers: List<Any> = emptyList()
    private var filteredUsers: List<Any> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvUsers = findViewById(R.id.rvUsers)
        progressBar = findViewById(R.id.progressBar)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        etSearch = findViewById(R.id.etSearch)
        spinnerFilter = findViewById(R.id.spinnerSectionFilter)

        adapter = UserAdapter(
            onEdit = { user -> showEditDialog(user) },
            onDelete = { user -> showDeleteConfirmation(user) }
        )
        rvUsers.adapter = adapter

        setupFilterSpinner()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                etSearch.text?.clear()
                setupFilterSpinner() // Update spinner items based on tab
                fetchData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Search Implementation
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString(), spinnerFilter.selectedItem.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter Implementation
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterData(etSearch.text.toString(), spinnerFilter.selectedItem.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        fetchData()
    }

    private fun setupFilterSpinner() {
        val items = if (currentTab == 0) {
            arrayOf("All Sections", "Section A", "Section B", "Section C")
        } else {
            arrayOf("All Departments", "Computer Science", "Mathematics", "Physics", "Chemistry")
        }
        
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter
    }

    private fun fetchData() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = if (currentTab == 0) {
                    RetrofitClient.adminService.getStudents()
                } else {
                    RetrofitClient.adminService.getTeachers()
                }

                if (response.isSuccessful) {
                    allUsers = response.body() ?: emptyList()
                    filterData(etSearch.text.toString(), spinnerFilter.selectedItem.toString())
                } else {
                    Toast.makeText(this@ManageUsersActivity, "Server Error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ManageUsers", "Fetch Error", e)
                Toast.makeText(this@ManageUsersActivity, "Connection Error", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun filterData(query: String, filterValue: String) {
        filteredUsers = allUsers.filter { user ->
            val matchesSearch = if (user is Student) {
                user.name.contains(query, ignoreCase = true) || user.rollNumber.contains(query, ignoreCase = true)
            } else if (user is Teacher) {
                user.name.contains(query, ignoreCase = true) || user.email.contains(query, ignoreCase = true)
            } else false

            val matchesFilter = when {
                filterValue == "All Sections" || filterValue == "All Departments" -> true
                user is Student && filterValue.startsWith("Section") -> {
                    // Map Section A -> 1, B -> 2, C -> 3
                    val sectionChar = filterValue.last()
                    val sectionId = when(sectionChar) {
                        'A' -> 1
                        'B' -> 2
                        'C' -> 3
                        else -> 0
                    }
                    user.sectionId == sectionId
                }
                user is Teacher -> {
                    user.department.equals(filterValue, ignoreCase = true)
                }
                else -> false
            }

            matchesSearch && matchesFilter
        }
        adapter.submitList(filteredUsers)
    }

    private fun showEditDialog(user: Any) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etExtra = dialogView.findViewById<EditText>(R.id.etEditExtra)

        if (user is Student) {
            etName.setText(user.name)
            etExtra.hint = "Roll Number"
            etExtra.setText(user.rollNumber)
        } else if (user is Teacher) {
            etName.setText(user.name)
            etExtra.hint = "Email"
            etExtra.setText(user.email)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit User")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newName = etName.text.toString()
                val newExtra = etExtra.text.toString()
                updateUser(user, newName, newExtra)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUser(user: Any, name: String, extra: String) {
        lifecycleScope.launch {
            try {
                val response = if (user is Student) {
                    val updated = user.copy(name = name, rollNumber = extra)
                    RetrofitClient.adminService.updateStudent(user.id ?: "", updated)
                } else if (user is Teacher) {
                    val updated = user.copy(name = name, email = extra)
                    RetrofitClient.adminService.updateTeacher(user.id?.toString() ?: "", updated)
                } else return@launch

                if (response.isSuccessful) {
                    Toast.makeText(this@ManageUsersActivity, "Updated Successfully", Toast.LENGTH_SHORT).show()
                    fetchData()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ManageUsersActivity, "Update Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation(user: Any) {
        AlertDialog.Builder(this)
            .setTitle("Deactivate User")
            .setMessage("Are you sure you want to deactivate this user?")
            .setPositiveButton("Yes") { _, _ -> deactivateUser(user) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deactivateUser(user: Any) {
        lifecycleScope.launch {
            try {
                val type = if (user is Student) "student" else "teacher"
                val id = if (user is Student) user.id else (user as Teacher).id?.toString()
                val response = RetrofitClient.adminService.deactivateUser(type, id ?: "")
                if (response.isSuccessful) {
                    Toast.makeText(this@ManageUsersActivity, "Deactivated", Toast.LENGTH_SHORT).show()
                    fetchData()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ManageUsersActivity, "Operation Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
