package com.webianks.expensive.ui.month_year_picker.custom_number_picker

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.util.AttributeSet
import android.widget.NumberPicker
import androidx.appcompat.content.res.AppCompatResources
import com.webianks.expensive.R
import java.lang.reflect.Field

/**
 * This class changes color of NumberPicker divider using reflection
 */
class NumberPickerWithColor(
    context: Context?,
    attrs: AttributeSet?
) : NumberPicker(context, attrs) {
    init {

        var numberPickerClass: Class<*>? = null
        try {
            numberPickerClass = Class.forName("android.widget.NumberPicker")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        var selectionDivider: Field? = null
        try {
            if (numberPickerClass != null) selectionDivider =
                numberPickerClass.getDeclaredField("mSelectionDivider")
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        try {
            if (selectionDivider != null) {
                selectionDivider.isAccessible = true
                selectionDivider[this] =
                    AppCompatResources.getDrawable(context!!, R.drawable.picker_divider_color)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}