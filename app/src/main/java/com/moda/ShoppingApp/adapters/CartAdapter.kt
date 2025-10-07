package com.moda.ShoppingApp.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.models.ProductModel

class CartAdapter(
    private val cartList: ArrayList<ProductModel>,
    private val onItemClick: (ProductModel) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.cartProductImage)
        val productName: TextView = itemView.findViewById(R.id.cartProductName)
        val productPrice: TextView = itemView.findViewById(R.id.cartProductPrice)
        val removeBtn: Button = itemView.findViewById(R.id.cartDeleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = cartList[position]

        holder.productName.text = product.name
        holder.productPrice.text = "₪${product.price}"

        // عرض الصورة من Base64
        if (!product.imageUrl.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(product.imageUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.productImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.productImage.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            holder.productImage.setImageResource(R.drawable.ic_launcher_foreground)
        }

        //  عند الضغط على المنتج نرسل الحدث إلى اللامبدا
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }

        //  زر الحذف من Firestore
        holder.removeBtn.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val db = FirebaseFirestore.getInstance()
            db.collection("Carts")
                .document(userId)
                .collection("Products")
                .document(product.id ?: "")
                .delete()
        }
    }

    override fun getItemCount(): Int = cartList.size
}
