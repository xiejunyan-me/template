package com.example.learndemo

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.learndemo.util.log

class MyConstraintHelper(context: Context?, attrs: AttributeSet?) :
    ConstraintHelper(context, attrs) {

    /**
     * onLayout完成之后调用
     */
    override fun updatePostLayout(container: ConstraintLayout?) {
        getViews(container).forEach {
            startScaleAnimation(it)
        }
    }

    private fun startScaleAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f, 1.5f, 1f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = 3
        }.start()
        ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f, 1.5f, 1f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = 3
        }.start()

    }
}