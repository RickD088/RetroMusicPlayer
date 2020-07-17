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

package code.name.monkey.retromusic.providers

import android.content.Context
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.Result
import code.name.monkey.retromusic.Result.Error
import code.name.monkey.retromusic.Result.Success
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.abram.RemoteConfig
import code.name.monkey.retromusic.adapter.HomeAdapter
import code.name.monkey.retromusic.extensions.toCommonData
import code.name.monkey.retromusic.loaders.*
import code.name.monkey.retromusic.model.*
import code.name.monkey.retromusic.model.Genre
import code.name.monkey.retromusic.providers.interfaces.Repository
import code.name.monkey.retromusic.rest.lastfm.LastFmClient
import code.name.monkey.retromusic.rest.lastfm.model.LastFmAlbum
import code.name.monkey.retromusic.rest.lastfm.model.LastFmArtist
import code.name.monkey.retromusic.rest.music.MusicClient
import code.name.monkey.retromusic.rest.music.model.*
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.RetroUtil
import java.io.IOException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val context: Context) : Repository {

    override suspend fun startupInfo(): Result<ServerInfo> = safeApiCall(
            call = {
                val info = MusicClient.getApiService().startupInfo(
                        RemoteConfig.getUid(),
                        RemoteConfig.getDid(),
                        RemoteConfig.getString(Constants.CFG_LOCATION),
                        RemoteConfig.getString(Constants.CFG_LANGUAGE),
                        RetroUtil.getLocalLanguage(context)
                )
                Success(info)
            },
            errorMessage = "Error"
    )

    override suspend fun startupReg(): Result<UserInfo> = safeApiCall(
            call = {
                val userName = PreferenceUtil.getInstance(context).userName
                val info =
                        MusicClient.getApiService().startupReg(
                                RemoteConfig.getUid(),
                                RemoteConfig.getDid(),
                                RemoteConfig.getAvatar(),
                                RemoteConfig.getEmail(),
                                RetroUtil.getLocalLanguage(context)
                        )
                Success(info.data)
            },
            errorMessage = "Error"
    )

    override suspend fun allAlbums(): Result<ArrayList<Album>> {
        return try {
            val albums = AlbumLoader.getAllAlbums(context)
            if (albums.isNotEmpty()) {
                Success(albums)
            } else {
                Error(Throwable("No items found"))
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun albumById(albumId: Int): Result<Album> {
        return try {
            val album = AlbumLoader.getAlbum(context, albumId)
            if (album != null) {
                Success(album)
            } else {
                Error(Throwable("No album"))
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun allArtists(): Result<ArrayList<Artist>> {
        return try {
            val artists = ArtistLoader.getAllArtists(context)
            if (artists.isNotEmpty()) {
                Success(artists)
            } else {
                Error(Throwable("No items found"))
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun allPlaylists(): Result<ArrayList<Playlist>> {
        return try {
            val playlists = PlaylistLoader.getAllPlaylists(context)
            if (playlists.isNotEmpty()) {
                Success(playlists)
            } else {
                Error(Throwable("No items found"))
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun allGenres(): Result<ArrayList<Genre>> {
        return try {
            val genres = GenreLoader.getAllGenres(context)
            if (genres.isNotEmpty()) {
                Success(genres)
            } else {
                Error(Throwable("No items found"))
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun search(query: String?): Result<MutableList<Any>> {
        return try {
            val result = SearchLoader.searchAll(context, query)
            if (result.isNotEmpty()) {
                Success(result)
            } else {
                Error(Throwable("No items found"))
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun allSongs(): Result<ArrayList<Song>> {
        return try {
            val songs = SongLoader.getAllSongs(context)
            if (songs.isEmpty()) {
                Error(Throwable("No items found"))
            } else {
                Success(songs)
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getPlaylistSongs(playlist: Playlist): Result<ArrayList<CommonData>> {
        return try {
            val songs: ArrayList<CommonData> = if (playlist is AbsCustomPlaylist) {
                playlist.getSongs(context)
            } else {
                PlaylistSongsLoader.getPlaylistSongsFromDb(context, playlist.id)
            }
            Success(songs)
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getGenre(genreId: Int): Result<ArrayList<Song>> {
        return try {
            val songs = GenreLoader.getSongs(context, genreId)
            if (songs.isEmpty()) {
                Error(Throwable("No items found"))
            } else {
                Success(songs)
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun recentArtists(): Result<Home> {
        return try {
            val artists = LastAddedSongsLoader.getLastAddedArtists(context)
            if (artists.isEmpty()) {
                Error(Throwable("No items found"))
            } else {
                Success(
                        Home(
                                0,
                                R.string.recent_artists,
                                artists.toCommonData(),
                                HomeAdapter.RECENT_ARTISTS,
                                R.drawable.ic_artist_white_24dp
                        )
                )
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun recentAlbums(): Result<Home> {
        return try {
            val albums = LastAddedSongsLoader.getLastAddedAlbums(context)
            if (albums.isEmpty()) {
                Error(Throwable("No items found"))
            } else {
                Success(
                        Home(
                                1,
                                R.string.recent_albums,
                                albums,
                                HomeAdapter.RECENT_ALBUMS,
                                R.drawable.ic_album_white_24dp
                        )
                )
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun topAlbums(): Result<Home> {
        return try {
            val albums = TopAndRecentlyPlayedTracksLoader.getTopAlbums(context)
            if (albums.isEmpty()) {
                Error(Throwable("No items found"))
            } else {
                Success(
                        Home(
                                3,
                                R.string.top_albums,
                                albums,
                                HomeAdapter.TOP_ALBUMS,
                                R.drawable.ic_album_white_24dp
                        )
                )
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun topArtists(): Result<Home> {
        return try {
            val artists = TopAndRecentlyPlayedTracksLoader.getTopArtists(context)
            if (artists.isEmpty()) {
                Error(Throwable("No items found"))
            } else {

                Success(
                        Home(
                                2,
                                R.string.top_artists,
                                artists.toCommonData(),
                                HomeAdapter.TOP_ARTISTS,
                                R.drawable.ic_artist_white_24dp
                        )
                )
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun favoritePlaylist(): Result<Home> {
        return try {
            val playlists = PlaylistLoader.getFavoritePlaylist(context)
            if (playlists.isEmpty()) {
                Error(Throwable("No items found"))
            } else {
                Success(
                        Home(
                                4,
                                R.string.favorites,
                                playlists.toCommonData(),
                                HomeAdapter.PLAYLISTS,
                                R.drawable.ic_favorite_white_24dp
                        )
                )
            }
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun artistInfo(
            name: String,
            lang: String?,
            cache: String?
    ): Result<LastFmArtist> = safeApiCall(
            call = {
                Success(LastFmClient.getApiService().artistInfo(name, lang, cache))
            },
            errorMessage = "Error"

    )

    override suspend fun albumInfo(
            artist: String,
            album: String
    ): Result<LastFmAlbum> = safeApiCall(
            call = {
                Success(LastFmClient.getApiService().albumInfo(artist, album))
            },
            errorMessage = "Error"
    )

    override suspend fun artistById(artistId: Int): Result<Artist> {
        return try {
            val artist = ArtistLoader.getArtist(context, artistId)
            return Success(artist)
        } catch (e: Exception) {
            Error(Throwable("Error loading artist"))
        }
    }

    override suspend fun discoverAll(): Result<MusicHome> = safeApiCall(
            call = {
                val discover = if (Constants.DEBUG) MusicHome() else MusicClient.getApiService().discoverAll(
                        RetroUtil.getLocalLanguage(context)
                )
                Success(discover)
            },
            errorMessage = "Error"
    )

    override suspend fun loadArtist(artistId: Int): Result<MusicArtist> = safeApiCall(
            {
                val response = MusicClient.getApiService().getArtistSong(
                        artistId,
                        200,
                        0
                )
                if (response.status == 200) {
                    Success(response.data)
                } else {
                    Error(Throwable(response.errorMsg))
                }
            }, "Error"
    )

    override suspend fun playlistById(playlistId: Int): Result<ArrayList<SongBean>> = safeApiCall({
        val response = MusicClient.getApiService().getPlaylistSong(
                playlistId,
                200,
                0,
                RetroUtil.getLocalLanguage(context)
        )
        if (response.status == 200) {
            Success(response.data)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")

    override suspend fun findSongs(query: String, count: Int): Result<ArrayList<SongBean>> = safeApiCall({
        if (query.isEmpty()) {
            return@safeApiCall Success(ArrayList<SongBean>())
        }
        val response = MusicClient.getApiService().findSong(
                query,
                RetroUtil.getLocalLanguage(context),
                count / Constants.PAGE_SIZE,
                Constants.PAGE_SIZE
        )
        if (response.status == 200) {
            Success(response.data.songs)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")

    override suspend fun getKeywords(): Result<ArrayList<SearchKeywords>> = safeApiCall({
        val response = MusicClient.getApiService().getKeywords(
                RetroUtil.getLocalLanguage(context)
        )
        if (response.status == 200) {
            Success(response.data)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")

    override suspend fun getUserData(): Result<UserData> = safeApiCall({
        val response = MusicClient.getApiService().getUserData(
                RemoteConfig.getUid()
        )
        if (response.status == 200) {
            Success(response.data)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")

    override suspend fun syncUserData(): Result<String> = safeApiCall({
        val response = MusicClient.getApiService().syncUserData(
                RemoteConfig.getUid(),
                UserData()
        )
        if (response.status == 200) {
            Success(response.data)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")

    override suspend fun getRewardInfo(): Result<RewardData> = safeApiCall({
        val response = RewardClient.getApiService().startReward(
                RemoteConfig.getDid(),
                RetroUtil.getLocalLanguage(context),
                RemoteConfig.getPushToken()
        )
        if (response.status == 200) {
            println("RewardInfo: ${response.data}")
            Success(response.data)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")

    override suspend fun winReward(prize: Prize, multiple: Int, card: String): Result<RewardData> = safeApiCall({
        val response = RewardClient.getApiService().winReward(
                RemoteConfig.getDid(),
                RetroUtil.getLocalLanguage(context),
                multiple,
                prize,
                card
        )
        if (response.status == 200) {
            println("winReward: ${response.data}")
            Success(response.data)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")

    override suspend fun startSpin(card: String): Result<RewardData> = safeApiCall({
        val response = RewardClient.getApiService().startSpin(
                RemoteConfig.getDid(),
                RetroUtil.getLocalLanguage(context),
                card
        )
        if (response.status == 200) {
            println("winReward: ${response.data}")
            Success(response.data)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")

    override suspend fun buyReward(action: Int, card: String, address: String): Result<RewardData> = safeApiCall({
        val response = RewardClient.getApiService().buyReward(
                RemoteConfig.getDid(),
                address,
                RetroUtil.getLocalLanguage(context),
                action,
                card
        )
        if (response.status == 200) {
            println("buyReward: ${response.data}")
            Success(response.data)
        } else {
            Error(Throwable(response.errorMsg))
        }
    }, "Error")
}


suspend fun <T : Any> safeApiCall(call: suspend () -> Result<T>, errorMessage: String): Result<T> =
        try {
            call.invoke()
        } catch (e: Exception) {
            Error(IOException(errorMessage, e))
        }
