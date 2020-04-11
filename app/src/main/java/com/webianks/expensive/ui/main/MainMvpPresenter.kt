package com.webianks.expensive.ui.main

import com.webianks.expensive.ui.base.MvpPresenter

interface MainMvpPresenter<V: MainMvpView> : MvpPresenter<V> {

    fun getCurrentMonthData()
}