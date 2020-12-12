package com.webianks.expensive.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DataManager(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore) {
}