package com.webianks.expensive

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.*


class DashboardViewModel : ViewModel() {

    private var currentMonthDataSnapshot: MutableLiveData<QuerySnapshot>? = null

    fun getCurrentMonthData(
        uid: String,
        firstDateOfThisMonth: Date,
        lastDateOfThisMonth: Date
    ): MutableLiveData<QuerySnapshot>? {

        if (currentMonthDataSnapshot == null) {
            currentMonthDataSnapshot = MutableLiveData()
            loadCurrentMonthData(uid, firstDateOfThisMonth, lastDateOfThisMonth)
        }

        return currentMonthDataSnapshot
    }

    private fun loadCurrentMonthData(
        uid: String,
        firstDateOfThisMonth: Date,
        lastDateOfThisMonth: Date
    ) {

        val db = FirebaseFirestore.getInstance()

        db.collection(Util.EXPENSE_COLLECTION)
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("date", firstDateOfThisMonth)
            .whereLessThanOrEqualTo("date", lastDateOfThisMonth)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                currentMonthDataSnapshot?.value = result
            }
            .addOnFailureListener { exception ->

                currentMonthDataSnapshot?.value = null

                Log.w(Util.TAG, "Error getting documents.", exception)
            }
    }

}