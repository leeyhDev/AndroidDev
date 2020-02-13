package com.leeyh.model.bean

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class SystemType(
    @SerializedName("children")
    val children: List<SystemChildren>,
    @SerializedName("courseId")
    val courseId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("order")
    val order: Int,
    @SerializedName("parentChapterId")
    val parentChapterId: Int,
    @SerializedName("userControlSetTop")
    val userControlSetTop: Boolean,
    @SerializedName("visible")
    val visible: Int
)

data class SystemChildren(
    @SerializedName("courseId")
    val courseId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("order")
    val order: Int,
    @SerializedName("parentChapterId")
    val parentChapterId: Int,
    @SerializedName("userControlSetTop")
    val userControlSetTop: Boolean,
    @SerializedName("visible")
    val visible: Int
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readInt(),
        source.readString().toString(),
        source.readInt(),
        source.readInt(),
        1 == source.readInt(),
        source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(courseId)
        writeInt(id)
        writeString(name)
        writeInt(order)
        writeInt(parentChapterId)
        writeInt((if (userControlSetTop) 1 else 0))
        writeInt(visible)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SystemChildren> = object : Parcelable.Creator<SystemChildren> {
            override fun createFromParcel(source: Parcel): SystemChildren = SystemChildren(source)
            override fun newArray(size: Int): Array<SystemChildren?> = arrayOfNulls(size)
        }
    }
}


