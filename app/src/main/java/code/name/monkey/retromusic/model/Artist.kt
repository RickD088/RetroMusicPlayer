/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package code.name.monkey.retromusic.model

import android.os.Parcelable
import code.name.monkey.retromusic.interfaces.CommonDataConverter
import code.name.monkey.retromusic.util.MusicUtil
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class Artist constructor(val albums: ArrayList<Album> = arrayListOf()) : CommonDataConverter, Parcelable {
//    var albums: ArrayList<Album>? = null

    val id: Int
        get() = safeGetFirstAlbum().artistId

    val name: String
        get() {
            val name = safeGetFirstAlbum().artistName
            return if (MusicUtil.isArtistNameUnknown(name)) {
                UNKNOWN_ARTIST_DISPLAY_NAME
            } else name!!
        }

    val songCount: Int
        get() {
            var songCount = 0
            for (album in albums!!) {
                songCount += album.songCount
            }
            return songCount
        }

    val albumCount: Int
        get() = albums!!.size

    val songs: ArrayList<Song>
        get() {
            val songs = ArrayList<Song>()
            for (album in albums!!) {
                songs.addAll(album.songs!!)
            }
            return songs
        }

    /*constructor(albums: ArrayList<Album>) : this() {
        this.albums = albums
    }*/

    fun safeGetFirstAlbum(): Album {
        return if (albums!!.isEmpty()) Album() else albums[0]
    }

    companion object {
        const val UNKNOWN_ARTIST_DISPLAY_NAME = "Unknown Artist"
    }

    override fun convertToCommonData(): CommonData {
        return CommonData(CommonData.TYPE_LOCAL_ARTIST, this)
    }


    override fun describeContents(): Int = 0
}
