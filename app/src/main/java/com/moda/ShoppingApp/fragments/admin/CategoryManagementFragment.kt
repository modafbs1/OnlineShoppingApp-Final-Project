package com.moda.ShoppingApp.fragments.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.adapters.CategoryAdapter
import com.moda.ShoppingApp.models.CategoryModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class CategoryManagementFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addBtn: Button
    private lateinit var categoryEdit: EditText
    private lateinit var selectImageBtn: Button
    private var imageUri: Uri? = null
    private val PICK_IMAGE = 101

    private val categoryList = ArrayList<CategoryModel>()
    private lateinit var adapter: CategoryAdapter
    private val dbRef: CollectionReference = FirebaseFirestore.getInstance().collection("Categories")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_management, container, false)

        recyclerView = view.findViewById(R.id.categoryRecycler)
        addBtn = view.findViewById(R.id.addCategoryButton)
        categoryEdit = view.findViewById(R.id.categoryNameEdit)
        selectImageBtn = view.findViewById(R.id.selectImageButton)

        adapter = CategoryAdapter(categoryList, dbRef)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        selectImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        addBtn.setOnClickListener {
            val name = categoryEdit.text.toString()
            if (name.isEmpty() || imageUri == null) {
                Toast.makeText(context, "ادخل اسم الفئة واختر صورة", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addCategory(name)
        }

        loadCategories()

        return view
    }

    private fun loadCategories() {
        dbRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            categoryList.clear()
            for (doc in snapshot.documents) {
                val category = doc.toObject(CategoryModel::class.java)
                if (category != null) categoryList.add(category)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun addCategory(name: String) {
        val id = dbRef.document().id
        val category = CategoryModel(id, name, imageUri.toString())
        dbRef.document(id).set(category)
            .addOnSuccessListener {
                Toast.makeText(context, "تمت إضافة الفئة ", Toast.LENGTH_SHORT).show()
                categoryEdit.text.clear()
                imageUri = null
                selectImageBtn.text = "اختر صورة"
            }
            .addOnFailureListener {
                Toast.makeText(context, "فشل الإضافة", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            selectImageBtn.text = "تم اختيار الصورة"
        }
    }
}
