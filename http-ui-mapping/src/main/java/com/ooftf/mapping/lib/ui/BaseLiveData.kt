package com.ooftf.mapping.lib.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.facade.Postcard
import com.ooftf.basic.utils.ThreadUtil
import com.ooftf.mapping.lib.LogUtil
import com.ooftf.mapping.lib.LostMutableLiveData
import com.ooftf.widget.statelayout.IStateLayout
import java.util.*
import kotlin.collections.HashMap

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/6/13 0013
 */
class BaseLiveData {
    /**
     * //finish
     */
    val finishLiveData by lazy { LostMutableLiveData<Int>() }

    /**
     * //finish
     */
    val finishWithData by lazy { LostMutableLiveData<FinishData>() }

    /**
     * //finish
     */
    val finishActivity by lazy { LostMutableLiveData<FinishData>() }

    /**
     * start
     */
    val startActivityLiveData by lazy { LostMutableLiveData<Postcard>() }

    /**
     * errorMessage
     */
    val messageLiveData by lazy { LostMutableLiveData<String>() }

    /**
     * showLoading
     */
    val showLoading by lazy { MutableLiveData<MutableList<Cancelable>>() }
    val smartRefresh by lazy { MutableLiveData<Int>() }
    val smartLoadMore by lazy { MutableLiveData<Int>() }
    val stateLayout by lazy { MutableLiveData<Int>() }
    private val dataListLiveData by lazy { HashMap<Class<out Any>, MutableLiveData<out Any>>() }

    /**
     * errorMessage
     */
    internal val invalidateBinding by lazy { LostMutableLiveData<String>() }

    /**
     * finish
     */
    fun finish() {
        finishLiveData.value = Activity.RESULT_CANCELED
    }

    fun bindingInvalidateAll() {
        invalidateBinding.postValue("")
    }

    fun <T : Any> getLiveData(tClass: Class<T>): MutableLiveData<T> {
        var mutableLiveData: MutableLiveData<T>? = dataListLiveData[tClass] as MutableLiveData<T>?
        if (mutableLiveData == null) {
            mutableLiveData = MutableLiveData()
            dataListLiveData[tClass] = mutableLiveData
        }
        return mutableLiveData
    }

    fun <T : Any> post(data: T) {
        getLiveData(data.javaClass).postValue(data)
    }

    /**
     * finish
     */
    fun finish(result: Int) {
        finishLiveData.value = result
    }

    /**
     * finish
     */
    @Deprecated("推荐使用 finishActivity 这两个获取 数据的方式不同，要注意下", ReplaceWith("finishActivity"))
    fun finish(result: FinishData) {
        finishWithData.value = result
    }

    fun finishActivity(result: FinishData? = null) {
        if (result == null) {
            finish()
        } else {
            finishActivity.postValue(result)
        }
    }

    fun startActivity(postcart: Postcard) {
        startActivityLiveData.postValue(postcart)
    }

    /**
     * showDialog
     */
    fun showDialog(call: Cancelable) {
        ThreadUtil.runOnUiThread() {
            var value = showLoading.value
            if (value == null) {
                value = ArrayList<Cancelable>()
                value.add(call)
            } else {
                value.add(call)
            }
            showLoading.setValue(value)
        }
    }

    /**
     * dismissDialog
     */
    fun dismissDialog(call: Cancelable?) {
        ThreadUtil.runOnUiThread {
            var value = showLoading.value
            if (value == null) {
                value = ArrayList()
            } else {
                value.remove(call)
            }
            showLoading.setValue(value)
        }
    }

    val isSmartLoading: Boolean
        get() {
            val value = smartRefresh.value
            return value != null && value > 0
        }

    val isStateLayoutLoading: Boolean
        get() {
            val value = stateLayout.value
            return value != null && value == IStateLayout.STATE_LOAD
        }

    fun showMessage(message: String) {
        messageLiveData.postValue(message)
    }

    fun startRefresh() {
        ThreadUtil.runOnUiThread {
            if (smartRefresh.value == null) {
                smartRefresh.setValue(1)
            } else {
                smartRefresh.setValue(smartRefresh.value!! + 1)
            }
        }
    }

    fun finishRefresh() {
        LogUtil.e("postFinishRefresh")
        ThreadUtil.runOnUiThread {
            if (smartRefresh.value == null) {
                smartRefresh.setValue(0)
            } else {
                smartRefresh.setValue(smartRefresh.value!! - 1)
            }
        }
    }

    fun finishLoadMore() {
        LogUtil.e("postFinishLoadMore")
        smartLoadMore.postValue(UIEvent.SMART_LAYOUT_LOADMORE_FINISH)
    }

    fun finishLoadMoreSuccess() {
        LogUtil.e("postFinishLoadMoreSuccess")
        smartLoadMore.postValue(UIEvent.SMART_LAYOUT_LOADMORE_FINISH_SUCCESS)
    }

    fun finishLoadMoreWithNoMoreData() {
        LogUtil.e("postFinishLoadMoreWithNoMoreData")
        smartLoadMore.postValue(UIEvent.SMART_LAYOUT_LOADMORE_FINISH_AND_NO_MORE)
    }

    fun switchToEmpty() {
        LogUtil.e("postSwitchToEmpty")
        ThreadUtil.runOnUiThread { stateLayout.setValue(IStateLayout.STATE_EMPTY) }
    }

    fun switchToLoading() {
        LogUtil.e("postSwitchToLoading")
        ThreadUtil.runOnUiThread { stateLayout.setValue(IStateLayout.STATE_LOAD) }
    }

    fun switchToError() {
        LogUtil.e("postSwitchToError")
        ThreadUtil.runOnUiThread { stateLayout.setValue(IStateLayout.STATE_ERROR) }
    }

    fun switchToSuccess() {
        LogUtil.e("postSwitchToSuccess")
        ThreadUtil.runOnUiThread { stateLayout.setValue(IStateLayout.STATE_SUCCESS) }
    }

    fun attach(owner: LifecycleOwner, activity: Activity): BaseLiveDataObserve {
        return BaseLiveDataObserve(this, owner, activity)
    }

    fun attach(activity: AppCompatActivity): BaseLiveDataObserve {
        return BaseLiveDataObserve(this, activity, activity)
    }

    fun attach(fragment: Fragment): BaseLiveDataObserve {
        return BaseLiveDataObserve(this, fragment, fragment.activity!!)
    }

    private val singleMap: MutableMap<Any, MutableLiveData<Int>> = HashMap()
    private val multipleMap: MutableMap<Any, MutableLiveData<Int>> = HashMap()
    fun singleLoading(tag: Any) {
        val value = getSingleValue(tag)
        ThreadUtil.runOnUiThread { value.setValue(UIEvent.Single.LOADING) }
    }

    fun singleSuccess(tag: Any) {
        val value = getSingleValue(tag)
        ThreadUtil.runOnUiThread { value.setValue(UIEvent.Single.SUCCESS) }
    }

    fun singleFail(tag: Any) {
        val value = getSingleValue(tag)
        ThreadUtil.runOnUiThread { value.setValue(UIEvent.Single.FAIL) }
    }

    fun getSingleValue(tag: Any): MutableLiveData<Int> {
        var data = singleMap[tag]
        if (data == null) {
            data = MutableLiveData()
            singleMap[tag] = data
        }
        return data
    }

    fun addMultiple(tag: Any) {
        val value = getMultipleValue(tag)
        ThreadUtil.runOnUiThread { value.setValue(value.value!! + 1) }
    }

    fun lessMultiple(tag: Any) {
        val value = getMultipleValue(tag)
        ThreadUtil.runOnUiThread { value.setValue(value.value!! - 1) }
    }

    fun getMultipleValue(tag: Any): MutableLiveData<Int> {
        var data = multipleMap[tag]
        if (data == null) {
            data = MutableLiveData()
            data.value = 0
            multipleMap[tag] = data
        }
        return data
    }
}