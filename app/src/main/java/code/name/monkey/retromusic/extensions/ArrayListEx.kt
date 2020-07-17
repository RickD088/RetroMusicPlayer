package code.name.monkey.retromusic.extensions

import code.name.monkey.retromusic.interfaces.CabHolder
import code.name.monkey.retromusic.interfaces.CommonDataConverter
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.model.Playlist
import code.name.monkey.retromusic.rest.music.model.PlaylistBean
import code.name.monkey.retromusic.rest.music.model.SongBean

fun <E> ArrayList<E>.toAlbums(): ArrayList<Album> {
    val arrayList = ArrayList<Album>()
    for (x in this) {
        arrayList.add(x as Album)
    }
    return arrayList
}


fun <E> ArrayList<E>.toArtists(): ArrayList<Artist> {
    val arrayList = ArrayList<Artist>()
    for (x in this) {
        arrayList.add(x as Artist)
    }
    return arrayList
}

fun <E> ArrayList<E>.toPlaylist(): ArrayList<Playlist> {
    val arrayList = ArrayList<Playlist>()
    for (x in this) {
        arrayList.add(x as Playlist)
    }
    return arrayList
}

fun <E> ArrayList<E>.toPlaylistBean(): ArrayList<PlaylistBean> {
    val arrayList = ArrayList<PlaylistBean>()
    for (x in this) {
        arrayList.add(x as PlaylistBean)
    }
    return arrayList
}

fun <E> ArrayList<E>.toSongBean(): ArrayList<SongBean> {
    val arrayList = ArrayList<SongBean>()
    for (x in this) {
        arrayList.add(x as SongBean)
    }
    return arrayList
}

fun <E> ArrayList<E>.asCommonDataList(): ArrayList<CommonData> {
    val arrayList = ArrayList<CommonData>()
    for (x in this) {
        arrayList.add(x as CommonData)
    }
    return arrayList
}

fun <E : CommonDataConverter> ArrayList<E>.toCommonData(): ArrayList<CommonData> {
    val arrayList = ArrayList<CommonData>()
    for (x in this) {
        arrayList.add(x.convertToCommonData())
    }
    return arrayList
}