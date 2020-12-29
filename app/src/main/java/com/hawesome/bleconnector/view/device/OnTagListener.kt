package com.hawesome.bleconnector.view.device

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import com.hawesome.bleconnector.kit.TagKit
import com.hawesome.bleconnector.model.DevicePageItem
import com.hawesome.bleconnector.model.Tag
import java.time.LocalDateTime

interface OnTagListener {
    fun observeTagUpdate(
        context: Context,
        pageItem: DevicePageItem,
        block: (tags: List<Tag>) -> Unit
    ) {
        pageItem.tags?.let {
            val tags = TagKit.getTags(it)
            val tagsLiveData = MediatorLiveData<LocalDateTime>()
            tags.forEach {
                tagsLiveData.addSource(it.valueLiveData) {
                    tagsLiveData.postValue(LocalDateTime.now())
                }
            }
            tagsLiveData.observe(context as FragmentActivity) {
                block(tags)
            }

        }
    }
}