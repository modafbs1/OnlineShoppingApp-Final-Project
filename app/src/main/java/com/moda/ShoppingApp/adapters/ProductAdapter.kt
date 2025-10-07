package com.moda.ShoppingApp.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.models.ProductModel
class ProductsAdapter(
    private var productList: List<ProductModel>,
    private val onItemClick: ((ProductModel) -> Unit)? = null
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.productName)
        val priceText: TextView = itemView.findViewById(R.id.productPrice)
        val imageView: ImageView = itemView.findViewById(R.id.productImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.nameText.text = product.name
        holder.priceText.text = "₪${product.price}"

        if (!product.imageUrl.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(product.imageUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.imageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // استدعاء الـ click listener إذا تم تمريره
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }

    fun updateList(newList: List<ProductModel>) {
        productList = newList
        notifyDataSetChanged()
    }
}
