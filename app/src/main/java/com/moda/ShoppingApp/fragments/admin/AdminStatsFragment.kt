package com.moda.ShoppingApp.fragments.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.models.ProductModel
import java.text.DecimalFormat

class AdminStatsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var totalProductsText: TextView
    private lateinit var totalRevenueText: TextView
    private lateinit var topRatedContainer: LinearLayout
    private lateinit var mostSoldContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_stats, container, false)

        totalProductsText = view.findViewById(R.id.totalProductsText)
        totalRevenueText = view.findViewById(R.id.totalRevenueText)
        topRatedContainer = view.findViewById(R.id.topRatedContainer)
        mostSoldContainer = view.findViewById(R.id.mostSoldContainer)

        fetchStats()

        return view
    }

    private fun fetchStats() {
        db.collection("products").get()
            .addOnSuccessListener { result ->
                val products = mutableListOf<ProductModel>()
                for (doc in result) {
                    val product = doc.toObject(ProductModel::class.java)
                    products.add(product)
                }

                totalProductsText.text = "عدد المنتجات: ${products.size}"

                // ️ حساب المبيعات الإجمالية لكل منتج من collection Sales
                db.collection("Sales").get()
                    .addOnSuccessListener { salesSnap ->
                        val productSalesMap = mutableMapOf<String, Int>() // productId -> total sold
                        var totalRevenue = 0.0

                        for (saleDoc in salesSnap.documents) {
                            val productsInSale = saleDoc.get("products") as? List<Map<String, Any>> ?: continue
                            for (p in productsInSale) {
                                val id = p["id"] as? String ?: continue
                                val price = (p["price"] as? String)?.toDoubleOrNull() ?: 0.0
                                val quantity = (p["quantity"] as? Long)?.toInt() ?: 1

                                productSalesMap[id] = (productSalesMap[id] ?: 0) + quantity
                                totalRevenue += price * quantity
                            }
                        }

                        totalRevenueText.text = "الإيرادات الإجمالية: ₪${DecimalFormat("#,###.00").format(totalRevenue)}"

                        // أعلى تقييم
                        val topRated = products.sortedByDescending { it.rating }.take(5)
                        topRatedContainer.removeAllViews()
                        for (p in topRated) {
                            val tv = TextView(requireContext())
                            tv.text = "${p.name} - تقييم: ${p.rating}"
                            tv.textSize = 16f
                            topRatedContainer.addView(tv)
                        }

                        // الأكثر مبيعًا
                        val mostSold = products.sortedByDescending { p -> productSalesMap[p.id] ?: 0 }.take(5)
                        mostSoldContainer.removeAllViews()
                        for (p in mostSold) {
                            val soldCount = productSalesMap[p.id] ?: 0
                            val tv = TextView(requireContext())
                            tv.text = "${p.name} - مبيعات: $soldCount"
                            tv.textSize = 16f
                            mostSoldContainer.addView(tv)
                        }
                    }
            }
            .addOnFailureListener {
                totalProductsText.text = "فشل جلب الإحصائيات "
            }
    }
}
