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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.List;

import code.name.monkey.retromusic.abram.Constants;
import code.name.monkey.retromusic.abram.RemoteConfig;
import code.name.monkey.retromusic.model.CommonData;

public class HistoryStore extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "history.db";
    private static final int MAX_ITEMS_IN_DB = 100;
    private static final int VERSION = 5;
    @Nullable
    private static HistoryStore sInstance = null;

    public HistoryStore(final Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @NonNull
    public static synchronized HistoryStore getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new HistoryStore(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStoreColumns.NAME + " ("
                + RecentStoreColumns.ID + " LONG NOT NULL,"
                + RecentStoreColumns.TIME_PLAYED + " LONG NOT NULL,"
                + RecentStoreColumns.TYPE + " INT NOT NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SongStoreColumns.NAME + " ("
                + SongStoreColumns.ID + " LONG NOT NULL,"
                + SongStoreColumns.SONG + " TEXT NOT NULL,"
                + RecentStoreColumns.TYPE + " INT NOT NULL);");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DownloadStoreColumns.NAME + " ("
                + DownloadStoreColumns.ID + " LONG NOT NULL,"
                + DownloadStoreColumns.PATH + " TEXT NOT NULL,"
                + DownloadStoreColumns.SONG_ID + " INT NOT NULL,"
                + DownloadStoreColumns.DOWNLOAD_PROGRESS + " INT NOT NULL);");
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SongStoreColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DownloadStoreColumns.NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SongStoreColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DownloadStoreColumns.NAME);
        onCreate(db);
    }

    public void addSongId(final long songId, CommonData commonData) {
        if (songId == -1) {
            return;
        }

        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            // remove previous entries
            removeSongId(songId);

            // add the entry
            final ContentValues values = new ContentValues(3);
            values.put(RecentStoreColumns.ID, songId);
            values.put(RecentStoreColumns.TIME_PLAYED, System.currentTimeMillis());
            values.put(RecentStoreColumns.TYPE, commonData.getDataType());
            database.insert(RecentStoreColumns.NAME, null, values);

            // if our db is too large, delete the extra items
            Cursor oldest = null;
            Cursor oldSong = null;
            try {

                oldSong = database.query(SongStoreColumns.NAME, new String[]{SongStoreColumns.ID, SongStoreColumns.TYPE}, SongStoreColumns.ID + "=? AND " + SongStoreColumns.TYPE + "=?", new String[]{String.valueOf(songId), String.valueOf(commonData.getDataType())}, null, null, null);
                if (oldSong != null) {
                    if (!oldSong.moveToFirst()) {
                        ContentValues songValues = new ContentValues();
                        songValues.put(SongStoreColumns.ID, songId);
                        if (commonData.getDataType() == CommonData.TYPE_CLOUD_SONG) {
                            songValues.put(SongStoreColumns.SONG, new Gson().toJson(commonData.getCloudSong()));
                        } else if (commonData.getDataType() == CommonData.TYPE_LOCAL_SONG) {
                            songValues.put(SongStoreColumns.SONG, new Gson().toJson(commonData.getLocalSong()));
                        }
                        songValues.put(SongStoreColumns.TYPE, commonData.getDataType());
                        database.insert(SongStoreColumns.NAME, null, songValues);
                    }
                }

                oldest = database.query(RecentStoreColumns.NAME,
                        new String[]{RecentStoreColumns.TIME_PLAYED}, null, null, null, null,
                        RecentStoreColumns.TIME_PLAYED + " ASC");

                if (oldest != null && oldest.getCount() > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.getCount() - MAX_ITEMS_IN_DB);
                    long timeOfRecordToKeep = oldest.getLong(0);

                    database.delete(RecentStoreColumns.NAME,
                            RecentStoreColumns.TIME_PLAYED + " < ?",
                            new String[]{String.valueOf(timeOfRecordToKeep)});

                }
            } finally {
                if (oldest != null) {
                    oldest.close();
                }
                if (oldSong != null) {
                    oldSong.close();
                }
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public void addOrUpdateDownload(String filePath, CommonData commonData, int progress) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        // 如果下载库已存在该 song_id 的歌曲，则更新记录，否则插入记录
        Cursor oldDownload = database.query(DownloadStoreColumns.NAME, new String[]{DownloadStoreColumns.SONG_ID},
                DownloadStoreColumns.SONG_ID + "=?",
                new String[]{String.valueOf(commonData.getSongId())},
                null, null, null);
        if (oldDownload != null) {
            if (oldDownload.moveToFirst()) {
                ContentValues values = new ContentValues(1);
                if (filePath != null && !filePath.isEmpty()) {
                    values.put(DownloadStoreColumns.PATH, filePath);
                }
                values.put(DownloadStoreColumns.DOWNLOAD_PROGRESS, progress);
                database.update(DownloadStoreColumns.NAME, values,
                        DownloadStoreColumns.SONG_ID + "=?",
                        new String[]{String.valueOf(commonData.getSongId())});
            } else {
                long id = System.currentTimeMillis();
                ContentValues values = new ContentValues();
                values.put(DownloadStoreColumns.ID, id);
                values.put(DownloadStoreColumns.PATH, filePath);
                values.put(DownloadStoreColumns.SONG_ID, commonData.getSongId());
                values.put(DownloadStoreColumns.DOWNLOAD_PROGRESS, progress);
                database.insert(DownloadStoreColumns.NAME, null, values);
                RemoteConfig.INSTANCE.setDownloadNewItemCount(RemoteConfig.INSTANCE.getDownloadNewItemCount() + 1);
            }
            oldDownload.close();
        }

        // 如果曲库不存在该 song_id 的歌曲，则插入一条记录
        Cursor oldSong = database.query(SongStoreColumns.NAME, new String[]{SongStoreColumns.ID, SongStoreColumns.TYPE},
                SongStoreColumns.ID + "=? AND " + SongStoreColumns.TYPE + "=?",
                new String[]{String.valueOf(commonData.getSongId()), String.valueOf(commonData.getDataType())},
                null, null, null);
        if (oldSong != null) {
            if (!oldSong.moveToFirst()) {
                ContentValues songValues = new ContentValues();
                songValues.put(SongStoreColumns.ID, commonData.getSongId());
                if (commonData.getDataType() == CommonData.TYPE_CLOUD_SONG) {
                    songValues.put(SongStoreColumns.SONG, new Gson().toJson(commonData.getCloudSong()));
                }
                songValues.put(SongStoreColumns.TYPE, commonData.getDataType());
                database.insert(SongStoreColumns.NAME, null, songValues);
            }
            oldSong.close();
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public Cursor queryDownloadBySongId(int songId) {
        SQLiteDatabase database = getReadableDatabase();
        return database.query(DownloadStoreColumns.NAME, new String[]{"*"}, DownloadStoreColumns.SONG_ID + " =?",
                new String[]{String.valueOf(songId)}, null, null, null);
    }

    public void removeSongId(final long songId) {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(RecentStoreColumns.NAME, RecentStoreColumns.ID + " = ?", new String[]{
                String.valueOf(songId)
        });

    }

    public void clear() {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(RecentStoreColumns.NAME, null, null);
    }

    public boolean contains(long id) {
        final SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(RecentStoreColumns.NAME,
                new String[]{RecentStoreColumns.ID},
                RecentStoreColumns.ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        boolean containsId = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        return containsId;
    }

    public Cursor queryAllDownload(){
        SQLiteDatabase database = getReadableDatabase();
        return database.query(DownloadStoreColumns.NAME, new String[]{DownloadStoreColumns.SONG_ID},null, null, null, null,
                DownloadStoreColumns.ID+" DESC");
    }

    public Cursor queryRecentIds() {
        final SQLiteDatabase database = getReadableDatabase();
        return database.query(RecentStoreColumns.NAME,
                new String[]{RecentStoreColumns.ID}, null, null, null, null,
                RecentStoreColumns.TIME_PLAYED + " DESC");
    }

    public Cursor queryRecentSongByIds(String ids) {
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery("select * from " + SongStoreColumns.NAME + " where " + SongStoreColumns.ID + " in " + ids, null);
    }

    public void addSongs(List<CommonData> songs) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            for (CommonData commonData : songs) {
                Cursor oldSong = database.query(SongStoreColumns.NAME, new String[]{SongStoreColumns.ID, SongStoreColumns.TYPE}, SongStoreColumns.ID + "=? AND " + SongStoreColumns.TYPE + "=?", new String[]{String.valueOf(commonData.getSongId()), String.valueOf(commonData.getDataType())}, null, null, null);
                if (oldSong != null) {
                    if (!oldSong.moveToFirst()) {
                        ContentValues songValues = new ContentValues();
                        songValues.put(SongStoreColumns.ID, commonData.getSongId());
                        if (commonData.getDataType() == CommonData.TYPE_CLOUD_SONG) {
                            songValues.put(SongStoreColumns.SONG, new Gson().toJson(commonData.getCloudSong()));
                        } else if (commonData.getDataType() == CommonData.TYPE_LOCAL_SONG) {
                            songValues.put(SongStoreColumns.SONG, new Gson().toJson(commonData.getLocalSong()));
                        }
                        songValues.put(SongStoreColumns.TYPE, commonData.getDataType());
                        database.insert(SongStoreColumns.NAME, null, songValues);
                    }
                    oldSong.close();
                }
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public interface RecentStoreColumns {
        String NAME = "recent_history";

        String ID = "song_id";

        String TIME_PLAYED = "time_played";

        String TYPE = "song_type";
    }

    public interface SongStoreColumns {
        String NAME = "recent_song";

        String ID = "song_id";

        String SONG = "song_json";

        String TYPE = "song_type";
    }

    public interface DownloadStoreColumns {
        String NAME = "song_download";

        String ID = "download_id";

        String PATH = "download_path";

        String SONG_ID = "song_id";

        String DOWNLOAD_PROGRESS = "download_progress";
    }
}
