package com.example.attendify_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarkAttendance() {

    var otp by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Mark Attendance",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("Enter OTP") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                message = "Attendance Marked Successfully"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Attendance")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan QR (Coming Soon)")
        }
    }
}