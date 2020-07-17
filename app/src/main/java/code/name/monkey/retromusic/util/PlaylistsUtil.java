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

package code.name.monkey.retromusic.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.M3UWriter;
import code.name.monkey.retromusic.model.CommonData;
import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.model.PlaylistSong;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.providers.PlaylistStore;

import static android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
import static code.name.monkey.retromusic.service.MusicService.MEDIA_STORE_CHANGED;

public class PlaylistsUtil {

    public static void addToPlaylist(@NonNull Context context,
                                     @NonNull List<CommonData> songs,
                                     Long playlistId,
                                     boolean showToastOnFinish) {
        PlaylistStore.getInstance(context).addSongs(context, playlistId, songs);
        if (showToastOnFinish) {
            Toast.makeText(context, context.getResources().getString(
                    R.string.inserted_x_songs_into_playlist_x, songs.size(), getNameForPlaylist(context, playlistId)),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static Long createPlaylist(Context context, String name) {
        if (!TextUtils.isEmpty(name)) {
            Cursor cursor = PlaylistStore.getInstance(context).queryPlaylistByName(name);
            Long id;
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.ID));
            } else {
                id = PlaylistStore.getInstance(context).addPlaylist(name);
            }
            return id;
        }
        Toast.makeText(context, context.getResources().getString(R.string.could_not_create_playlist), Toast.LENGTH_SHORT).show();
        return -1L;
    }


    public static void deletePlaylists(@NonNull final Context context, @NonNull final ArrayList<Playlist> playlists) {
        final StringBuilder selection = new StringBuilder();
        selection.append("(");
        for (int i = 0; i < playlists.size(); i++) {
            selection.append(playlists.get(i).id);
            if (i < playlists.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        PlaylistStore.getInstance(context).deletePlaylist(selection.toString());
        context.sendBroadcast(new Intent(MEDIA_STORE_CHANGED));
    }

    static boolean doPlaylistContains(@NonNull final Context context, final long playlistId,
                                      final int songId) {
        if (playlistId != -1) {
            try {
                Cursor c = context.getContentResolver().query(
                        MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                        new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID},
                        MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", new String[]{String.valueOf(songId)},
                        null);
                int count = 0;
                if (c != null) {
                    count = c.getCount();
                    c.close();
                }
                return count > 0;
            } catch (SecurityException ignored) {
            }
        }
        return false;
    }

    public static boolean doesPlaylistExist(@NonNull final Context context, final long playlistId) {
        return playlistId != -1 && doesPlaylistExist(context, String.valueOf(playlistId));
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        try {
            Cursor cursor = PlaylistStore.getInstance(context).queryPlaylistById(String.valueOf(id));
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(cursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.PLAYLIST_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (SecurityException ignored) {
        }
        return "";
    }

    @NonNull
    public static ContentValues[] makeInsertItems(@NonNull final List<Song> songs, final int offset, int len,
                                                  final int base) {
        if (offset + len > songs.size()) {
            len = songs.size() - offset;
        }

        ContentValues[] contentValues = new ContentValues[len];

        for (int i = 0; i < len; i++) {
            contentValues[i] = new ContentValues();
            contentValues[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            contentValues[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songs.get(offset + i).getId());
        }
        return contentValues;
    }

    public static boolean moveItem(@NonNull final Context context, Long playlistId, int from, int to) {
        Cursor playlistCursor = PlaylistStore.getInstance(context).queryPlaylistById(String.valueOf(playlistId));
        if (playlistCursor != null && playlistCursor.moveToFirst()) {
            Long id = playlistCursor.getLong(playlistCursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.ID));
            String name = playlistCursor.getString(playlistCursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.PLAYLIST_NAME));
            String songIds = playlistCursor.getString(playlistCursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.SONG_IDS));
            String[] ids = songIds.split(",");
            List<String> idList = new ArrayList<>(Arrays.asList(ids));
            String moveId = idList.remove(from);
            idList.add(to, moveId);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < idList.size(); i++) {
                if (i == 0) {
                    builder.append(idList.get(i));
                } else {
                    builder.append(",").append(idList.get(i));
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaylistStore.PlaylistStoreColumns.SONG_IDS, builder.toString());
            PlaylistStore.getInstance(context).updatePlaylist(contentValues, String.valueOf(playlistId));
            playlistCursor.close();
        }
        return true;
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final CommonData data, int playlistId) {
        if (data.localSong()) {
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                    "external", playlistId);
            String selection = MediaStore.Audio.Playlists.Members.AUDIO_ID + " =?";
            String[] selectionArgs = new String[]{String.valueOf(data.getSongId())};
            try {
                context.getContentResolver().delete(uri, selection, selectionArgs);
            } catch (SecurityException ignored) {
            }
        }
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final List<PlaylistSong> songs) {
        final int playlistId = songs.get(0).getPlaylistId();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        String selectionArgs[] = new String[songs.size()];
        for (int i = 0; i < selectionArgs.length; i++) {
            selectionArgs[i] = String.valueOf(songs.get(i).getIdInPlayList());
        }
        String selection = MediaStore.Audio.Playlists.Members._ID + " in (";
        //noinspection unused
        for (String selectionArg : selectionArgs) {
            selection += "?, ";
        }
        selection = selection.substring(0, selection.length() - 2) + ")";

        try {
            context.getContentResolver().delete(uri, selection, selectionArgs);
        } catch (SecurityException ignored) {
        }
    }

    public static void removeSongFromPlaylist(Context context, long playlistId, List<CommonData> songs) {
        Cursor playlistCursor = PlaylistStore.getInstance(context).queryPlaylistById(String.valueOf(playlistId));
        if (playlistCursor != null && playlistCursor.moveToFirst()) {
            Long id = playlistCursor.getLong(playlistCursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.ID));
            String name = playlistCursor.getString(playlistCursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.PLAYLIST_NAME));
            String songIds = playlistCursor.getString(playlistCursor.getColumnIndex(PlaylistStore.PlaylistStoreColumns.SONG_IDS));
            String[] ids = songIds.split(",");
            List<String> idList = new ArrayList<>(Arrays.asList(ids));
            List<String> removeIds = new ArrayList<>();
            for (String s : idList) {
                for (CommonData data : songs) {
                    if (s.equals(String.valueOf(data.getSongId()))) {
                        removeIds.add(s);
                    }
                }
            }
            idList.removeAll(removeIds);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < idList.size(); i++) {
                if (i == 0) {
                    builder.append(idList.get(i));
                } else {
                    builder.append(",").append(idList.get(i));
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaylistStore.PlaylistStoreColumns.SONG_IDS, builder.toString());
            PlaylistStore.getInstance(context).updatePlaylist(contentValues, String.valueOf(playlistId));
            playlistCursor.close();
        }
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlaylistStore.PlaylistStoreColumns.PLAYLIST_NAME, newName);
        PlaylistStore.getInstance(context).updatePlaylist(contentValues, String.valueOf(id));
        context.sendBroadcast(new Intent(MEDIA_STORE_CHANGED));
    }

    @Nullable
    public static File savePlaylist(@NonNull Context context,
                                    @NonNull Playlist playlist) throws IOException {
        return M3UWriter.write(context, new File(Environment.getExternalStorageDirectory(), "Playlists"), playlist);
    }

    static void addToPlaylist(@NonNull Context context,
                              @NonNull CommonData song,
                              Long playlistId,
                              boolean showToastOnFinish) {
        List<CommonData> helperList = new ArrayList<>();
        helperList.add(song);
        addToPlaylist(context, helperList, playlistId, showToastOnFinish);
    }

    private static boolean doesPlaylistExist(@NonNull Context context, @NonNull final String playlistId) {
        Cursor cursor = PlaylistStore.getInstance(context).queryPlaylistById(playlistId);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() != 0;
            cursor.close();
        }
        return exists;
    }
}