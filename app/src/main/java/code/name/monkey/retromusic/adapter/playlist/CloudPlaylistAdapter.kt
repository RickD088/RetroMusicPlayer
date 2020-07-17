package code.name.monkey.retromusic.adapter.playlist

import android.app.ActivityOptions
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.adapter.base.AbsMultiSelectAdapter
import code.name.monkey.retromusic.adapter.base.MediaEntryViewHolder
import code.name.monkey.retromusic.extensions.hide
import code.name.monkey.retromusic.extensions.show
import code.name.monkey.retromusic.helper.menu.SongMenuHelper
import code.name.monkey.retromusic.interfaces.CabHolder
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.rest.music.model.PlaylistBean
import code.name.monkey.retromusic.util.NavigationUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.ArrayList

class CloudPlaylistAdapter(
        val activity: AppCompatActivity,
        var dataSet: List<CommonData>,
        var itemLayoutRes: Int,
        cabHolder: CabHolder?
) : AbsMultiSelectAdapter<CloudPlaylistAdapter.ViewHolder, CommonData>(
        activity,
        cabHolder,
        R.menu.menu_media_selection
) {

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        protected var songMenuRes = SongMenuHelper.MENU_RES
        protected val song: CommonData
            get() = dataSet[adapterPosition]

        init {
            setImageTransitionName(activity.getString(R.string.transition_album_art))
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
                        "${activity.getString(R.string.transition_album_art)}_${dataSet[adapterPosition].cloudPlayList()}"
                )
                NavigationUtil.goToAlbumOptions(
                        activity,
                        dataSet[adapterPosition].getCloudPlaylistBean().id,
                        false,
                        dataSet[adapterPosition].getCloudPlaylistBean().title,
                        dataSet[adapterPosition],
                        activityOptions
                )
            }
        }

        override fun onLongClick(v: View?): Boolean {
            toggleChecked(adapterPosition)
            return super.onLongClick(v)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
        return createViewHolder(view)
    }

    private fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): CommonData? {
        return dataSet[position]
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = dataSet[position].getCloudPlaylistBean()
        val isChecked = isChecked(dataSet[position])
        holder.itemView.isActivated = isChecked
        if (isChecked) {
            holder.menu?.hide()
        } else {
            holder.menu?.show()
        }
        holder.title?.text = playlist.title
        holder.text?.text = playlist.description
        loadAlbumCover(playlist, holder)
    }

    private fun loadAlbumCover(playlist: PlaylistBean, holder: ViewHolder) {
        if (holder.image == null) {
            return
        }
        Glide.with(activity).load(playlist.img)
                .diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false).into(holder.image)
        /*SongGlideRequest.Builder.from(Glide.with(activity), song)
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
            })*/
    }

    override fun onMultipleItemAction(menuItem: MenuItem?, selection: ArrayList<CommonData>?) {
    }
}