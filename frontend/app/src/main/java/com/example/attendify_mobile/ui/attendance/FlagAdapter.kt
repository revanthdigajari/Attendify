package com.example.attendify_mobile.ui.attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendify.R
import com.example.attendify_mobile.api.models.AttendanceFlag

class FlagAdapter(
    private var flags: List<AttendanceFlag>,
    private val onResolve: (AttendanceFlag, String) -> Unit
) : RecyclerView.Adapter<FlagAdapter.FlagViewHolder>() {

    class FlagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvStudentName)
        val tvReason: TextView = view.findViewById(R.id.tvReason)
        val tvDate: TextView = view.findViewById(R.id.tvTimestamp)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flag, parent, false)
        return FlagViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlagViewHolder, position: Int) {
        val flag = flags[position]
        holder.tvName.text = flag.studentName
        holder.tvReason.text = flag.reason
        holder.tvDate.text = flag.date // Matches backend 'date' field

        holder.btnApprove.setOnClickListener { onResolve(flag, "APPROVE") }
        holder.btnReject.setOnClickListener { onResolve(flag, "REJECT") }
    }

    override fun getItemCount() = flags.size

    fun updateData(newFlags: List<AttendanceFlag>) {
        flags = newFlags
        notifyDataSetChanged()
    }
}
