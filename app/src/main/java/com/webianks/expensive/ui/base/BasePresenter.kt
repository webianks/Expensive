package com.webianks.expensive.ui.base

import com.webianks.expensive.data.DataManager


open class BasePresenter<V: MvpView>(val dataManager: DataManager)
    : MvpPresenter<V>{

    lateinit var mMvpView: V

    override fun onAttach(v: V) {
        this.mMvpView = v
    }
}