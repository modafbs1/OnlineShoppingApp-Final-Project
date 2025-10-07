package com.moda.ShoppingApp.activities.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.activities.dashboard.AdminHomeActivity
import com.moda.ShoppingApp.activities.dashboard.UserMainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEdit: EditText
    private lateinit var passEdit: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEdit = findViewById(R.id.emailEditText)
        passEdit = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)

        loginButton.setOnClickListener { loginUser() }
        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // التحقق إذا كان المستخدم مسجل دخول مسبقاً
        auth.currentUser?.let { user ->
            val intent = if (user.email == "admin@gmail.com") {
                Intent(this, AdminHomeActivity::class.java)
            } else {
                Intent(this, UserMainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun loginUser() {
        val email = emailEdit.text.toString().trim()
        val password = passEdit.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "الرجاء ملء البريد وكلمة المرور", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "تم تسجيل الدخول", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    val intent = if (user?.email == "admin@gmail.com") {
                        Intent(this, AdminHomeActivity::class.java)
                    } else {
                        Intent(this, UserMainActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "فشل تسجيل الدخول: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
