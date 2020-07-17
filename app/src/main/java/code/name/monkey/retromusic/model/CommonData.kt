package code.name.monkey.retromusic.model

import android.os.Parcelable
import code.name.monkey.retromusic.interfaces.CommonDataConverter
import code.name.monkey.retromusic.rest.music.model.MusicArtist
import code.name.monkey.retromusic.rest.music.model.MusicSongs
import code.name.monkey.retromusic.rest.music.model.PlaylistBean
import code.name.monkey.retromusic.rest.music.model.SongBean
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommonData(
        val dataType: Int,
        val localData: CommonDataConverter? = null,
        val cloudData: CommonDataConverter? = null
) : Parcelable {
    companion object {
        const val TYPE_NATIVE_ADS = -2
        const val TYPE_EMPTY = -1
        const val TYPE_LOCAL_ARTIST = 0
        const val TYPE_CLOUD_ARTIST = 1
        const val TYPE_CLOUD_ARTIST_PLAYLIST = 7
        const val TYPE_LOCAL_PLAYLIST = 2
        const val TYPE_CLOUD_PLAYLIST = 3
        const val TYPE_CLOUD_PLAYLIST_SONG = 8
        const val TYPE_LOCAL_SONG = 4
        const val TYPE_CLOUD_SONG = 5
        const val TYPE_LOCAL_ALBUM = 6
    }

    fun localArtist(): Boolean {
        return this.dataType == TYPE_LOCAL_ARTIST
    }

    fun cloudArtist(): Boolean {
        return this.dataType == TYPE_CLOUD_ARTIST
    }

    fun localPlayList(): Boolean {
        return this.dataType == TYPE_LOCAL_PLAYLIST
    }

    fun cloudPlayList(): Boolean {
        return this.dataType == TYPE_CLOUD_PLAYLIST
    }

    fun cloudPlayListSong(): Boolean {
        return this.dataType == TYPE_CLOUD_PLAYLIST_SONG
    }

    fun cloudArtistPlayList(): Boolean {
        return this.dataType == TYPE_CLOUD_ARTIST_PLAYLIST
    }

    fun localSong(): Boolean {
        return this.dataType == TYPE_LOCAL_SONG
    }

    fun cloudSong(): Boolean {
        return this.dataType == TYPE_CLOUD_SONG
    }

    fun localAlbum(): Boolean {
        return this.dataType == TYPE_LOCAL_ALBUM
    }

    /*
    * 需要先判断类型再获取相对应的数据类型
    * */
    fun getLocalArtist(): Artist = this.localData as Artist

    fun getCloudArtist(): MusicArtist = this.cloudData as MusicArtist

    fun getLocalPlaylist(): Playlist = this.localData as Playlist

    //dataType: TYPE_CLOUD_PLAYLIST
    fun getCloudPlaylistSongs(): MusicSongs = this.cloudData as MusicSongs

    fun getCloudPlaylistBean(): PlaylistBean = this.cloudData as PlaylistBean

    fun getCloudArtistPlaylist(): PlaylistBean = this.cloudData as PlaylistBean

    fun getLocalSong(): Song = this.localData as Song

    //dataType: TYPE_CLOUD_SONG
    fun getCloudSong(): SongBean = this.cloudData as SongBean

    fun getLocalAlbum(): Album = this.localData as Album

    fun getSongTitle(): String {
        return when {
            localSong() -> {
                getLocalSong().title
            }
            cloudSong() -> {
                getCloudSong().songName
            }
            else -> {
                ""
            }
        }
    }

    fun getSongSinger(): String {
        return when {
            localSong() -> {
                getLocalSong().artistName
            }
            cloudSong() -> {
                getCloudSong().singerName
            }
            else -> {
                ""
            }
        }
    }

    fun getSongId(): Int {
        return when {
            localSong() -> {
                getLocalSong().id
            }
            cloudSong() -> {
                getCloudSong().id
            }
            else -> {
                -1
            }
        }
    }

    fun getSongAlbum(): String {
        return when {
            localSong() -> {
                getLocalSong().albumName
            }
            cloudSong() -> {
                "online" // getCloudSong().channelTitle
            }
            else -> {
                ""
            }
        }
    }

}