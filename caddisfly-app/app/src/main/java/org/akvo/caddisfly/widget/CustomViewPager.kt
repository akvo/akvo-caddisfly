package org.akvo.caddisfly.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

//https://stackoverflow.com/questions/19602369/how-to-disable-viewpager-from-swiping-in-one-direction
class CustomViewPager : ViewPager {
    private var startX = 0f
    private var direction: SwipeDirection

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        direction = SwipeDirection.all
    }

    constructor(context: Context) : super(context) {
        direction = SwipeDirection.all
    }

    /*****DispatchTouchEvent for the View Pager to intercept and block swipes Right */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        try {
            when (ev.actionMasked and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    startX = ev.x
                    return super.dispatchTouchEvent(ev)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (ev.x - startX < 0 && direction == SwipeDirection.left) {
                        ev.action = MotionEvent.ACTION_CANCEL
                    } else if (ev.x - startX > 0 && direction == SwipeDirection.right) {
                        ev.action = MotionEvent.ACTION_CANCEL
                    }
                    super.dispatchTouchEvent(ev)
                }
            }
            return super.dispatchTouchEvent(ev)
        } catch (ignored: Exception) {
            return true
        }
    }

    fun setAllowedSwipeDirection(direction: SwipeDirection) {
        this.direction = direction
    }
}