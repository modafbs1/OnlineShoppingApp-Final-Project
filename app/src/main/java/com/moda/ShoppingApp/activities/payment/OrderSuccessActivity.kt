package com.moda.ShoppingApp.activities.payment

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.activities.dashboard.UserMainActivity

class OrderSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_success)

        val backButton = findViewById<Button>(R.id.backToHomeButton)
        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        backButton.setOnClickListener {

            val intent = Intent(this, UserMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
