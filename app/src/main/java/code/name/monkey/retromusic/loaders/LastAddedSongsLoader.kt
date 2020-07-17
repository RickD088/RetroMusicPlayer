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

package code.name.monkey.retromusic.loaders

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.Artist
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.providers.HistoryStore
import code.name.monkey.retromusic.util.PreferenceUtil
import java.lang.StringBuilder

/**
 * Created by hemanths on 16/08/17.
 */

object LastAddedSongsLoader {

    fun getLastAddedSongs(context: Context): ArrayList<Song> {
        return SongLoader.getSongs(makeLastAddedCursor(context))
    }

    fun getDownloadSongs(context: Context): ArrayList<CommonData> {
        // 用于保存原始顺序
        val order = arrayListOf<String>()
        val rawList = SongLoader.getSongsFromDataBase(makeDownloadSongCursor(context, order))
        val orderedList = arrayListOf<CommonData>()
        // 恢复原始顺序
        order.mapIndexed { index, songId ->
            val found = rawList.find { it.getSongId().toString() == songId }
            found?.let {
                orderedList.add(it)
            }
        }
        return orderedList
    }

    private fun makeDownloadSongCursor(context: Context, order: ArrayList<String>): Cursor? {
        val downloadCursor = HistoryStore.getInstance(context).queryAllDownload()
        val builder = StringBuilder()
        if (downloadCursor.moveToFirst()) {
            val id =
                downloadCursor.getInt(downloadCursor.getColumnIndex(HistoryStore.DownloadStoreColumns.SONG_ID))
            builder.append("(").append(id)
            order.add(id.toString())
            while (downloadCursor.moveToNext()) {
                val songId =
                    downloadCursor.getInt(downloadCursor.getColumnIndex(HistoryStore.DownloadStoreColumns.SONG_ID))
                builder.append(",$songId")
                order.add(songId.toString())
            }
            builder.append(")")
            downloadCursor.close()
            return HistoryStore.getInstance(context).queryRecentSongByIds(builder.toString())
        }
        return null
    }

    private fun makeLastAddedCursor(context: Context): Cursor? {
        val cutoff = PreferenceUtil.getInstance(context).lastAddedCutoff

        return SongLoader.makeSongCursor(
            context,
            MediaStore.Audio.Media.DATE_ADDED + ">?",
            arrayOf(cutoff.toString()),
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
    }

    fun getLastAddedAlbums(context: Context): ArrayList<Album> {
        return AlbumLoader.splitIntoAlbums(getLastAddedSongs(context))
    }

    fun getLastAddedArtists(context: Context): ArrayList<Artist> {
        return ArtistLoader.splitIntoArtists(getLastAddedAlbums(context))
    }
}
