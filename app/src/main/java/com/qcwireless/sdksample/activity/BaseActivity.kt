package com.qcwireless.sdksample.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.event.BluetoothEvent
import com.qcwireless.sdksample.event.MessageEvent
import com.qcwireless.sdksample.log.BleLogPanelController
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.ref.WeakReference

open class BaseActivity : AppCompatActivity() {

    private var isActive: Boolean = false

    protected var activity: Activity? = null

    private var activityWR: WeakReference<Activity>? = null

    protected val TAG: String = this.javaClass.simpleName

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    private lateinit var viewStatusBarPlace: View
    private lateinit var frameLayoutContentPlace: FrameLayout
    private var bleLogPanelController: BleLogPanelController? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bg_primary)
        super.setContentView(R.layout.activity_status_bar)
        viewStatusBarPlace = findViewById(R.id.view_status_bar_place)
        frameLayoutContentPlace = findViewById(R.id.frame_layout_content_place)
        setStatusBarBackground(getStatusBarColorRes())
        applySystemBarInsets()
        activity = this
        activityWR = WeakReference(activity!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        isActive = true
        bleLogPanelController?.onResume()
    }

    override fun onPause() {
        bleLogPanelController?.onPause()
        super.onPause()
        isActive = false
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        bleLogPanelController?.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
        activity = null
    }

    override fun setContentView(layoutResID: Int) {
        val contentView = LayoutInflater.from(this).inflate(layoutResID, frameLayoutContentPlace, false)
        setContentView(contentView)
    }

    override fun setContentView(layoutView: View) {
        frameLayoutContentPlace.removeAllViews()
        frameLayoutContentPlace.addView(layoutView)
        setupViews()
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        frameLayoutContentPlace.removeAllViews()
        frameLayoutContentPlace.addView(view, params)
        setupViews()
    }

    protected open fun setupViews() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        val navigateBefore = findViewById<ImageView>(R.id.ivNavigateBefore)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        navigateBefore?.setOnClickListener { finish() }
        tvTitle?.isSelected = true
        initBleLogPanelIfNeeded()
    }

    protected open fun isBleLogPanelVisibleByDefault(): Boolean = false

    protected open fun isBleLogFloatingVisibleByDefault(): Boolean = true

    protected open fun isBleLogPanelEnabled(): Boolean = true

    protected open fun shouldApplyContentBottomInsetPadding() = true

    protected open fun onContentSystemBarInsetsChanged(insets: Insets) {
    }

    @ColorRes
    protected open fun getStatusBarColorRes() = R.color.bg_title_bar

    fun setStatusBarBackground(@ColorRes statusBarColor: Int) {
        viewStatusBarPlace.setBackgroundResource(statusBarColor)
    }

    private fun initBleLogPanelIfNeeded() {
        if (!isBleLogPanelEnabled() || bleLogPanelController != null) {
            return
        }
        bleLogPanelController = BleLogPanelController(
            activity = this,
            panelVisibleByDefault = isBleLogPanelVisibleByDefault(),
            floatingVisibleByDefault = isBleLogFloatingVisibleByDefault()
        )
        bleLogPanelController?.bind()
    }

    private fun applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(frameLayoutContentPlace) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            viewStatusBarPlace.updateLayoutParams<ViewGroup.LayoutParams> {
                height = insets.top
            }
            if (shouldApplyContentBottomInsetPadding()) {
                view.updatePadding(bottom = insets.bottom)
            } else {
                view.updatePadding(bottom = 0)
                onContentSystemBarInsetsChanged(insets)
            }
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(frameLayoutContentPlace)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(messageEvent: MessageEvent) {
    }
}
