package com.moda.ShoppingApp.activities.payment

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.models.ProductModel
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_payment)

        val radioGroup = findViewById<RadioGroup>(R.id.paymentMethods)
        val cardLayout = findViewById<LinearLayout>(R.id.cardDetailsLayout)
        val payButton = findViewById<Button>(R.id.payButton)
        val totalText = findViewById<TextView>(R.id.totalAmountText)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        totalText.text = "الإجمالي الكلي: ₪$totalAmount"

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            cardLayout.visibility =
                if (checkedId == R.id.cardPayment) LinearLayout.VISIBLE else LinearLayout.GONE
        }

        payButton.setOnClickListener {
            processPayment(totalAmount)
        }
    }

    private fun processPayment(totalAmount: Double) {
        val userId = auth.currentUser?.uid ?: return

        val cartRef = firestore.collection("Carts").document(userId).collection("Products")
        cartRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                Toast.makeText(this, "السلة فارغة!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val products = snapshot.toObjects(ProductModel::class.java)
            val batch = firestore.batch()

            // حفظ عملية البيع في Collection "Sales"
            val saleId = firestore.collection("Sales").document().id
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val saleData = hashMapOf(
                "saleId" to saleId,
                "userId" to userId,
                "totalAmount" to totalAmount,
                "date" to date,
                "products" to products.map { product ->
                    mapOf(
                        "id" to product.id,
                        "name" to product.name,
                        "price" to product.price,
                        "category" to product.category,
                        "rating" to product.rating,
                        "imageUrl" to product.imageUrl
                    )
                }
            )
            batch.set(firestore.collection("Sales").document(saleId), saleData)

            //  تحديث عدد المبيعات لكل منتج
            for (product in products) {
                val productRef = firestore.collection("products").document(product.id ?: "")
                batch.update(productRef, "soldCount", FieldValue.increment(1))
            }

            //  حذف جميع المنتجات من السلة
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }

            // تنفيذ جميع العمليات دفعة واحدة
            batch.commit().addOnSuccessListener {
                Toast.makeText(this, " تم تأكيد الدفع وتحديث المبيعات", Toast.LENGTH_LONG).show()
                // الانتقال إلى صفحة نجاح الطلب
                val intent = Intent(this, OrderSuccessActivity::class.java)
                intent.putExtra("totalAmount", totalAmount)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, " فشل معالجة الدفع", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this, " حدث خطأ أثناء تحميل سلة المشتريات", Toast.LENGTH_SHORT).show()
        }
    }
}
