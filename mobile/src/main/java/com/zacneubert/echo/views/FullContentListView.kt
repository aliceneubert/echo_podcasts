package com.zacneubert.echo.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ListView

class FullContentListView(context: Context, attrs: AttributeSet) : ListView(context, attrs) {
    private var oldCount = 0

    override fun onDraw(canvas: Canvas?) {
        if (count != oldCount && count > 0) {
            val height = getChildAt(0).height + 1
            oldCount = count

            val adjustedParams = layoutParams
            adjustedParams.height = count * height
            layoutParams = adjustedParams
        }

        super.onDraw(canvas)
    }
}