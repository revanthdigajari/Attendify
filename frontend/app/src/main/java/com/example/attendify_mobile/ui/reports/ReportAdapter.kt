package com.example.attendify_mobile.ui.reports

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendify.R
import com.example.attendify_mobile.api.models.ReportItem

class ReportAdapter(private var reports: List<ReportItem>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    fun updateData(newReports: List<ReportItem>) {
        reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_detail, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.tvTitle.text = report.title
        holder.tvSubtitle.text = report.subtitle
        holder.tvValue.text = report.value
        holder.tvDate.text = report.date ?: ""

        // Professional Color Coding for Status
        when (report.value.uppercase()) {
            "PRESENT" -> holder.tvValue.setTextColor(Color.parseColor("#2E7D32")) // Success Green
            "ABSENT" -> holder.tvValue.setTextColor(Color.parseColor("#C62828"))  // Error Red
            "FLAGGED" -> holder.tvValue.setTextColor(Color.parseColor("#F57C00")) // Warning Orange
            else -> {
                // Handle Percentage values (e.g. 85%)
                if (report.value.contains("%")) {
                    val percent = report.value.replace("%", "").toDoubleOrNull() ?: 0.0
                    when {
                        percent >= 75.0 -> holder.tvValue.setTextColor(Color.parseColor("#2E7D32"))
                        percent >= 50.0 -> holder.tvValue.setTextColor(Color.parseColor("#F57C00"))
                        else -> holder.tvValue.setTextColor(Color.parseColor("#C62828"))
                    }
                } else {
                    holder.tvValue.setTextColor(Color.parseColor("#1A237E")) // Default Indigo
                }
            }
        }
    }

    override fun getItemCount() = reports.size

    class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvReportTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvReportSubtitle)
        val tvValue: TextView = view.findViewById(R.id.tvReportValue)
        val tvDate: TextView = view.findViewById(R.id.tvReportDate)
    }
}
