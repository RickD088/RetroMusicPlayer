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

package code.name.monkey.retromusic.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import code.name.monkey.retromusic.extensions.ArrayListExKt;
import code.name.monkey.retromusic.interfaces.CommonDataConverter;
import code.name.monkey.retromusic.loaders.PlaylistSongsLoader;


public class Playlist implements Parcelable, CommonDataConverter {

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        public Playlist createFromParcel(Parcel source) {
            return new Playlist(source);
        }

        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public final long id;

    public final String name;

    public final String songIds;

    public Playlist(final long id, final String name, final String songIds) {
        this.id = id;
        this.name = name;
        this.songIds = songIds;
    }

    public Playlist() {
        this.id = -1;
        this.name = "";
        this.songIds = "";
    }

    protected Playlist(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.songIds = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Playlist playlist = (Playlist) o;

        if (id != playlist.id) {
            return false;
        }
        return name != null ? name.equals(playlist.name) : playlist.name == null;

    }

    @NonNull
    public ArrayList<CommonData> getSongs(@NonNull Context context) {
        // this default implementation covers static playlists
        return PlaylistSongsLoader.INSTANCE.getPlaylistSongsFromDb(context, id);
    }

    @Override
    public int hashCode() {
        long result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return (int) result;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", songIds='" + songIds + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.songIds);
    }


    @NotNull
    @Override
    public CommonData convertToCommonData() {
        return new CommonData(CommonData.TYPE_LOCAL_PLAYLIST, this, null);
    }
}
