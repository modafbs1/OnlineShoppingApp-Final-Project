package com.moda.ShoppingApp.fragments.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.moda.ShoppingApp.activities.ProductDetailsActivity
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.adapters.ProductsAdapter
import com.moda.ShoppingApp.models.ProductModel

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<ProductModel>
    private var favListener: ListenerRegistration? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(context)
        productList = arrayListOf()

        loadFavorites()

        return view
    }

    private fun loadFavorites() {
        val favRef = db.collection("Favorites").document(userId).collection("Products")
        favListener = favRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            productList.clear()
            for (doc in snapshot.documents) {
                val product = doc.toObject(ProductModel::class.java)
                if (product != null) productList.add(product)
            }

            recyclerView.adapter = ProductsAdapter(productList) { product ->
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra("productId", product.id)
                intent.putExtra("productName", product.name)
                intent.putExtra("productPrice", product.price)
                intent.putExtra("productDesc", product.description)
                intent.putExtra("productImage", product.imageUrl)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        favListener?.remove()
    }
}
