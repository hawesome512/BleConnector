package com.hawesome.bleconnector.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData

/*
* 监控点
* */
data class Tag(val name: String, val address: Int, var value: Int = 0):Parcelable {
    val valueLiveData = MutableLiveData<Int>()

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    fun updateValue(newValue: Int) {
        valueLiveData.value = newValue
        value = newValue
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(address)
        parcel.writeInt(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Tag> {

        const val NULL_VALUE = 0

        override fun createFromParcel(parcel: Parcel): Tag {
            return Tag(parcel)
        }

        override fun newArray(size: Int): Array<Tag?> {
            return arrayOfNulls(size)
        }
    }
}
