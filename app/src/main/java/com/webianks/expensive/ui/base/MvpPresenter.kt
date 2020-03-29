package com.webianks.expensive.ui.base

interface MvpPresenter<V : MvpView> {

    fun onAttach(v: V)

}