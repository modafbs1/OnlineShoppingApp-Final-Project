package com.moda.ShoppingApp.adapters

import android.app.AlertDialog
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.CollectionReference
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.models.CategoryModel
import android.view.*

class CategoryAdapter(
    private val list: ArrayList<CategoryModel>,
    private val dbRef: CollectionReference
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.categoryImage)
        val name: TextView = view.findViewById(R.id.categoryName)
        val editBtn: ImageView = view.findViewById(R.id.btnEditCategory)
        val deleteBtn: ImageView = view.findViewById(R.id.btnDeleteCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.image)

        // تعديل اسم الفئة
        holder.editBtn.setOnClickListener {
            val editText = EditText(holder.itemView.context)
            editText.setText(item.name)
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("تعديل اسم الفئة")
                .setView(editText)
                .setPositiveButton("حفظ") { _, _ ->
                    dbRef.document(item.id!!).update("name", editText.text.toString())
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        // حذف الفئة
        holder.deleteBtn.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("حذف الفئة")
                .setMessage("هل أنت متأكد من حذف هذه الفئة؟")
                .setPositiveButton("حذف") { _, _ ->
                    dbRef.document(item.id!!).delete()
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
    }
}
