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

package code.name.monkey.retromusic.providers.interfaces

import code.name.monkey.retromusic.Result
import code.name.monkey.retromusic.model.*
import code.name.monkey.retromusic.model.Genre
import code.name.monkey.retromusic.rest.lastfm.model.LastFmAlbum
import code.name.monkey.retromusic.rest.lastfm.model.LastFmArtist
import code.name.monkey.retromusic.rest.music.model.*
import okhttp3.ResponseBody

/**
 * Created by hemanths on 11/08/17.
 */

interface Repository {

    suspend fun startupInfo(): Result<ServerInfo>

    suspend fun startupReg(): Result<UserInfo>

    suspend fun allAlbums(): Result<ArrayList<Album>>

    suspend fun albumById(albumId: Int): Result<Album>

    suspend fun allSongs(): Result<ArrayList<Song>>

    suspend fun allArtists(): Result<ArrayList<Artist>>

    suspend fun allPlaylists(): Result<ArrayList<Playlist>>

    suspend fun allGenres(): Result<ArrayList<Genre>>

    suspend fun search(query: String?): Result<MutableList<Any>>

    suspend fun getPlaylistSongs(playlist: Playlist): Result<ArrayList<CommonData>>

    suspend fun getGenre(genreId: Int): Result<ArrayList<Song>>

    suspend fun recentArtists(): Result<Home>

    suspend fun topArtists(): Result<Home>

    suspend fun topAlbums(): Result<Home>

    suspend fun recentAlbums(): Result<Home>

    suspend fun favoritePlaylist(): Result<Home>

    suspend fun artistInfo(name: String, lang: String?, cache: String?): Result<LastFmArtist>

    suspend fun albumInfo(artist: String, album: String): Result<LastFmAlbum>

    suspend fun artistById(artistId: Int): Result<Artist>

    suspend fun discoverAll(): Result<MusicHome>

    suspend fun loadArtist(artistId: Int): Result<MusicArtist>

    suspend fun playlistById(playlistId: Int): Result<ArrayList<SongBean>>

    suspend fun findSongs(query: String, count: Int): Result<ArrayList<SongBean>>

    suspend fun getKeywords(): Result<ArrayList<SearchKeywords>>

    suspend fun getUserData(): Result<UserData>

    suspend fun syncUserData(): Result<String>

    suspend fun getRewardInfo(): Result<RewardData>

    suspend fun winReward(prize: Prize, multiple: Int, card: String): Result<RewardData>

    suspend fun startSpin(card: String): Result<RewardData>

    suspend fun buyReward(action: Int, card: String, address: String): Result<RewardData>
}