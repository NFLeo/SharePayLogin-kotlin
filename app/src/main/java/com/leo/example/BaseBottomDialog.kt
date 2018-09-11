package com.leo.example

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.Gravity
import android.view.WindowManager
import com.leo.simple.R

class BaseBottomDialog : BaseDialogFragment() {

    override fun getLayoutId() = resourceId

    private var resourceId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null != arguments) {
            resourceId = arguments!!.getInt(BOTTOM_KEY)
        }
    }

    override fun setThemeRes(): Int {
        return R.style.BottomDialogFragment
    }

    override fun setView() {
        mDialog.setCanceledOnTouchOutside(true)
        val window = mDialog.window
        val lp = window.attributes
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.gravity = Gravity.BOTTOM

        window.attributes = lp
    }

    override fun initView(dialog: Dialog) {}

    override fun show(manager: FragmentManager, tag: String) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
        // 解决 调用show后无法获取布局
        manager.executePendingTransactions()
    }

    companion object {
        const val BOTTOM_KEY = "BOTTOM_KEY"

        fun newInstance(resourceId: Int): BaseBottomDialog {
            val dialog = BaseBottomDialog()
            val bundle = Bundle()
            bundle.putInt(BaseBottomDialog.BOTTOM_KEY, resourceId)
            dialog.arguments = bundle
            return dialog
        }
    }
}
