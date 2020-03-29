package com.webianks.expensive

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExpensiveApplication : Application(){

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }
}