package com.anubhav.codereverse.uiapp.data.model

import androidx.annotation.LayoutRes
import com.anubhav.codereverse.R

interface ItemViewModel {

    @get:LayoutRes
    var layoutId: Int

    var viewType: Int
        get() {return 0}
        set(v) {}

}

inline fun <reified IVM : ItemViewModel> getEmptyItem() : IVM {
    val data: IVM = IVM::class.java.newInstance()
    data.layoutId = R.layout.item_not_found
    data.viewType = R.layout.item_not_found
    return data
}