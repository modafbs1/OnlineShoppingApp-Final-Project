package com.moda.ShoppingApp.fragments.admin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.moda.ShoppingApp.activities.ProductDetailsActivity
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.adapters.ProductsAdapter
import com.moda.ShoppingApp.models.ProductModel
import java.io.ByteArrayOutputStream

class AdminProductsFragment : Fragment() {

    private lateinit var productList: MutableList<ProductModel>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val PICK_IMAGE_REQUEST = 1001
    private var imageBitmap: Bitmap? = null
    private var editingProductId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_products, container, false)

        recyclerView = view.findViewById(R.id.recyclerProducts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        productList = mutableListOf()

        // تمرير click listener لفتح صفحة التفاصيل
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

        val fabAdd: FloatingActionButton = view.findViewById(R.id.fabAddProduct)
        fabAdd.setOnClickListener { showProductDialog() }

        fetchProducts()
        return view
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
                Toast.makeText(requireContext(), "فشل تحميل المنتجات", Toast.LENGTH_SHORT).show()
            }
    }



private fun showProductDialog(product: ProductModel? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val etName: EditText = dialogView.findViewById(R.id.etProductName)
        val etDesc: EditText = dialogView.findViewById(R.id.etProductDescription)
        val etPrice: EditText = dialogView.findViewById(R.id.etProductPrice)
        val etRating: EditText = dialogView.findViewById(R.id.etProductRating)
        val spCategory: Spinner = dialogView.findViewById(R.id.spCategory)
        val etLocation: EditText = dialogView.findViewById(R.id.etLocation)
        val ivImage: ImageView = dialogView.findViewById(R.id.ivProductImage)
        val btnSelectImage: Button = dialogView.findViewById(R.id.btnSelectImage)

        //  استدعاء الفئات من Firestore
        val categories = mutableListOf<String>()
        val categoryDb = FirebaseFirestore.getInstance().collection("Categories")
        categoryDb.get().addOnSuccessListener { snapshot ->
            categories.clear()
            for (doc in snapshot.documents) {
                val catName = doc.getString("name")
                if (!catName.isNullOrEmpty()) categories.add(catName)
            }
            spCategory.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
            )

            product?.let {
                spCategory.setSelection(categories.indexOf(it.category))
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "فشل تحميل الفئات", Toast.LENGTH_SHORT).show()
        }

        product?.let {
            etName.setText(it.name)
            etDesc.setText(it.description)
            etPrice.setText(it.price)
            etRating.setText(it.rating.toString())
            etLocation.setText(it.location)
            editingProductId = it.id
            imageBitmap = it.imageUrl?.let { base64ToBitmap(it) }
            ivImage.setImageBitmap(imageBitmap)
        }

        btnSelectImage.setOnClickListener { pickImageFromGallery() }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (product == null) "إضافة منتج" else "تعديل المنتج")
            .setView(dialogView)
            .setPositiveButton(if (product == null) "إضافة" else "تحديث") { _, _ ->
                val name = etName.text.toString()
                val desc = etDesc.text.toString()
                val price = etPrice.text.toString()
                val rating = etRating.text.toString().toFloatOrNull() ?: 0f
                val category = spCategory.selectedItem.toString()
                val location = etLocation.text.toString()

                if (product == null) {
                    uploadProduct(name, desc, price, rating, category, location)
                } else {
                    updateProduct(editingProductId!!, name, desc, price, rating, category, location)
                }
            }
            .setNegativeButton("إلغاء", null)
            .create()
        dialog.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            uri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                imageBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    private fun uploadProduct(
        name: String,
        desc: String,
        price: String,
        rating: Float,
        category: String,
        location: String
    ) {
        if (imageBitmap == null) {
            Toast.makeText(requireContext(), "الرجاء اختيار صورة", Toast.LENGTH_SHORT).show()
            return
        }

        val productId = db.collection("products").document().id
        val base64Image = bitmapToBase64(imageBitmap!!)
        val product = ProductModel(
            id = productId,
            name = name,
            description = desc,
            price = price,
            rating = rating,
            category = category,
            imageUrl = base64Image,
            location = location
        )

        db.collection("products").document(productId).set(product)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "تم إضافة المنتج", Toast.LENGTH_SHORT).show()
                fetchProducts()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "فشل إضافة المنتج", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProduct(
        productId: String,
        name: String,
        desc: String,
        price: String,
        rating: Float,
        category: String,
        location: String
    ) {
        val updatedMap = mutableMapOf<String, Any>(
            "name" to name,
            "description" to desc,
            "price" to price,
            "rating" to rating,
            "category" to category,
            "location" to location
        )

        imageBitmap?.let {
            updatedMap["imageUrl"] = bitmapToBase64(it)
        }

        db.collection("products").document(productId)
            .update(updatedMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "تم تحديث المنتج", Toast.LENGTH_SHORT).show()
                fetchProducts()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "فشل تحديث المنتج", Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteProduct(productId: String) {
        db.collection("products").document(productId).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "تم حذف المنتج", Toast.LENGTH_SHORT).show()
                fetchProducts()
            }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

}
