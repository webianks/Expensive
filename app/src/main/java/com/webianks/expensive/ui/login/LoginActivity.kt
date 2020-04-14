package com.webianks.expensive.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.webianks.expensive.ExpensiveApplication
import com.webianks.expensive.R
import com.webianks.expensive.data.DataManager
import com.webianks.expensive.ui.base.BaseActivity
import com.webianks.expensive.ui.main.MainActivity
import kotlinx.android.synthetic.main.login_activity_layout.*

class LoginActivity : BaseActivity(), LoginMvpView {

    private lateinit var loginPresenter: LoginPresenter<LoginMvpView>
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    companion object{
        private const val RC_SIGN_IN: Int = 1242
        private const val TAG: String = "Expensive"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity_layout)

        val signInButton: MaterialButton = findViewById(R.id.signInButton)

        mGoogleSignInClient = GoogleSignIn.getClient(this,
            (application as ExpensiveApplication).gso)
        auth = (application as ExpensiveApplication).auth

        val dataManager: DataManager = (application as ExpensiveApplication).dataManager
        loginPresenter = LoginPresenter(this,dataManager)
        loginPresenter.onAttach(this)

        signInButton.setOnClickListener {
            signIn()
        }

    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                loginPresenter.firebaseAuthenticationWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }


    public override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null)
            openMainActivity(currentUser)
    }

    override fun openMainActivity(user: FirebaseUser?) {
        if(user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("uid", user.uid)
            intent.putExtra("photo_url", user.photoUrl?.toString())
            intent.putExtra("name", user.displayName)
            intent.putExtra("email", user.email)
            startActivity(intent)
            finish()
        }else{
            Snackbar.make(loginFrame, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
        }
    }

}