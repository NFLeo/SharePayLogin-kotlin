package com.leo.example

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.leo.simple.R

abstract class BaseDialogFragment : DialogFragment(), View.OnTouchListener {

    lateinit var mDialog: Dialog
    private var parentView: View? = null

    /**
     * 设置自定义对话框布局
     */
    abstract fun getLayoutId(): Int

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        setDialog()
        initView(mDialog)
        setView()
        return mDialog
    }

    // 设置基础对话框
    private fun setDialog() {
        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙。
        mDialog = Dialog(context!!, setThemeRes())
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 设置Content前设定
        parentView = LayoutInflater.from(context).inflate(getLayoutId(), null)
        mDialog.setContentView(parentView!!)
        mDialog.setCanceledOnTouchOutside(false) // 外部点击取消

        setLayoutParams()
        if (parentView != null) {
            parentView!!.setOnTouchListener(this)
        }
    }

    open fun setThemeRes(): Int {
        return R.style.DialogFragment
    }

    // 设置窗体尺寸
    open fun setLayoutParams() {
        val window = mDialog.window
        val lp = window!!.attributes
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.gravity = Gravity.BOTTOM
        window.attributes = lp
    }

    protected abstract fun setView()

    protected abstract fun initView(dialog: Dialog)

    open fun dismissParent() {
        dismiss()
    }

    override fun onDestroyView() {
        dismissParent()
        super.onDestroyView()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        activity!!.dispatchTouchEvent(event)
        return false
    }
}