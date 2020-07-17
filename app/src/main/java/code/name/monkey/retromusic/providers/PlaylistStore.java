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

package code.name.monkey.retromusic.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import code.name.monkey.retromusic.App;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.abram.Constants;
import code.name.monkey.retromusic.model.CommonData;

public class PlaylistStore extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "playlist.db";
    private static final int VERSION = 1;
    @Nullable
    private static PlaylistStore sInstance = null;

    public PlaylistStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @NonNull
    public static synchronized PlaylistStore getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new PlaylistStore(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PlaylistStoreColumns.TABLE_NAME + " ("
                + PlaylistStoreColumns.ID + " LONG NOT NULL," + PlaylistStoreColumns.PLAYLIST_NAME
                + " TEXT NOT NULL," + PlaylistStoreColumns.SONG_IDS + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaylistStoreColumns.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaylistStoreColumns.TABLE_NAME);
        onCreate(db);
    }

    public void removePlaylistId(final long playlistId) {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(PlaylistStoreColumns.TABLE_NAME, PlaylistStoreColumns.ID + " = ?", new String[]{
                String.valueOf(playlistId)
        });
    }

    public void clear() {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(PlaylistStoreColumns.TABLE_NAME, null, null);
    }

    public Long addPlaylist(String name) {

        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        Long id = name.equals(App.Companion.getContext().getString(R.string.favorites))
                ? Constants.UI_FAVORITE_PLAYLIST_ID : System.currentTimeMillis();
        try {
            // add the entry
            final ContentValues values = new ContentValues();
            values.put(PlaylistStoreColumns.ID, id);
            values.put(PlaylistStoreColumns.PLAYLIST_NAME, name);
            values.put(PlaylistStoreColumns.SONG_IDS, "");
            database.insert(PlaylistStoreColumns.TABLE_NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        return id;
    }

    public Cursor queryAllPlaylist(String selection, String[] selectionArgs) {
        SQLiteDatabase database = getReadableDatabase();
        return database.query(PlaylistStoreColumns.TABLE_NAME, new String[]{"*"}, selection, selectionArgs, null, null,
                PlaylistStoreColumns.ID + " DESC");
    }

    public Cursor queryPlaylistByName(String name) {
        SQLiteDatabase database = getReadableDatabase();
        return database.query(PlaylistStoreColumns.TABLE_NAME, new String[]{PlaylistStoreColumns.ID},
                PlaylistStoreColumns.PLAYLIST_NAME + " =?", new String[]{name}, null, null, null);
    }

    public Cursor queryPlaylistById(String id) {
        SQLiteDatabase database = getReadableDatabase();
        return database.query(PlaylistStoreColumns.TABLE_NAME, new String[]{"*"},
                PlaylistStoreColumns.ID + " =?", new String[]{id}, null, null, null);
    }

    public void deletePlaylist(String ids) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(PlaylistStoreColumns.TABLE_NAME, PlaylistStoreColumns.ID + " IN " + ids, null);
    }

    public void updatePlaylist(ContentValues contentValues, String playlistId) {
        SQLiteDatabase database = getWritableDatabase();
        database.update(PlaylistStoreColumns.TABLE_NAME, contentValues, PlaylistStoreColumns.ID + " =?", new String[]{playlistId});
    }

    public void addSongs(Context context, Long playlistId, List<CommonData> songs) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        Cursor playlistCursor = null;
        try {
            playlistCursor = database.query(PlaylistStoreColumns.TABLE_NAME, new String[]{"*"},
                    PlaylistStoreColumns.ID + " =?", new String[]{String.valueOf(playlistId)}, null, null, null);
            if (playlistCursor != null && playlistCursor.moveToFirst()) {
                String playlistName = playlistCursor.getString(playlistCursor.getColumnIndex(PlaylistStoreColumns.PLAYLIST_NAME));
                String songIds = playlistCursor.getString(playlistCursor.getColumnIndex(PlaylistStoreColumns.SONG_IDS));
                String updateSongIds = updateSongIds(songIds, songs);
                removePlaylistId(playlistId);
                final ContentValues values = new ContentValues();
                values.put(PlaylistStoreColumns.ID, playlistId);
                values.put(PlaylistStoreColumns.PLAYLIST_NAME, playlistName);
                values.put(PlaylistStoreColumns.SONG_IDS, updateSongIds);
                database.insert(PlaylistStoreColumns.TABLE_NAME, null, values);
                HistoryStore.getInstance(context).addSongs(songs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (playlistCursor != null) {
                playlistCursor.close();
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    private String updateSongIds(String songIds, List<CommonData> songs) {
        StringBuilder newSongIds = new StringBuilder();
        if (TextUtils.isEmpty(songIds)) {
            for (int i = 0; i < songs.size(); i++) {
                if (i == 0) {
                    newSongIds.append(songs.get(i).getSongId());
                } else {
                    newSongIds.append(",");
                    newSongIds.append(songs.get(i).getSongId());
                }
            }
        } else {
            newSongIds.append(songIds);
            String[] ids = songIds.split(",");
            for (CommonData data : songs) {
                boolean contain = false;
                for (String s : ids) {
                    if (data.getSongId() == Integer.parseInt(s)) {
                        contain = true;
                        break;
                    }
                }
                if (!contain) {
                    newSongIds.append(",");
                    newSongIds.append(data.getSongId());
                }
            }
        }
        return newSongIds.toString();
    }

    public interface PlaylistStoreColumns {
        String TABLE_NAME = "playlist_info";

        String ID = "playlist_id";

        String PLAYLIST_NAME = "playlist_name";

        String SONG_IDS = "song_ids";
    }

}
