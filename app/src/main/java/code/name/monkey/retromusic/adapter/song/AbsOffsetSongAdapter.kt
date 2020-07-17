package code.name.monkey.retromusic.adapter.song

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.interfaces.CabHolder
import code.name.monkey.retromusic.model.CommonData

abstract class AbsOffsetSongAdapter(
    activity: AppCompatActivity,
    dataSet: MutableList<CommonData>,
    @LayoutRes itemLayoutRes: Int,
    cabHolder: CabHolder?
) : SongAdapter(activity, dataSet, itemLayoutRes, cabHolder) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.ViewHolder {
        if (viewType == OFFSET_ITEM) {
            val view = LayoutInflater.from(activity)
                .inflate(R.layout.item_list_quick_actions, parent, false)
            return createViewHolder(view, curType)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun createViewHolder(view: View, type: Int): SongAdapter.ViewHolder {
        return ViewHolder(view, type)
    }

    override fun getItemId(position: Int): Long {
        var positionFinal = position
        positionFinal--
        return if (positionFinal < 0) -2 else super.getItemId(positionFinal)
    }

    override fun getIdentifier(position: Int): CommonData? {
        var positionFinal = position
        positionFinal--
        return if (positionFinal < 0) null else super.getIdentifier(positionFinal)
    }

    override fun getItemCount(): Int {
        val superItemCount = super.getItemCount()
        return if (superItemCount == 0) 0 else superItemCount + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position > 0) {
            curType = dataSet[position - 1].dataType
        }
        return if (position == 0) OFFSET_ITEM else curType
    }

    open inner class ViewHolder(itemView: View, type: Int) :
        SongAdapter.ViewHolder(itemView, type) {

        override // could also return null, just to be safe return empty song
        val song: CommonData
            get() = if (itemViewType == OFFSET_ITEM) CommonData(CommonData.TYPE_EMPTY) else dataSet[adapterPosition - 1]

        override fun onClick(v: View?) {
            if (dataSet[adapterPosition - 1].dataType != CommonData.TYPE_NATIVE_ADS) {
                if (isInQuickSelectMode && itemViewType != OFFSET_ITEM) {
                    toggleChecked(adapterPosition)
                } else {
                    MusicPlayerRemote.openQueue(activity, dataSet, adapterPosition - 1, true)
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (itemViewType == OFFSET_ITEM || dataSet[adapterPosition - 1].dataType == CommonData.TYPE_NATIVE_ADS) return false
            toggleChecked(adapterPosition)
            return true
        }
    }

    companion object {
        const val OFFSET_ITEM = 0
    }
}