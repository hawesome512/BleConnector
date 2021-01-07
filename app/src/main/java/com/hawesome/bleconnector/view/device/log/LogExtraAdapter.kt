package com.hawesome.bleconnector.view.device.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.showToast
import com.hawesome.bleconnector.ext.toUnitString
import com.hawesome.bleconnector.model.LogItemExtra

class LogExtraAdapter(val extraList: List<LogItemExtra>) :
    RecyclerView.Adapter<LogExtraAdapter.ViewHoler>() {

    companion object {
        const val HEADER = 1
        const val ITEM = 2
    }

    inner class ViewHoler(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText = itemView.findViewById<TextView>(R.id.nameText)
        val valueText = itemView.findViewById<TextView>(R.id.valueText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHoler {
        val context = parent.context
        val layoutID = if (viewType == HEADER) R.layout.display_header else R.layout.display_list
        val itemView =
            LayoutInflater.from(context).inflate(layoutID, parent, false)
        val holder = ViewHoler(itemView)
        itemView.setOnClickListener {
            if (viewType == HEADER) return@setOnClickListener
            "${holder.nameText.text}(${holder.valueText.text})".showToast()
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHoler, position: Int) {
        val extra = extraList[position]
        holder.nameText.text = extra.name.toUnitString()
        holder.valueText.text = extra.value
    }

    override fun getItemCount() = extraList.size

    override fun getItemViewType(position: Int) =
        if (extraList[position].value.isNullOrEmpty()) HEADER else ITEM
}