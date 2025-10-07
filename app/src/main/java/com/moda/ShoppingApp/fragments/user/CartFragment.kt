package com.moda.ShoppingApp.fragments.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.moda.ShoppingApp.activities.payment.PaymentActivity
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.activities.ProductDetailsActivity
import com.moda.ShoppingApp.adapters.CartAdapter
import com.moda.ShoppingApp.models.ProductModel

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalText: TextView
    private lateinit var checkoutBtn: Button
    private lateinit var productList: ArrayList<ProductModel>
    private var cartListener: ListenerRegistration? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    private var totalAmount: Double = 0.0 //  لحفظ المجموع الكلي

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCart)
        totalText = view.findViewById(R.id.totalPriceText)
        checkoutBtn = view.findViewById(R.id.checkoutButton)

        recyclerView.layoutManager = LinearLayoutManager(context)
        productList = arrayListOf()

        loadCartItems()

        checkoutBtn.setOnClickListener {
            val intent = Intent(requireContext(), PaymentActivity::class.java)
            intent.putExtra("totalAmount", totalAmount)
            startActivity(intent)




        }

        return view
    }

    private fun loadCartItems() {
        val cartRef = db.collection("Carts").document(userId).collection("Products")
        cartListener = cartRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            productList.clear()
            totalAmount = 0.0
            for (doc in snapshot.documents) {
                val product = doc.toObject(ProductModel::class.java)
                if (product != null) {
                    productList.add(product)
                    totalAmount += product.price?.toDoubleOrNull() ?: 0.0
                }
            }

            recyclerView.adapter = CartAdapter(productList) { product ->
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra("productId", product.id)
                intent.putExtra("productName", product.name)
                intent.putExtra("productPrice", product.price)
                intent.putExtra("productDesc", product.description)
                intent.putExtra("productImage", product.imageUrl)
                startActivity(intent)
            }

            totalText.text = "الإجمالي: ₪$totalAmount"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cartListener?.remove()
    }
}
