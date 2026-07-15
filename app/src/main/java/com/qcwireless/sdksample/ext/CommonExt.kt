package com.qcwireless.sdksample.ext

import android.content.res.Resources
import android.widget.Toast
import com.qcwireless.sdksample.app.MyApplication

/**
 * showToast。
 *
 * @param duration duration – How long to display the message.  Either {@link Toast#LENGTH_SHORT} or {@link Toast#LENGTH_LONG}
 */
fun CharSequence.showToast(duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(MyApplication.getApplication(), this, duration).show()
}

/* dp 转 px*/
val Number.dp
    get() = (this.toFloat() * Resources.getSystem().displayMetrics.density).toInt()

/* sp 转 px*/
val Number.sp
    get() = (this.toFloat() * Resources.getSystem().displayMetrics.scaledDensity).toInt()
