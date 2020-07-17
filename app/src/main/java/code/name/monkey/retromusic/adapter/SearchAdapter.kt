package code.name.monkey.retromusic.adapter

import android.app.ActivityOptions
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.abram.AbramAds
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.adapter.base.MediaEntryViewHolder
import code.name.monkey.retromusic.extensions.toCommonData
import code.name.monkey.retromusic.glide.AlbumGlideRequest
import code.name.monkey.retromusic.glide.ArtistGlideRequest
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.helper.menu.SongMenuHelper
import code.name.monkey.retromusic.loaders.PlaylistSongsLoader
import code.name.monkey.retromusic.model.*
import code.name.monkey.retromusic.model.smartplaylist.AbsSmartPlaylist
import code.name.monkey.retromusic.rest.music.model.SongBean
import code.name.monkey.retromusic.util.MusicUtil
import code.name.monkey.retromusic.util.NavigationUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import android.util.Pair as UtilPair


class SearchAdapter(
        private val activity: AppCompatActivity,
        private val dataSet: MutableList<Any>,
        private val loadMoreCallback: ((MoreData) -> Unit)?
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    fun swapDataSet(dataSet: MutableList<Any>) {
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        notifyDataSetChanged()
    }

    fun getDataSet(): List<Any>? {
        return dataSet
    }

    override fun getItemViewType(position: Int): Int {
        if (dataSet[position] is Album) return ALBUM
        if (dataSet[position] is Artist) return ARTIST
        if (dataSet[position] is Genre) return GENRE
        if (dataSet[position] is Playlist) return PLAYLIST
        if (dataSet[position] is SongBean) return CLOUD_SONG
        if (dataSet[position] is MoreData) return LOAD_MORE
        return if (dataSet[position] is Song) SONG else HEADER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            HEADER -> {
                ViewHolder(LayoutInflater.from(activity).inflate(R.layout.sub_header, parent, false), viewType)
            }
            LOAD_MORE -> {
                ViewHolder(LayoutInflater.from(activity).inflate(R.layout.layout_load_more, parent, false), viewType)
            }
            else -> {
                ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false), viewType)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ALBUM -> {
                val album = dataSet?.get(position) as Album
                holder.title?.text = album.title
                holder.text?.text = album.artistName
                AlbumGlideRequest.Builder.from(Glide.with(activity), album.safeGetFirstSong())
                        .checkIgnoreMediaStore(activity).build().into(holder.image)
            }
            ARTIST -> {
                val artist = dataSet?.get(position) as Artist
                holder.title?.text = artist.name
                holder.text?.text = MusicUtil.getArtistInfoString(activity, artist)
                ArtistGlideRequest.Builder.from(Glide.with(activity), artist).build()
                        .into(holder.image)
            }
            SONG -> {
                val song = dataSet?.get(position) as Song
                holder.title?.text = song.title
                holder.text?.text = song.albumName
            }
            CLOUD_SONG -> {
                val song = dataSet?.get(position) as SongBean
                holder.title?.text = song.title
                holder.text?.text = activity.getString(R.string.online) // song.channelTitle
                Glide.with(activity).load(song.artworkUrl).asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false).into(holder.image)
            }
            GENRE -> {
                val genre = dataSet?.get(position) as Genre
                holder.title?.text = genre.name
            }
            PLAYLIST -> {
                val playlist = dataSet?.get(position) as Playlist
                holder.title?.text = playlist.name
                holder.text?.text = MusicUtil.getPlaylistInfoString(activity, getSongs(playlist))
            }
            NATIVE_ADS -> {
            }
            LOAD_MORE -> {
                val data = dataSet?.get(position) as MoreData
                holder.text?.text = if (data.loading) activity.getString(R.string.loading) else activity.getString(R.string.load_more)
                holder.text?.postDelayed({
                    if (!data.loading) {
                        loadMoreCallback?.invoke(data)
                        holder.text?.text = activity.getString(R.string.loading)
                    }
                }, Constants.ONE_FIFTH_SECOND_MS)
            }
            else -> {
                holder.title?.text = dataSet?.get(position).toString()
                holder.title?.setTextColor(ThemeStore.accentColor(activity))
            }
        }
    }

    fun getOnlineSongsSize(): Int {
        return dataSet.filterIsInstance<SongBean>()?.size ?: 0
    }

    private fun getSongs(playlist: Playlist): java.util.ArrayList<CommonData> {
        val songs = java.util.ArrayList<CommonData>()
        if (playlist is AbsSmartPlaylist) {
            songs.addAll(playlist.getSongs(activity))
        } else {
            songs.addAll(PlaylistSongsLoader.getPlaylistSongsFromDb(activity, playlist.id))
        }
        return songs
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    inner class ViewHolder(itemView: View, itemViewType: Int) : MediaEntryViewHolder(itemView) {

        init {
            itemView.setOnLongClickListener(null)

            if (itemViewType == SONG || itemViewType == CLOUD_SONG) {
                menu?.visibility = View.VISIBLE
                menu?.setOnClickListener(object : SongMenuHelper.OnClickSongMenu(activity) {
                    override val song: CommonData
                        get() {
                            return if (adapterPosition >= 0 && dataSet.size > adapterPosition) {
                                if (dataSet[adapterPosition] is Song) {
                                    (dataSet[adapterPosition] as Song).convertToCommonData()
                                } else {
                                    (dataSet[adapterPosition] as SongBean).convertToCommonData()
                                }
                            } else CommonData(CommonData.TYPE_EMPTY)
                        }
                    override val menuRes: Int
                        get() = SongMenuHelper.getMenuRes(
                                if (itemViewType == CLOUD_SONG) CommonData.TYPE_CLOUD_SONG
                                else CommonData.TYPE_LOCAL_SONG
                        )
                })
            } else {
                menu?.visibility = View.GONE
            }

            when (itemViewType) {
                ALBUM -> setImageTransitionName(activity.getString(R.string.transition_album_art))
                ARTIST -> setImageTransitionName(activity.getString(R.string.transition_artist_image))
                CLOUD_SONG -> setImageTransitionName(activity.getString(R.string.transition_artist_image))
                else -> {
                    val container = itemView.findViewById<View>(R.id.imageContainer)
                    container?.visibility = View.GONE
                }
            }
        }

        override fun onClick(v: View?) {
            if (adapterPosition >= 0 && dataSet.size > adapterPosition) {
                val item = dataSet[adapterPosition]
                when (itemViewType) {
                    ALBUM -> {
                        val options = ActivityOptions.makeSceneTransitionAnimation(
                            activity,
                            UtilPair.create(
                                image,
                                activity.getString(R.string.transition_album_art)
                            )
                        )
                        NavigationUtil.goToAlbumOptions(
                            activity,
                            (item as Album).id,
                            (item as Album).convertToCommonData(),
                            options
                        )
                    }
                    ARTIST -> {
                        val options = ActivityOptions.makeSceneTransitionAnimation(
                            activity,
                            UtilPair.create(
                                image,
                                activity.getString(R.string.transition_artist_image)
                            )
                        )
                        NavigationUtil.goToArtistOptions(
                            activity,
                            (item as Artist).id,
                            (item as Artist).convertToCommonData(),
                            options
                        )
                    }
                    GENRE -> {
                        NavigationUtil.goToGenre(activity, item as Genre)
                    }
                    PLAYLIST -> {
                        NavigationUtil.goToPlaylistNew(activity, item as Playlist)
                    }
                    SONG -> {
                        val playList = ArrayList<Song>()
                        dataSet.forEach {
                            if (it is Song) {
                                playList.add(it)
                            }
                        }
                        val index = playList.indexOf(item)
                        MusicPlayerRemote.openQueue(activity, playList.toCommonData(), index, true)
                    }
                    LOAD_MORE -> {
                        if (!(item as MoreData).loading) {
                            loadMoreCallback?.invoke(item)
                        }
                    }
                    CLOUD_SONG -> {
                        val playList = ArrayList<SongBean>()
                        dataSet.forEach {
                            if (it is SongBean) {
                                playList.add(it)
                            }
                        }
                        val index = playList.indexOf(item)
                        MusicPlayerRemote.openQueue(activity, playList.toCommonData(), index, true)
                    }
                }
            }
        }
    }

    companion object {
        private const val HEADER = 0
        private const val ALBUM = 1
        private const val ARTIST = 2
        private const val SONG = 3
        private const val GENRE = 4
        private const val PLAYLIST = 5
        private const val CLOUD_SONG = 6
        private const val NATIVE_ADS = 7
        private const val LOAD_MORE = 8
    }
}
