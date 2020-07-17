package code.name.monkey.retromusic.interfaces

import android.os.Parcelable
import code.name.monkey.retromusic.model.CommonData

interface CommonDataConverter : Parcelable {
    fun convertToCommonData(): CommonData
}