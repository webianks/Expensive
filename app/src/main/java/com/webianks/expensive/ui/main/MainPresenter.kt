package com.webianks.expensive.ui.main

import com.webianks.expensive.data.DataManager
import com.webianks.expensive.ui.base.BasePresenter

class MainPresenter<V: MainMvpView>(dataManager: DataManager):
    BasePresenter<V>(dataManager), MainMvpPresenter<V>{

    override fun getCurrentMonthData() {

    }
}