package com.hawesome.bleconnector.view.device.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.ext.startActivity
import com.hawesome.bleconnector.model.LogItem

class LogAdapter(val logItems: List<LogItem?>) : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeText = itemView.findViewById<TextView>(R.id.typeText)
        val orderText = itemView.findViewById<TextView>(R.id.orderText)
        val timeText = itemView.findViewById<TextView>(R.id.timeText)
        val startText = itemView.findViewById<TextView>(R.id.startText)
        val arrowImage = itemView.findViewById<ImageView>(R.id.arrowImage)
        val endText = itemView.findViewById<TextView>(R.id.endText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_log, parent, false)
        itemView.visibility = View.INVISIBLE
        val holder = ViewHolder(itemView)
        itemView.setOnClickListener {
            val extras = logItems[holder.adapterPosition]?.extras
            if (extras.isNullOrEmpty()) {
                val info = "${holder.typeText.text}(${holder.timeText.text})"
                Toast.makeText(context, holder.typeText.text, Toast.LENGTH_SHORT).show()
            } else {
                startActivity<LogExtraActivity>(context) {
                    putParcelableArrayListExtra(LogExtraActivity.EXT_DATA, extras as ArrayList)
                }
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val logItem = logItems[position]
        if (logItem == null){
            holder.itemView.visibility = View.INVISIBLE
            return
        }
        holder.itemView.visibility = View.VISIBLE
        holder.typeText.text = logItem.type
        holder.orderText.text = (position + 1).toString()
        holder.timeText.text = logItem.time
        val infos = logItem.info.split(LogItem.INFO_SEPERATOR)
        if (infos.size == 2) {
            holder.startText.text = infos[0]
            holder.endText.text = infos[1]
        } else {
            holder.startText.visibility = View.INVISIBLE
            holder.endText.visibility = View.INVISIBLE
            holder.arrowImage.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = logItems.size
}