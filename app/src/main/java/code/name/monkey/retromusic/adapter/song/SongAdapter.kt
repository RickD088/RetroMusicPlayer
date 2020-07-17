package code.name.monkey.retromusic.adapter.song

import android.app.ActivityOptions
import android.graphics.drawable.Drawable
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.abram.AbramAds
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.abram.Constants.DOWNLOAD_ASSUMED_COMPLETION_PERCENTAGE
import code.name.monkey.retromusic.adapter.base.AbsMultiSelectAdapter
import code.name.monkey.retromusic.adapter.base.MediaEntryViewHolder
import code.name.monkey.retromusic.extensions.hide
import code.name.monkey.retromusic.extensions.show
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget
import code.name.monkey.retromusic.glide.SongGlideRequest
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.helper.SortOrder
import code.name.monkey.retromusic.helper.menu.SongMenuHelper
import code.name.monkey.retromusic.helper.menu.SongsMenuHelper
import code.name.monkey.retromusic.interfaces.CabHolder
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.rest.music.model.SongBean
import code.name.monkey.retromusic.util.FileUtil
import code.name.monkey.retromusic.util.MusicUtil
import code.name.monkey.retromusic.util.NavigationUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import com.afollestad.materialcab.MaterialCab
import com.bumptech.glide.Glide
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.util.*

/**
 * Created by hemanths on 13/08/17.
 */

open class SongAdapter(
        protected val activity: AppCompatActivity,
        var dataSet: MutableList<CommonData>,
        protected var itemLayoutRes: Int,
        cabHolder: CabHolder?,
        showSectionName: Boolean = true
) : AbsMultiSelectAdapter<SongAdapter.ViewHolder, CommonData>(
        activity,
        cabHolder,
        R.menu.menu_media_selection
), MaterialCab.Callback, PopupTextProvider {

    private var showSectionName = true
    var curType = CommonData.TYPE_LOCAL_SONG

    init {
        this.showSectionName = showSectionName
        this.setHasStableIds(true)
    }

    open fun swapDataSet(dataSet: List<CommonData>) {
        this.dataSet = dataSet.toMutableList()

        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        val item = dataSet[position]
        return when {
            localSong(item) -> getLocalSong(position).id.toLong()
            cloudSong(item) -> getCloudSong(position).id.toLong()
            else -> position.toLong()
        }
    }

    private fun localSong(position: Int): Boolean {
        return localSong(dataSet[position])
    }

    private fun localSong(data: CommonData): Boolean {
        return data.dataType == CommonData.TYPE_LOCAL_SONG
    }

    private fun cloudSong(data: CommonData): Boolean {
        return data.dataType == CommonData.TYPE_CLOUD_SONG
    }

    private fun getLocalSong(position: Int): Song {
        return getLocalSong(dataSet[position])
    }

    private fun getLocalSong(data: CommonData): Song {
        return data.localData as Song
    }

    private fun getCloudSong(position: Int): SongBean {
        return getCloudSong(dataSet[position])
    }

    private fun getCloudSong(data: CommonData): SongBean {
        return data.cloudData as SongBean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(
                LayoutInflater.from(activity).inflate(
                        itemLayoutRes,
                        parent,
                        false),
                curType)
    }

    protected open fun createViewHolder(view: View, type: Int): ViewHolder {
        return ViewHolder(view, type)
    }

    override fun getItemViewType(position: Int): Int {
        curType = dataSet[position].dataType
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (curType == CommonData.TYPE_NATIVE_ADS) {
        } else {
            val song = dataSet[position]
            val isChecked = isChecked(song)
            holder.itemView.isActivated = isChecked
            if (isChecked) {
                holder.menu?.hide()
            } else {
                holder.menu?.show()
            }
            holder.title?.text = getSongTitle(song)
            holder.text?.text = getSongText(song)
            loadAlbumCover(song, holder)
        }
    }

    private fun setColors(color: Int, holder: ViewHolder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer?.setBackgroundColor(color)
        }
    }

    protected open fun loadAlbumCover(song: CommonData, holder: ViewHolder) {
        if (holder.image == null) {
            return
        }
        SongGlideRequest.Builder.from(Glide.with(activity), song)
                .checkIgnoreMediaStore(activity)
                .generatePalette(activity).build()
                .into(object : RetroMusicColoredTarget(holder.image!!) {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        super.onLoadCleared(placeholder)
                        setColors(defaultFooterColor, holder)
                    }

                    override fun onColorReady(color: Int) {
                        setColors(color, holder)
                    }
                })
    }

    private fun getSongTitle(song: CommonData): String? {
        return if (localSong(song)) getLocalSong(song).title else getCloudSong(song).title
    }

    private fun getSongText(song: CommonData): String? {
        var text = if (localSong(song)) getLocalSong(song).artistName else getCloudSong(song).singerName
        val url = if (localSong(song)) "" else getCloudSong(song).cache

        val inDownloadingQueue = Constants.DOWNLOAD_INPROGRESS_QUEUE.contains(song.getSongId().toString())
        val inDownloadList = FileUtil.isMusicInDownloadList(activity, song.getSongId())
        if (inDownloadList) {
            val downloadProgress = FileUtil.getMusicDownloadProgress(activity, song.getSongId(), url)
            text = if (downloadProgress > DOWNLOAD_ASSUMED_COMPLETION_PERCENTAGE) {
                // 已下载成功
                text//"( ${activity.getString(R.string.cached)} ) $text"
            } else {
                if (inDownloadingQueue) {
                    // 已部分下载（可继续下载）
                    if (downloadProgress > 0) {
                        "( ${activity.getString(R.string.downloading)} - $downloadProgress% ) $text"
                    } else {
                        "( ${activity.getString(R.string.waiting_to_download)} ) $text"
                    }
                } else {
                    // 已下载失败（可重新下载）
                    "( ${activity.getString(R.string.save_failed)} ) $text"
                }
            }
        } else {
            val cached = FileUtil.isMusicCached(url)
            if (cached) {
                // 已缓存（可下载）
                text = "( ${activity.getString(R.string.cached)} ) $text"
            }
        }

        return text
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): CommonData? {
        return dataSet[position]
    }

    override fun getName(song: CommonData): String {
        return if (localSong(song)) getLocalSong(song).title else getCloudSong(song).songName
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: ArrayList<CommonData>) {
        SongsMenuHelper.handleMenuClick(activity, selection, menuItem.itemId)
    }

    private fun getAlbumName(data: CommonData): String {
        return if (localSong(data)) getLocalSong(data).albumName else activity.getString(R.string.online) // getCloudSong(data).channelTitle
    }

    override fun getPopupText(position: Int): String {
        val item = dataSet[position]
        if (item.dataType != CommonData.TYPE_NATIVE_ADS) {
            val sectionName: String? = when (PreferenceUtil.getInstance(activity).songSortOrder) {
                SortOrder.SongSortOrder.SONG_A_Z, SortOrder.SongSortOrder.SONG_Z_A -> getName(item)
                SortOrder.SongSortOrder.SONG_ALBUM -> getAlbumName(item)
                SortOrder.SongSortOrder.SONG_ARTIST -> getSongText(item)
//            SortOrder.SongSortOrder.SONG_YEAR -> return MusicUtil.getYearString(item.year)
//            SortOrder.SongSortOrder.COMPOSER -> item.composer
                else -> {
                    return ""
                }
            }

            return MusicUtil.getSectionName(sectionName)
        }
        return ""
    }

    open inner class ViewHolder(itemView: View, type: Int) : MediaEntryViewHolder(itemView) {
        protected open var songMenuRes = SongMenuHelper.getMenuRes(type)
        protected open val song: CommonData
            get() = dataSet[adapterPosition]

        init {
            setImageTransitionName(activity.getString(R.string.transition_album_art))
            menu?.setOnClickListener(object : SongMenuHelper.OnClickSongMenu(activity) {
                override val song: CommonData
                    get() = this@ViewHolder.song

                override val menuRes: Int
                    get() = songMenuRes

                override fun onMenuItemClick(item: MenuItem): Boolean {
                    return onSongMenuItemClick(item) || super.onMenuItemClick(item)
                }
            })
        }

        protected open fun onSongMenuItemClick(item: MenuItem): Boolean {
            if (image != null && image!!.visibility == View.VISIBLE) {
                when (item.itemId) {
                    R.id.action_go_to_album -> {
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
                }
            }
            return false
        }

        override fun onClick(v: View?) {
            if (adapterPosition >= 0 && dataSet.size > adapterPosition &&
                dataSet[adapterPosition].dataType != CommonData.TYPE_NATIVE_ADS) {
                if (isInQuickSelectMode) {
                    toggleChecked(adapterPosition)
                } else {
                    MusicPlayerRemote.openQueue(activity, dataSet, adapterPosition, true)
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            return if (adapterPosition >= 0 && dataSet.size > adapterPosition) {
                if (dataSet[adapterPosition].dataType == CommonData.TYPE_NATIVE_ADS) false
                else toggleChecked(adapterPosition)
            } else false
        }
    }

    companion object {

        val TAG: String = SongAdapter::class.java.simpleName
    }
}
