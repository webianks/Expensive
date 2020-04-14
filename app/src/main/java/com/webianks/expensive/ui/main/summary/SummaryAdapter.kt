package com.webianks.expensive.ui.main.summary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.webianks.expensive.R
import com.webianks.expensive.data.local.Summary
import kotlinx.android.synthetic.main.item_layout_single_month.view.*
import java.text.DecimalFormat

class SummaryAdapter(
    private val context: Context,
    private val monthList: List<Summary>
) : RecyclerView.Adapter<SummaryAdapter.VH>() {

    val decimalFormat = DecimalFormat("#.##")

    init {
        decimalFormat.isGroupingUsed = true
        decimalFormat.groupingSize = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_layout_single_month, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(monthList[position])
    }

    override fun getItemCount(): Int {
        return monthList.size
    }


    inner class VH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(summary: Summary) {
            itemView.tv_month.text = summary.month
            itemView.tv_year.text = summary.year
            itemView.tv_amount.text = "\u20B9 "+decimalFormat.format(summary.totalAmount)
        }
    }

}