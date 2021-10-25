package com.ts.bindid.example.ui.main.token

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ts.bindid.example.R
import kotlinx.android.synthetic.main.item_passport.view.*

private const val VIEW_TYPE_FIRST = 0
private const val VIEW_TYPE_LAST = 10
private const val VIEW_TYPE_REGULAR = 1

class PassportAdapter(val list: List<TokenItem>) : RecyclerView.Adapter<PassportViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassportViewHolder {
        return PassportViewHolder.create(parent, viewType)
    }

    val listLastIndex by lazy { list.size - 1 }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_FIRST
        } else if (position == listLastIndex) {
            VIEW_TYPE_LAST
        } else {
            VIEW_TYPE_REGULAR
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: PassportViewHolder, position: Int) {
        list[position].apply {
            holder.itemView.name.text = this.name
            holder.itemView.value.text = this.value
        }
    }
}

class PassportViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
    companion object {
        fun create(parent: ViewGroup, viewType:Int): PassportViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(R.layout.item_passport, parent, false)
            when (viewType) {
                VIEW_TYPE_FIRST -> root.background = ContextCompat.getDrawable(parent.context, R.drawable.smooth_top_corners_blue_dark)
                VIEW_TYPE_LAST -> root.background = ContextCompat.getDrawable(parent.context, R.drawable.smooth_bottom_corners_blue_dark)
            }
            return PassportViewHolder(root)
        }
    }
}