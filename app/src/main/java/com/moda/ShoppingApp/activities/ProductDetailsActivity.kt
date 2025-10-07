package com.moda.ShoppingApp.activities

    import android.app.AlertDialog
    import android.content.Intent
    import android.graphics.BitmapFactory
    import android.net.Uri
    import android.os.Bundle
    import android.util.Base64
    import android.view.LayoutInflater
    import android.widget.*
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.widget.Toolbar
    import com.bumptech.glide.Glide
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.moda.ShoppingApp.R
    import com.moda.ShoppingApp.models.ProductModel
    import java.io.InputStream

    class ProductDetailsActivity : AppCompatActivity() {

        private val db = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()
        private val PICK_IMAGE = 200
        private var newImageUri: Uri? = null
        private lateinit var productId: String
        private lateinit var currentImage: String

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_product_details)

            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "تفاصيل المنتج"
            toolbar.setNavigationOnClickListener { onBackPressed() }

            val image = findViewById<ImageView>(R.id.detailImage)
            val name = findViewById<TextView>(R.id.detailName)
            val price = findViewById<TextView>(R.id.detailPrice)
            val desc = findViewById<TextView>(R.id.detailDesc)
            val categoryText = findViewById<TextView>(R.id.detailCategory)
            val locationText = findViewById<TextView>(R.id.detailLocation)
            val ratingBar = findViewById<RatingBar>(R.id.detailRating)
            val favBtn = findViewById<Button>(R.id.addToFav)
            val cartBtn = findViewById<Button>(R.id.addToCart)
            val adminEdit = findViewById<Button>(R.id.editProduct)
            val adminDelete = findViewById<Button>(R.id.deleteProduct)

            productId = intent.getStringExtra("productId") ?: ""
            val productName = intent.getStringExtra("productName") ?: ""
            val productPrice = intent.getStringExtra("productPrice") ?: ""
            val productDesc = intent.getStringExtra("productDesc") ?: ""
            currentImage = intent.getStringExtra("productImage") ?: ""
            val productRating = intent.getFloatExtra("productRating", 0f)
            val productCategory = intent.getStringExtra("productCategory") ?: ""
            val productLocation = intent.getStringExtra("productLocation") ?: ""

            name.text = productName
            price.text = "₪$productPrice"
            desc.text = productDesc
            categoryText.text = "الفئة: $productCategory"
            locationText.text = "الموقع: $productLocation"
            ratingBar.rating = productRating

            // تحميل الصورة
            if (currentImage.startsWith("http")) {
                Glide.with(this).load(currentImage).into(image)
            } else {
                try {
                    val decodedBytes = Base64.decode(currentImage, Base64.DEFAULT)
                    val bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    image.setImageBitmap(bmp)
                } catch (e: Exception) {
                    image.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }

            val isAdmin = auth.currentUser?.email == "admin@gmail.com"
            favBtn.visibility = if (isAdmin) Button.GONE else Button.VISIBLE
            cartBtn.visibility = if (isAdmin) Button.GONE else Button.VISIBLE
            adminEdit.visibility = if (isAdmin) Button.VISIBLE else Button.GONE
            adminDelete.visibility = if (isAdmin) Button.VISIBLE else Button.GONE

            // حذف المنتج
            adminDelete.setOnClickListener {
                db.collection("products").document(productId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, " تم حذف المنتج بنجاح", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, " فشل حذف المنتج", Toast.LENGTH_SHORT).show()
                    }
            }

            // تعديل المنتج
            adminEdit.setOnClickListener {
                showEditDialog(
                    productName,
                    productDesc,
                    productPrice,
                    productRating,
                    productCategory,
                    productLocation
                )
            }

            // إضافة إلى المفضلة
            favBtn.setOnClickListener {
                val favRef = db.collection("Favorites")
                    .document(auth.currentUser?.uid ?: "")
                    .collection("Products")
                    .document(productId)

                val product = ProductModel(
                    productId,
                    productName,
                    productDesc,
                    productPrice,
                    currentImage,
                    productRating,
                    productCategory,
                    productLocation
                )
                favRef.set(product).addOnSuccessListener {
                    Toast.makeText(this, "تمت الإضافة إلى المفضلة ", Toast.LENGTH_SHORT).show()
                }
            }

            // إضافة إلى السلة
            cartBtn.setOnClickListener {
                val cartRef = db.collection("Carts")
                    .document(auth.currentUser?.uid ?: "")
                    .collection("Products")
                    .document(productId)

                val product = ProductModel(
                    productId,
                    productName,
                    productDesc,
                    productPrice,
                    currentImage,
                    productRating,
                    productCategory,
                    productLocation
                )
                cartRef.set(product).addOnSuccessListener {
                    Toast.makeText(this, "تمت الإضافة إلى السلة ", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun showEditDialog(
            currentName: String,
            currentDesc: String,
            currentPrice: String,
            currentRating: Float,
            currentCategory: String,
            currentLocation: String
        ) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null)
            val etName = dialogView.findViewById<EditText>(R.id.etProductName)
            val etDesc = dialogView.findViewById<EditText>(R.id.etProductDescription)
            val etPrice = dialogView.findViewById<EditText>(R.id.etProductPrice)
            val etRating = dialogView.findViewById<EditText>(R.id.etProductRating)
            val etLocation = dialogView.findViewById<EditText>(R.id.etLocation)
            val imgProduct = dialogView.findViewById<ImageView>(R.id.etProductImage)
            val btnSelect = dialogView.findViewById<Button>(R.id.btnSelectImage)
            val spCategory = dialogView.findViewById<Spinner>(R.id.spCategory)

            // تعبئة البيانات
            etName.setText(currentName)
            etDesc.setText(currentDesc)
            etPrice.setText(currentPrice)
            etRating.setText(currentRating.toString())
            etLocation.setText(currentLocation)

            try {
                val decoded = Base64.decode(currentImage, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                imgProduct.setImageBitmap(bmp)
            } catch (_: Exception) { }

            // تحميل الفئات من Firestore
            val categoryList = ArrayList<String>()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryList)
            spCategory.adapter = adapter

            FirebaseFirestore.getInstance().collection("Categories").get()
                .addOnSuccessListener { snapshot ->
                    categoryList.clear()
                    for (doc in snapshot) {
                        val name = doc.getString("name") ?: continue
                        categoryList.add(name)
                    }
                    adapter.notifyDataSetChanged()
                    val index = categoryList.indexOf(currentCategory)
                    if (index >= 0) spCategory.setSelection(index)
                }

            btnSelect.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE)
            }

            val dialog = AlertDialog.Builder(this)
                .setTitle("تعديل المنتج")
                .setView(dialogView)
                .setPositiveButton("تحديث", null)
                .setNegativeButton("إلغاء", null)
                .create()

            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val newName = etName.text.toString().ifEmpty { currentName }
                    val newDesc = etDesc.text.toString().ifEmpty { currentDesc }
                    val newPrice = etPrice.text.toString().ifEmpty { currentPrice }
                    val newRating = etRating.text.toString().toFloatOrNull() ?: currentRating
                    val newCategory = spCategory.selectedItem?.toString() ?: currentCategory
                    val newLocation = etLocation.text.toString().ifEmpty { currentLocation }

                    val newImageBase64 = if (newImageUri != null) convertImageToBase64(newImageUri!!) else currentImage

                    val updatedProduct = mapOf(
                        "name" to newName,
                        "description" to newDesc,
                        "price" to newPrice,
                        "rating" to newRating,
                        "category" to newCategory,
                        "location" to newLocation,
                        "imageUrl" to newImageBase64
                    )

                    db.collection("products").document(productId)
                        .update(updatedProduct)
                        .addOnSuccessListener {
                            Toast.makeText(this, " تم تحديث المنتج بنجاح", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            recreate()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, " فشل التحديث", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            dialog.show()
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
                newImageUri = data?.data
            }
        }

        private fun convertImageToBase64(uri: Uri): String {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return ""
            return Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }
