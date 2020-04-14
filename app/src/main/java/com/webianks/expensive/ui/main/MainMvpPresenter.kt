package com.webianks.expensive.ui.main

import com.webianks.expensive.ui.base.MvpPresenter
import com.webianks.expensive.ui.main.this_month.MainMvpView

interface MainMvpPresenter<V: MainMvpView> : MvpPresenter<V> {

    fun getCurrentMonthData()
}