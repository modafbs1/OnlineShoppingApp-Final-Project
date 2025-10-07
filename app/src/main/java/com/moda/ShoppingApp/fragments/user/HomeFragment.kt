package com.moda.ShoppingApp.fragments.user

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.moda.ShoppingApp.activities.ProductDetailsActivity
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.adapters.ProductsAdapter
import com.moda.ShoppingApp.models.ProductModel

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEdit: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRating: Spinner
    private lateinit var spinnerPrice: Spinner

    private lateinit var productList: MutableList<ProductModel>
    private lateinit var adapter: ProductsAdapter
    private val db = FirebaseFirestore.getInstance()

    private var categoriesList = mutableListOf<String>("الكل")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewProducts)
        searchEdit = view.findViewById(R.id.searchEditText)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        spinnerRating = view.findViewById(R.id.spinnerRating)
        spinnerPrice = view.findViewById(R.id.spinnerPrice)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        productList = mutableListOf()

        adapter = ProductsAdapter(productList) { product ->
            val intent = Intent(requireContext(), ProductDetailsActivity::class.java).apply {
                putExtra("productId", product.id)
                putExtra("productName", product.name)
                putExtra("productPrice", product.price)
                putExtra("productDesc", product.description)
                putExtra("productImage", product.imageUrl)
                putExtra("productRating", product.rating)
                putExtra("productCategory", product.category)
                putExtra("productLocation", product.location)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        setupSpinners()
        fetchCategories() // تحميل الفئات من Firestore
        fetchProducts()

        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts()
            }
        })

        return view
    }

    private fun setupSpinners() {
        // التصنيفات (ستتم جلبها من فيرستور)
        spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoriesList)
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterProducts()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val ratings = arrayOf("التقييم", "1+", "2+", "3+", "4+", "5")
        spinnerRating.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ratings)
        spinnerRating.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterProducts()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val prices = arrayOf("السعر", "0-50", "50-100", "100-200", "200+")
        spinnerPrice.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, prices)
        spinnerPrice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterProducts()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchCategories() {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                val set = mutableSetOf<String>()
                for (document in result) {
                    val category = document.getString("category")
                    if (!category.isNullOrEmpty()) set.add(category)
                }
                categoriesList.clear()
                categoriesList.add("الفئات")
                categoriesList.addAll(set.sorted())
                (spinnerCategory.adapter as ArrayAdapter<String>).notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "فشل تحميل الفئات", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchProducts() {
        db.collection("products").get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    val product = document.toObject(ProductModel::class.java)
                    product.id = document.id
                    productList.add(product)
                }
                adapter.updateList(productList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "فشل تحميل المنتجات ", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterProducts() {
        val query = searchEdit.text.toString()
        val selectedCategory = spinnerCategory.selectedItem?.toString() ?: "الفئات"
        val selectedRating = spinnerRating.selectedItem?.toString() ?: "التقييم"
        val selectedPrice = spinnerPrice.selectedItem?.toString() ?: "السعر"

        val filtered = productList.filter { product ->
            val matchesSearch = product.name?.contains(query, true) == true ||
                    product.description?.contains(query, true) == true ||
                    product.price?.contains(query) == true

            val matchesCategory = selectedCategory == "الفئات" || product.category == selectedCategory

            val matchesRating = when (selectedRating) {
                "التقييم" -> true
                "1+" -> product.rating >= 1f
                "2+" -> product.rating >= 2f
                "3+" -> product.rating >= 3f
                "4+" -> product.rating >= 4f
                "5" -> product.rating == 5f
                else -> true
            }

            val priceValue = product.price?.toFloatOrNull() ?: 0f
            val matchesPrice = when (selectedPrice) {
                "السعر" -> true
                "0-50" -> priceValue in 0f..50f
                "50-100" -> priceValue in 50f..100f
                "100-200" -> priceValue in 100f..200f
                "200+" -> priceValue > 200f
                else -> true
            }

            matchesSearch && matchesCategory && matchesRating && matchesPrice
        }

        adapter.updateList(filtered)
    }
}
