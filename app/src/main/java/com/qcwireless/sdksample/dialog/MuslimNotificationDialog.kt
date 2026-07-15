package com.qcwireless.sdksample.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.qcwireless.sdksample.R
import com.qcwireless.sdksample.databinding.DialogFragmentNotificationBinding
import com.qcwireless.sdksample.ext.dp
import com.qcwireless.sdksample.ext.setMargin


import kotlin.let
import kotlin.run

class NotificationDialog : DialogFragment() {

    /**
     * 标题
     */
    private var title: String? = null

    /**
     * 内容
     */
    private var content: String? = null
    /**
     * 确认按钮文字
     */
    private var confirmMessage: String? = null
    /**
     * 取消按钮文字
     */
    private var cancelMessage: String? = null

    /**
     * 是否隐藏取消按钮
     */
    private var hideCancelButton:Boolean = false

    /**
     * 是否允许返回键取消弹框
     */
    private var cancelable = true

    /**
     * 是否允许外部点击取消
     */
    private var canceledOnTouchOutside = true

    /**
     * 标题距离顶部距离 dp
     */
    private var titleMarginTop:Int = 26.dp


    private lateinit var binding: DialogFragmentNotificationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Customer_Dialog)
        arguments?.let {
            content = it.getString(DIALOG_CONTENT)
            title = it.getString(DIALOG_TITLE)
            confirmMessage = it.getString(DIALOG_CONFIRM_MSG)
            cancelMessage = it.getString(DIALOG_CANCEL_MSG)
            cancelable = it.getBoolean(DIALOG_CANCELABLE,true)
            canceledOnTouchOutside = it.getBoolean(DIALOG_CANCELABLE_OUTSIDE,true)
            hideCancelButton = it.getBoolean(DIALOG_HIDE_CANCEL_BUTTON)
            titleMarginTop = it.getInt(DIALOG_TITLE_MARGIN_TOP)
        }
    }



    private var onConfirm: ((view: View) -> Unit)? = null
    private var onCancel: ((view: View) -> Unit)? = null
    fun setOnConfirmListener(listener: (view: View) -> Unit) {
        this.onConfirm = listener
    }

    fun setOnCancelListener(listener: (view: View) -> Unit) {
        this.onCancel = listener
    }
    private var dismiss:(()->Unit)?=null
    fun  setOnDismissListener(action:()->Unit){
        this.dismiss = action
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismiss?.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        binding = DialogFragmentNotificationBinding.inflate(inflater, container, false)
        fillData()
        return binding.getRoot()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(cancelable) // 禁用返回键
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside) // 禁用外部点击
        dialog.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                !cancelable // 拦截返回键
            } else {
                false
            }
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        // 设置弹窗宽度和高度
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun fillData() {

        val ivClose = binding.ivClose
        ivClose.setOnClickListener {
            dismiss()
        }


        binding.run {
            tvTitle.text = title
            tvContent.text = content
            tvConfirm.text = confirmMessage

            tvTitle.setMargin(top = titleMarginTop)
        }

        binding.tvConfirm.setOnClickListener {
            onConfirm?.invoke(it)
            dismiss()
        }
    }

    companion object {
        const val DIALOG_TITLE = "dialog_title"
        const val DIALOG_CONTENT = "dialog_content"
        const val DIALOG_CONFIRM_MSG = "dialog_confirm_msg"
        const val DIALOG_CANCEL_MSG = "dialog_cancel_msg"
        const val DIALOG_CANCELABLE = "dialog_cancelable"
        const val DIALOG_CANCELABLE_OUTSIDE = "dialog_cancelable_outside"
        const val DIALOG_HIDE_CANCEL_BUTTON = "dialog_hide_cancel_button"
        const val DIALOG_TITLE_MARGIN_TOP = "dialog_title_margin_top"

        fun newInstance(): NotificationDialog {
            return NotificationDialog()
        }
    }

    class Builder {
        private var content: String? = null
        private var title: String? = null
        private var confirmMessage: String? = null
        private var cancelMessage: String? = null
        private var cancelable: Boolean? = null
        private var canceledOnTouchOutside: Boolean? = null
        private var hideCancelButton = false
        private var titleMarginTop = 26.dp

        fun isCancelable(cancelable:Boolean):Builder{
            this.cancelable = cancelable
            return this
        }


        fun hideCancelButton(hideCancelButton:Boolean):Builder{
            this.hideCancelButton = hideCancelButton
            return this

        }


        fun titleMarginTop(margin:Int):Builder{
            this.titleMarginTop = margin
            return this
        }



        fun isCanceledOnTouchOutside(canceledOnTouchOutside: Boolean):Builder{
            this.canceledOnTouchOutside = canceledOnTouchOutside
            return this
        }

        fun setContent(content: String): Builder {
            this.content = content
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }
        fun setConfirmMessage(msg: String): Builder {
            this.confirmMessage = msg
            return this
        }
        fun setCancelMessage(msg: String): Builder {
            this.cancelMessage = msg
            return this
        }

        fun build(): NotificationDialog {
            val fragment = newInstance()
            val args = Bundle()
            content?.let {
                args.putString(DIALOG_CONTENT, it)
            }
            title?.let {
                args.putString(DIALOG_TITLE, it)
            }
            confirmMessage?.let {
                args.putString(DIALOG_CONFIRM_MSG, it)
            }
            cancelMessage?.let {
                args.putString(DIALOG_CANCEL_MSG, it)
            }
            cancelable?.let {
                args.putBoolean(DIALOG_CANCELABLE,it)
            }
            canceledOnTouchOutside?.let {
                args.putBoolean(DIALOG_CANCELABLE_OUTSIDE,it)
            }
            args.putInt(DIALOG_TITLE_MARGIN_TOP,titleMarginTop)
            args.putBoolean(DIALOG_HIDE_CANCEL_BUTTON,hideCancelButton)
            fragment.arguments = args
            return fragment
        }
    }
}