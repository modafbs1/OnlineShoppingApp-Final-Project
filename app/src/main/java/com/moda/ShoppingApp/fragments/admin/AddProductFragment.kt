package com.moda.ShoppingApp.fragments.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.models.ProductModel
import com.moda.ShoppingApp.models.CategoryModel
import java.util.*

class AddProductFragment : Fragment() {

    private var imageUri: Uri? = null
    private val PICK_IMAGE = 100
    private val dbRef = FirebaseFirestore.getInstance().collection("products")
    private val categoryRef = FirebaseFirestore.getInstance().collection("Categories")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_product, container, false)

        // الحقول
        val nameEdit = view.findViewById<EditText>(R.id.etProductName)
        val descEdit = view.findViewById<EditText>(R.id.etProductDescription)
        val priceEdit = view.findViewById<EditText>(R.id.etProductPrice)
        val ratingEdit = view.findViewById<EditText>(R.id.etProductRating)
        val categorySpinner = view.findViewById<Spinner>(R.id.spCategory)
        val locationEdit = view.findViewById<EditText>(R.id.etLocation)
        val imagePreview = view.findViewById<ImageView>(R.id.ivProductImage)
        val selectImageBtn = view.findViewById<Button>(R.id.btnSelectImage)
        val addProductBtn = view.findViewById<Button>(R.id.btnAddProduct)

        //  تحميل الفئات من Firestore
        val categoryList = ArrayList<String>()
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryList)
        categorySpinner.adapter = spinnerAdapter

        categoryRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            categoryList.clear()
            for (doc in snapshot.documents) {
                val category = doc.toObject(CategoryModel::class.java)
                category?.name?.let { categoryList.add(it) }
            }
            spinnerAdapter.notifyDataSetChanged()
        }

        // اختيار الصورة
        selectImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        // إضافة المنتج
        addProductBtn.setOnClickListener {
            val name = nameEdit.text.toString()
            val desc = descEdit.text.toString()
            val price = priceEdit.text.toString()
            val ratingText = ratingEdit.text.toString()
            val category = categorySpinner.selectedItem?.toString() ?: ""
            val location = locationEdit.text.toString()

            if (name.isEmpty() || price.isEmpty() || imageUri == null || category.isEmpty()) {
                Toast.makeText(context, "الرجاء إدخال البيانات كاملة", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rating = ratingText.toFloatOrNull() ?: 0f
            val id = UUID.randomUUID().toString()
            val base64Image = convertImageToBase64(imageUri!!)

            val product = ProductModel(
                id = id,
                name = name,
                description = desc,
                price = price,
                rating = rating,
                category = category,
                location = location,
                imageUrl = base64Image
            )

            dbRef.document(id).set(product)
                .addOnSuccessListener {
                    Toast.makeText(context, "تمت إضافة المنتج بنجاح", Toast.LENGTH_SHORT).show()
                    nameEdit.text.clear()
                    descEdit.text.clear()
                    priceEdit.text.clear()
                    ratingEdit.text.clear()
                    locationEdit.text.clear()
                    imagePreview.setImageResource(R.drawable.ic_launcher_foreground)
                    imageUri = null
                }
                .addOnFailureListener {
                    Toast.makeText(context, "فشل إضافة المنتج ", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            view?.findViewById<ImageView>(R.id.ivProductImage)?.setImageURI(imageUri)
        }
    }

    private fun convertImageToBase64(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bytes = inputStream!!.readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}
