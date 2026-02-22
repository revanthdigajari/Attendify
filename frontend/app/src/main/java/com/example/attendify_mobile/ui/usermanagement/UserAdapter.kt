package com.example.attendify_mobile.ui.usermanagement

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendify.R
import com.example.attendify_mobile.api.models.Student
import com.example.attendify_mobile.api.models.Teacher
import com.google.android.material.button.MaterialButton

class UserAdapter(
    private val onEdit: (Any) -> Unit,
    private val onDelete: (Any) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users: List<Any> = emptyList()

    fun submitList(newList: List<Any>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        
        // Reset photo to default first
        holder.ivPhoto.setImageResource(R.drawable.ic_launcher_foreground) // Use a better default if available
        
        if (user is Student) {
            holder.tvName.text = user.name
            holder.tvInfo.text = "Roll: ${user.rollNumber}"
            
            // Decode and display the student photo if it exists
            user.faceTemplate?.let { base64String ->
                try {
                    val decodedString = Base64.decode(base64String, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    bitmap?.let {
                        holder.ivPhoto.setImageBitmap(it)
                    }
                } catch (e: Exception) {
                    // Stay with default icon if decoding fails
                }
            }
        } else if (user is Teacher) {
            holder.tvName.text = user.name
            holder.tvInfo.text = "${user.department} | ${user.email}"
            // Teachers might not have face templates yet, keep default icon
            holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_myplaces)
        }
        
        holder.btnEdit.setOnClickListener { onEdit(user) }
        holder.btnDelete.setOnClickListener { onDelete(user) }
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivUserPhoto)
        val tvName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvInfo: TextView = itemView.findViewById(R.id.tvUserInfo)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEditUser)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDeactivateUser)
    }
}
