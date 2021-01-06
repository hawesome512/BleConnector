package com.hawesome.bleconnector.view.device.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.model.LogItemExtra

class LogExtraAdapter(val extraList: List<LogItemExtra>) :
    RecyclerView.Adapter<LogExtraAdapter.ViewHoler>() {

    inner class ViewHoler(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText = itemView.findViewById<TextView>(R.id.nameText)
        val valueText = itemView.findViewById<TextView>(R.id.valueText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHoler {
        val context = parent.context
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.display_list, parent, false)
        val holder = ViewHoler(itemView)
        itemView.setOnClickListener {
            val info = "${holder.nameText.text}(${holder.valueText.text})"
            Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHoler, position: Int) {
        val extra = extraList[position]
        holder.nameText.text = extra.name
        holder.valueText.text = extra.value
    }

    override fun getItemCount() = extraList.size

}