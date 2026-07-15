package com.qcwireless.sdksample.ext

import android.view.View
import android.view.ViewGroup

fun View.setMargin(left: Int?=null, top: Int?=null, right: Int?=null, bottom: Int ?=null) {
    val lp = layoutParams as ViewGroup.MarginLayoutParams
    left?.let {
        lp.leftMargin = it
    }

    top?.let {
        lp.topMargin = it
    }

    right?.let {
        lp.rightMargin = it
    }
    bottom?.let {
        lp.bottomMargin = it
    }
    layoutParams = lp
}

/**
 *  view visible
 */
fun View?.visible() {
    this?.visibility = View.VISIBLE
}

/**
 * view gone
 */
fun View?.gone() {
    this?.visibility = View.GONE
}