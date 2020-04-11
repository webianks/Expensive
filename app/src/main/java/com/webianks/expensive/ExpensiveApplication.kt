package com.webianks.expensive

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.webianks.expensive.data.DataManager

class ExpensiveApplication : Application(){

    lateinit var gso: GoogleSignInOptions
    lateinit var dataManager: DataManager
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        dataManager = DataManager(auth,db)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

    }
}