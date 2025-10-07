package com.moda.ShoppingApp.activities.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.moda.ShoppingApp.R

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val backButton = findViewById<Button>(R.id.backToLoginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        val nameEdit = findViewById<EditText>(R.id.nameEditText)
        val emailEdit = findViewById<EditText>(R.id.emailEditText)
        val passEdit = findViewById<EditText>(R.id.passwordEditText)

        backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        registerButton.setOnClickListener {
            val name = nameEdit.text.toString()
            val email = emailEdit.text.toString()
            val pass = passEdit.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: ""
                            val userData = UserModel(name, email)

                            database.child(userId).setValue(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "تم التسجيل وحفظ البيانات", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "فشل حفظ البيانات: ${it.message}", Toast.LENGTH_SHORT).show()
                                }

                        } else {
                            Toast.makeText(this, "فشل التسجيل: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "الرجاء ملء جميع الحقول", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// نموذج المستخدم لتخزين البيانات
data class UserModel(
    val name: String = "",
    val email: String = ""
)
