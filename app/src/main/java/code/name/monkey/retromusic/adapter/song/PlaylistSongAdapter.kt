package code.name.monkey.retromusic.adapter.song

import android.app.ActivityOptions
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.interfaces.CabHolder
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.util.NavigationUtil
import com.google.android.material.button.MaterialButton

open class PlaylistSongAdapter(
        activity: AppCompatActivity,
        dataSet: MutableList<CommonData>,
        itemLayoutRes: Int,
        cabHolder: CabHolder?
) : AbsOffsetSongAdapter(activity, dataSet, itemLayoutRes, cabHolder) {

    init {
        this.setMultiSelectMenuRes(R.menu.menu_cannot_delete_single_songs_playlist_songs_selection)
    }

    override fun createViewHolder(view: View, type: Int): SongAdapter.ViewHolder {
        return ViewHolder(view, type)
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        if (holder.itemViewType == OFFSET_ITEM) {
            val viewHolder = holder as ViewHolder
            viewHolder.playAction?.let {
                it.setOnClickListener {
                    MusicPlayerRemote.openQueue(activity, dataSet, 0, true)
                }
            }
            viewHolder.shuffleAction?.let {
                it.setOnClickListener {
                    MusicPlayerRemote.openAndShuffleQueue(activity, dataSet, true)
                }
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
        }
    }

    open inner class ViewHolder(itemView: View, val type: Int) :
            AbsOffsetSongAdapter.ViewHolder(itemView, type) {

        val playAction: MaterialButton? = itemView.findViewById(R.id.playAction)
        val shuffleAction: MaterialButton? = itemView.findViewById(R.id.shuffleAction)

        override var songMenuRes: Int
            get() = if (type == CommonData.TYPE_CLOUD_SONG) {
                R.menu.menu_item_cannot_delete_single_cloud_songs_playlist_song
            } else {
                R.menu.menu_item_cannot_delete_single_songs_playlist_song
            }
            set(value) {
                super.songMenuRes = value
            }

        override fun onSongMenuItemClick(item: MenuItem): Boolean {
            if (item.itemId == R.id.action_go_to_album) {
                if (song.localSong()) {
                    val activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                            activity,
                            imageContainerCard ?: image,
                            "${activity.getString(R.string.transition_album_art)}_${song.getLocalSong().albumId}"
                    )
                    NavigationUtil.goToAlbumOptions(
                            activity,
                            song.getLocalSong().albumId,
                            song,
                            activityOptions
                    )
                }
                return true
            }
            return super.onSongMenuItemClick(item)
        }
    }

    companion object {
        val TAG: String = PlaylistSongAdapter::class.java.simpleName
    }
}