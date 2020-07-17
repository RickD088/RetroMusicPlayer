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

package code.name.monkey.retromusic.mvp.presenter

import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.Result.Error
import code.name.monkey.retromusic.Result.Success
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.loaders.LastAddedSongsLoader
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.model.Playlist
import code.name.monkey.retromusic.mvp.BaseView
import code.name.monkey.retromusic.mvp.Presenter
import code.name.monkey.retromusic.mvp.PresenterImpl
import code.name.monkey.retromusic.providers.interfaces.Repository
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by hemanths on 20/08/17.
 */
interface PlaylistSongsView : BaseView {

    fun songs(songs: List<CommonData>)
}

interface PlaylistSongsPresenter : Presenter<PlaylistSongsView> {
    fun loadPlaylistSongs(playlist: Playlist)

    class PlaylistSongsPresenterImpl @Inject constructor(
        private val repository: Repository
    ) : PresenterImpl<PlaylistSongsView>(), PlaylistSongsPresenter, CoroutineScope {

        private var job: Job = Job()

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        override fun loadPlaylistSongs(playlist: Playlist) {
            launch {
                when (val songs = repository.getPlaylistSongs(playlist)) {
                    is Success -> withContext(Dispatchers.Main) {
                        view?.let {
                            val allSongs = mutableListOf<CommonData>()
                            if (playlist.id == Constants.UI_DOWNLOAD_PLAYLIST_ID) {
                                // abram 伪装成播放列表的下载项，需要特别填充数据
                                allSongs.addAll(LastAddedSongsLoader.getDownloadSongs(App.getContext()))
                            }
                            allSongs.addAll(songs.data)
                            it.songs(allSongs)
                        }
                    }
                    is Error -> withContext(Dispatchers.Main) { view?.showEmptyView() }
                }
            }
        }

        override fun detachView() {
            super.detachView()
            job.cancel()
        }
    }
}