package com.webianks.expensive.ui.login

import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import com.webianks.expensive.data.DataManager
import com.webianks.expensive.ui.base.BasePresenter

class LoginPresenter<V: LoginMvpView>(
    private val activity: Activity,
    dataManager: DataManager)
    : BasePresenter<V>(dataManager), LoginMvpPresenter<V>{

    companion object{
        const val TAG = "LoginPresenter"
    }

    override fun firebaseAuthenticationWithGoogle(acct: GoogleSignInAccount?) {

        acct?.let {
            Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)
            val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
            dataManager.auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")
                        val user = dataManager.auth.currentUser
                        mMvpView.openMainActivity(user)
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        mMvpView.openMainActivity(null)
                    }
                }
        }
    }

}