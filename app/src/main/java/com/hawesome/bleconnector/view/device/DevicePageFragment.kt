package com.hawesome.bleconnector.view.device

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hawesome.bleconnector.R
import com.hawesome.bleconnector.kit.BluetoothKit
import com.hawesome.bleconnector.kit.DeviceKit
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.model.DevicePage
import kotlinx.android.synthetic.main.device_page_fragment.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

/*
* 设备页面or次要页面
* newInstance(title: String, children: ArrayList<String>? = null)
* */
class DevicePageFragment : Fragment() {

    companion object {
        const val TAG = "DevicePageFragment"
        const val KEY_PAGE_TITLE = "title"
        const val KEY_SECONDARY_CHILDREN = "children"

        /*
        * children非空时：title页面下的次要页面
        * */
        fun newInstance(
            title: String? = null,
            children: ArrayList<String>? = null
        ): DevicePageFragment {
            val fragment = DevicePageFragment()
            fragment.arguments = Bundle().apply {
                putString(KEY_PAGE_TITLE, title)
                putStringArrayList(KEY_SECONDARY_CHILDREN, children)
            }
            return fragment
        }
    }

    private lateinit var devicePage: DevicePage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.device_page_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        initViews()
    }

    private fun initViews() {
        val title = arguments?.getString(KEY_PAGE_TITLE)
        val children = arguments?.getStringArrayList(KEY_SECONDARY_CHILDREN)
        devicePage = DeviceKit.getPage(title)!!
        //初始化UI
        val pageItemList = if (children.isNullOrEmpty()) devicePage.content.filter { !it.secondary }
        else children.map { child -> devicePage.content.first { it.name == child } }
        for (i in 0 until pageItemList.size) {
            val pageItem = pageItemList[i]
            pageItem.section?.let {
                val header = HeaderDisplay(requireContext(), pageItem)
                container.addView(header)
            }
            DeviceKit.divideItems(pageItem).forEach {
                val display = DeviceKit.getDisplay(requireContext(), it)
                container.addView(display)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //数据刷新:一次刷新+循环刷新
        Timer(false).schedule(500){
            TagKit.requestPageQuery(devicePage.addresses, devicePage.refresh)
        }
    }

    override fun onPause() {
        super.onPause()
        //清空刷新指令
        BluetoothKit.executeCMDList(listOf())
    }
}