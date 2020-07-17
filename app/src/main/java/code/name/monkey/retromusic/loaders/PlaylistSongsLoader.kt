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
import android.provider.MediaStore.Audio.AudioColumns
import code.name.monkey.retromusic.Constants.BASE_SELECTION
import code.name.monkey.retromusic.extensions.toCommonData
import code.name.monkey.retromusic.model.*
import code.name.monkey.retromusic.providers.HistoryStore
import code.name.monkey.retromusic.providers.PlaylistStore
import kotlin.collections.ArrayList

/**
 * Created by hemanths on 16/08/17.
 */

object PlaylistSongsLoader {

    fun getPlaylistSongList(
        context: Context,
        playlist: Playlist
    ): ArrayList<CommonData> {
        return (playlist as? AbsCustomPlaylist)?.getSongs(context)
            ?: getPlaylistSongsFromDb(context, playlist.id)
    }

    @JvmStatic
    fun getPlaylistSongList(context: Context, playlistId: Int): ArrayList<Song> {
        val songs = arrayListOf<Song>()
        val cursor = makePlaylistSongCursor(context, playlistId)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getPlaylistSongFromCursorImpl(cursor, playlistId))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    fun getPlaylistSongsFromDb(context: Context, playlistId: Long): ArrayList<CommonData> {
        return SongLoader.getSongsFromDataBase(
            makeSongCursorAndClearUpDatabase(
                context,
                playlistId
            )
        )
    }

    private fun makeSongCursorAndClearUpDatabase(context: Context, playlistId: Long): Cursor? {
        val retCursor = makeSongCursorImpl(context, playlistId)
        // clean up the databases with any ids not found
        if (retCursor != null) {
            val missingIds = retCursor.missingIds
            if (missingIds != null && missingIds.size > 0) {
                for (id in missingIds) {
                    HistoryStore.getInstance(context).removeSongId(id)
                }
            }
        }
        return retCursor
    }

    private fun makeSongCursorImpl(context: Context, playlistId: Long): SortedLongCursor? {
        val playlistCursor =
            PlaylistStore.getInstance(context).queryPlaylistById(playlistId.toString())
        playlistCursor.use {
            return makeSortLongCursor(context, it)
        }
    }

    private fun makeSortLongCursor(context: Context, playlistCursor: Cursor): SortedLongCursor? {
        if (playlistCursor.moveToFirst()) {
            val songIds =
                playlistCursor.getString(playlistCursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.SONG_IDS))
            if (songIds.isNotEmpty()) {
                val ids = songIds.split(",")
                val order = LongArray(ids.size)
                for (index in ids.indices) {
                    order[index] = ids[index].toLong()
                }
                val songCursor =
                    HistoryStore.getInstance(context).queryRecentSongByIds("($songIds)")
                if (songCursor != null) {
                    return SortedLongCursor(songCursor, order, HistoryStore.SongStoreColumns.ID)
                }
            }
        }
        return null
    }

    private fun getPlaylistSongFromCursorImpl(cursor: Cursor, playlistId: Int): PlaylistSong {
        val id = cursor.getInt(0)
        val title = cursor.getString(1)
        val trackNumber = cursor.getInt(2)
        val year = cursor.getInt(3)
        val duration = cursor.getLong(4)
        val data = cursor.getString(5)
        val dateModified = cursor.getLong(6)
        val albumId = cursor.getInt(7)
        val albumName = cursor.getString(8)
        val artistId = cursor.getInt(9)
        val artistName = cursor.getString(10)
        val idInPlaylist = cursor.getInt(11)
        val composer = cursor.getString(12)

        return PlaylistSong(
            id,
            title,
            trackNumber,
            year,
            duration,
            data,
            dateModified,
            albumId,
            albumName,
            artistId,
            artistName,
            playlistId,
            idInPlaylist,
            composer
        )
    }

    private fun makePlaylistSongCursor(context: Context, playlistId: Int): Cursor? {
        try {
            return context.contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId.toLong()),
                arrayOf(
                    MediaStore.Audio.Playlists.Members.AUDIO_ID, // 0
                    AudioColumns.TITLE, // 1
                    AudioColumns.TRACK, // 2
                    AudioColumns.YEAR, // 3
                    AudioColumns.DURATION, // 4
                    AudioColumns.DATA, // 5
                    AudioColumns.DATE_MODIFIED, // 6
                    AudioColumns.ALBUM_ID, // 7
                    AudioColumns.ALBUM, // 8
                    AudioColumns.ARTIST_ID, // 9
                    AudioColumns.ARTIST, // 10
                    MediaStore.Audio.Playlists.Members._ID,//11
                    AudioColumns.COMPOSER
                )// 12
                , BASE_SELECTION, null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER
            )
        } catch (e: SecurityException) {
            return null
        }
    }
}
