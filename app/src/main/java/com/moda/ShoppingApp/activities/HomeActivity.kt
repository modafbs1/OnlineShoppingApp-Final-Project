package com.moda.ShoppingApp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.adapters.ProductsAdapter
import com.moda.ShoppingApp.models.ProductModel

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbRef: DatabaseReference
    private lateinit var productList: ArrayList<ProductModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = arrayListOf()

        dbRef = FirebaseDatabase.getInstance().getReference("Products")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (item in snapshot.children) {
                    val product = item.getValue(ProductModel::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                recyclerView.adapter = ProductsAdapter(productList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "فشل تحميل المنتجات", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
