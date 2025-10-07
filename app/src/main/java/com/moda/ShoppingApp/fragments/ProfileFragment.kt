package com.moda.ShoppingApp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.moda.ShoppingApp.activities.auth.LoginActivity
import com.moda.ShoppingApp.R

class    ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        val userImage = view.findViewById<ImageView>(R.id.profileImage)
        val emailText = view.findViewById<TextView>(R.id.userEmailText)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        emailText.text = "البريد الإلكتروني:\n${auth.currentUser?.email}"

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }
}
