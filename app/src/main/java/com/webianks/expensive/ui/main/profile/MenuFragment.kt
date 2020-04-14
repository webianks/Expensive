package com.webianks.expensive.ui.main.profile

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.webianks.expensive.ExpensiveApplication
import com.webianks.expensive.R
import com.webianks.expensive.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment(R.layout.fragment_menu){

    private var imageUrl: String? = "--"
    private var email: String? = "--"
    private var name: String? = "--"

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    companion object{
        fun newInstance(userName: String, userEmail: String, userImage: String): MenuFragment {
            val menuFragment =
                MenuFragment()
            menuFragment.arguments = Bundle().apply {
                putString("user_name",userName)
                putString("user_email",userEmail)
                putString("user_image",userImage)
            }
            return menuFragment
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name = arguments?.getString("user_name")
        email = arguments?.getString("user_email")
        imageUrl = arguments?.getString("user_image")

        mGoogleSignInClient = GoogleSignIn.getClient(context!!, (context?.applicationContext as ExpensiveApplication).gso)
        auth = (context?.applicationContext  as ExpensiveApplication).auth

        logoutBt.setOnClickListener{
            confirmAndLogout()
        }

        user_name.text = name
        user_email.text = email

        Glide.with(this).load(imageUrl).into(userImageSheet)
    }

    private fun revokeAccess() {

         val dialog = ProgressDialog( context)
         dialog.setMessage("Logging out...Please wait.")
         dialog.setCancelable(false)
         dialog.show()

         auth.signOut()
         mGoogleSignInClient.revokeAccess().addOnCompleteListener {

             dialog.dismiss()

             startActivity(
                 Intent(context, LoginActivity::class.java)
             )
             activity?.finish()
         }
    }

    private fun confirmAndLogout() {

        MaterialAlertDialogBuilder(context)
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> revokeAccess() }
            .setNegativeButton("Cancel") { it, _ ->
                it.dismiss()
            }
            .show()
    }
}
