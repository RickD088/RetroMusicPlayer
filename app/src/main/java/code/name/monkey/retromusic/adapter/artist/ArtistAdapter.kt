package code.name.monkey.retromusic.adapter.artist

import android.app.ActivityOptions
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.adapter.base.AbsMultiSelectAdapter
import code.name.monkey.retromusic.adapter.base.MediaEntryViewHolder
import code.name.monkey.retromusic.extensions.hide
import code.name.monkey.retromusic.glide.ArtistGlideRequest
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget
import code.name.monkey.retromusic.helper.menu.SongsMenuHelper
import code.name.monkey.retromusic.interfaces.CabHolder
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.rest.music.model.PlaylistBean
import code.name.monkey.retromusic.util.MusicUtil
import code.name.monkey.retromusic.util.NavigationUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.util.*

class ArtistAdapter(
        val activity: AppCompatActivity,
        var dataSet: List<CommonData>,
        var itemLayoutRes: Int,
        cabHolder: CabHolder?
) : AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, CommonData>(
        activity, cabHolder, R.menu.menu_media_selection
), PopupTextProvider {

    init {
        this.setHasStableIds(true)
    }

    fun swapDataSet(dataSet: List<CommonData>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return if (localData(position)) {
            getArtist(position).id.toLong()
        } else {
            getPlaylistBean(position).id.toLong()
        }
    }

    private fun localData(position: Int): Boolean {
        return localData(dataSet[position])
    }

    private fun localData(data: CommonData): Boolean {
        return data.dataType == CommonData.TYPE_LOCAL_ARTIST
    }

    private fun getArtist(position: Int): Artist {
        return getArtist(dataSet[position])
    }

    private fun getArtist(data: CommonData): Artist {
        return data.localData as Artist
    }

    private fun getPlaylistBean(position: Int): PlaylistBean {
        return getPlaylistBean(dataSet[position])
    }

    private fun getPlaylistBean(data: CommonData): PlaylistBean {
        return data.cloudData as PlaylistBean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
        return createViewHolder(view)
    }

    private fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = dataSet[position]
        val isChecked = isChecked(artist)
        holder.itemView.isActivated = isChecked
        holder.title?.text =
                if (localData(position)) getArtist(position).name else getPlaylistBean(position).title
        holder.text?.hide()
        loadArtistImage(artist, holder, position)
    }

    fun setColors(color: Int, holder: ViewHolder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer?.setBackgroundColor(color)
            holder.title?.setTextColor(
                    MaterialValueHelper.getPrimaryTextColor(
                            activity, ColorUtil.isColorLight(
                            color
                    )
                    )
            )
        }
        holder.imageContainerCard?.setCardBackgroundColor(color)
        holder.mask?.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun loadArtistImage(
            artist: CommonData,
            holder: ViewHolder,
            position: Int
    ) {
        if (holder.image == null) {
            return
        }
        if (localData(position)) {
            ArtistGlideRequest.Builder.from(Glide.with(activity), getArtist(position))
                    .generatePalette(activity)
                    .build()
                    .into(object : RetroMusicColoredTarget(holder.image!!) {
                        override fun onLoadCleared(placeholder: Drawable?) {
                            super.onLoadCleared(placeholder)
                            setColors(defaultFooterColor, holder)
                        }

                        override fun onColorReady(color: Int) {
                            setColors(color, holder)
                        }
                    })
        } else {
            Glide.with(activity).load(getPlaylistBean(position).img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false).into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): CommonData? {
        return dataSet[position]
    }

    override fun getName(artist: CommonData): String {
        return if (localData(artist)) getArtist(artist).name else getPlaylistBean(artist).title
    }

    override fun onMultipleItemAction(
            menuItem: MenuItem, selection: ArrayList<CommonData>
    ) {
        //todo 菜单操作
//        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.itemId)
    }

    private fun getSongList(artists: List<Artist>): ArrayList<Song> {
        val songs = ArrayList<Song>()
        for (artist in artists) {
            songs.addAll(artist.songs) // maybe async in future?
        }
        return songs
    }

    override fun getPopupText(position: Int): String {
        return getSectionName(position)
    }

    private fun getSectionName(position: Int): String {
        return MusicUtil.getSectionName(getName(dataSet[position]))
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {

        init {
            setImageTransitionName(activity.getString(R.string.transition_artist_image))
            menu?.visibility = View.GONE
        }

        override fun onClick(v: View?) {
            super.onClick(v)
            if (isInQuickSelectMode) {
                toggleChecked(adapterPosition)
            } else {
                val activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                        activity,
                        imageContainerCard ?: image,
                        "${activity.getString(R.string.transition_artist_image)}_${getItemId(
                                adapterPosition
                        )}"
                )
                NavigationUtil.goToArtistOptions(
                        activity, getItemId(adapterPosition).toInt(), localData(adapterPosition), dataSet[adapterPosition], activityOptions
                )
            }
        }

        override fun onLongClick(v: View?): Boolean {
            toggleChecked(adapterPosition)
            return super.onLongClick(v)
        }
    }
}
