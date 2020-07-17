package code.name.monkey.retromusic.adapter.song

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.interfaces.CabHolder
import code.name.monkey.retromusic.model.CommonData
import com.google.android.material.button.MaterialButton

class ShuffleButtonSongAdapter(
    activity: AppCompatActivity,
    dataSet: MutableList<CommonData>,
    itemLayoutRes: Int,
    cabHolder: CabHolder?
) : AbsOffsetSongAdapter(activity, dataSet, itemLayoutRes, cabHolder) {

    override fun createViewHolder(view: View, type: Int): SongAdapter.ViewHolder {
        return ViewHolder(view, type)
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        if (holder.itemViewType == OFFSET_ITEM) {
            val viewHolder = holder as ViewHolder
            viewHolder.playAction?.setOnClickListener {
                MusicPlayerRemote.openQueue(activity, dataSet, 0, true)
            }
            viewHolder.shuffleAction?.setOnClickListener {
                MusicPlayerRemote.openAndShuffleQueue(activity, dataSet, true)
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
        }
    }

    inner class ViewHolder(itemView: View, type: Int) : AbsOffsetSongAdapter.ViewHolder(itemView, type) {
        val playAction: MaterialButton? = itemView.findViewById(R.id.playAction)
        val shuffleAction: MaterialButton? = itemView.findViewById(R.id.shuffleAction)

        override fun onClick(v: View?) {
            if (itemViewType == OFFSET_ITEM) {
                MusicPlayerRemote.openAndShuffleQueue(activity, dataSet, true)
                return
            }
            super.onClick(v)
        }
    }
}