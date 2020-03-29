package com.webianks.expensive.ui.login

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.webianks.expensive.ui.base.MvpPresenter

interface LoginMvpPresenter<V: LoginMvpView> : MvpPresenter<V>{

    fun firebaseAuthenticationWithGoogle(acct: GoogleSignInAccount?)
}