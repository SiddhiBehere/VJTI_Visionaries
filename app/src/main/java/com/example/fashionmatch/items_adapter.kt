package com.example.fashionmatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class items_adapter(var items: List<items_data>) : RecyclerView.Adapter<items_adapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_fashion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Glide.with(holder.itemImage.context)
            .load(item.item_img)
            .into(holder.itemImage)
        holder.itemPrice.text = item.selling_price
        holder.itemDiscount.text = item.mrp
        holder.itemBrand.text = item.brand
        holder.itemDescription.text = item.desc
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(newItems: List<items_data>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        val itemDiscount: TextView = itemView.findViewById(R.id.item_discount)
        val itemBrand: TextView = itemView.findViewById(R.id.item_brand)
        val itemDescription: TextView = itemView.findViewById(R.id.item_description)
    }
}
