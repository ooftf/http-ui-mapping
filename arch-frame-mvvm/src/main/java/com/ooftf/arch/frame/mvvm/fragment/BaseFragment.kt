package com.ooftf.arch.frame.mvvm.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.gyf.immersionbar.components.SimpleImmersionOwner
import com.ooftf.arch.frame.mvvm.R
import com.ooftf.arch.frame.mvvm.activity.BaseActivity
import com.ooftf.arch.frame.mvvm.immersion.Immersion
import com.ooftf.arch.frame.mvvm.utils.BackPressedHandler

/**
 * Created by master on 2016/4/12.
 */
abstract class BaseFragment : androidx.fragment.app.Fragment(), SimpleImmersionOwner,
    BackPressedHandler {
    private var mToast: Toast? = null
    private var touchable = false
    private var alive = false
    override fun onAttach(context: Context) {
        alive = true
        super.onAttach(context)
    }

    override fun onResume() {
        touchable = true
        super.onResume()
    }

    override fun onPause() {
        touchable = false
        super.onPause()
    }


    override fun onDetach() {
        alive = false
        super.onDetach()
    }

    fun toast(content: String) {
        mToast?.cancel()
        mToast = Toast.makeText(context?.applicationContext, content, Toast.LENGTH_SHORT)
        mToast?.setGravity(Gravity.CENTER, 0, 0)
        mToast?.show()
    }

    fun getBaseActivity(): BaseActivity {
        return activity as BaseActivity
    }

    //protected val mSimpleImmersionProxy = SimpleImmersionProxy(this)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (immersionBarEnabled()) {
            activity?.let {
                Immersion.setup(it, isDarkFont())
            }
        }
        //mSimpleImmersionProxy.onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        //mSimpleImmersionProxy.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        //mSimpleImmersionProxy.onHiddenChanged(hidden)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //mSimpleImmersionProxy.onConfigurationChanged(newConfig)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isInflateContentOnCreatedView()) {
            initImmersionBar()
        }
    }

    open fun isInflateContentOnCreatedView(): Boolean {
        return true
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        //mSimpleImmersionProxy.isUserVisibleHint = isVisibleToUser
    }

    fun isRealVisible(): Boolean {
        return view != null && getUserVisibleHint() && !isHidden() && isShowing()
    }

    fun isAlive(): Boolean = alive

    fun isShowing(): Boolean = showing

    var showing = false
    override fun onStart() {
        super.onStart()
        showing = true
    }

    override fun onStop() {
        showing = false
        super.onStop()
    }

    fun isTouchable(): Boolean = touchable

    open fun getToolbarId(): Int {
        return R.id.toolbar
    }

    open fun getToolbarIds(): IntArray {
        return intArrayOf(R.id.toolbar)
    }

    open fun getToolbar(): View? {
        return null
    }

    open fun getToolbars(): Array<View> {
        return emptyArray()
    }

    open fun isDarkFont(): Boolean {
        return true
    }

    /**
     * 是否可以实现沉浸式，当为true的时候才可以执行initImmersionBar方法
     * Immersion bar enabled boolean.
     *
     * @return the boolean
     */
    override fun immersionBarEnabled(): Boolean {
        return true
    }


    override fun initImmersionBar() {
        if (!immersionBarEnabled()) {
            return
        }
        /*val immersionBar =
            ImmersionBar.with(this).statusBarDarkFont(isDarkFont()).keyboardEnable(true)
        var toolbar = getToolbar()
        if (toolbar == null) {
            toolbar = view?.findViewById<View>(getToolbarId())
        }
        if (toolbar != null) {
            immersionBar.titleBar(toolbar)
        }
        immersionBar.navigationBarColorInt(Color.WHITE).init()*/

        var list: MutableList<View> = ArrayList()
        getToolbarIds().forEach {
            view?.findViewById<View>(it)?.let { it ->
                list.add(it)
            }
        }
        view?.findViewById<View>(getToolbarId())?.let { it ->
            list.add(it)
        }

        getToolbar()?.let {
            list.add(it)
        }
        getToolbars().forEach {
            list.add(it)
        }
        Immersion.fitStatusBar(*list.toTypedArray())
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}
