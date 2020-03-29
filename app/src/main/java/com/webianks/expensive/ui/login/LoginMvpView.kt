package com.webianks.expensive.ui.login

import com.google.firebase.auth.FirebaseUser
import com.webianks.expensive.ui.base.MvpView

interface LoginMvpView : MvpView{

    fun openMainActivity(user: FirebaseUser?)

}