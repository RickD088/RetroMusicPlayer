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

import android.util.Log
import android.widget.Toast
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.Result
import code.name.monkey.retromusic.abram.RemoteConfig
import code.name.monkey.retromusic.adapter.HomeAdapter
import code.name.monkey.retromusic.extensions.toCommonData
import code.name.monkey.retromusic.model.Home
import code.name.monkey.retromusic.mvp.BaseView
import code.name.monkey.retromusic.mvp.Presenter
import code.name.monkey.retromusic.mvp.PresenterImpl
import code.name.monkey.retromusic.providers.interfaces.Repository
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface HomeView : BaseView {
    fun sections(sections: List<Home>)
}

interface HomePresenter : Presenter<HomeView> {
    fun loadStartupInfo()
    fun startupReg()
    fun loadSections()
    fun getUserData()

    companion object {
        var discover_home: ArrayList<Home>? = null
    }

    class HomePresenterImpl @Inject constructor(
            private val repository: Repository
    ) : PresenterImpl<HomeView>(), HomePresenter, CoroutineScope {

        private val TAG = javaClass.simpleName
        private val job = Job()

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        override fun detachView() {
            super.detachView()
            job.cancel()
        }

        override fun loadStartupInfo() {
            launch {
                when (val info = repository.startupInfo()) {
                    is Result.Success -> {
                        Log.d(TAG, "startupInfo: ${info.data.status}")
                        if (info.data.status == 200) {
                            val config = info.data.data
                            RemoteConfig.updateApiSettings(config)
                        }
                    }
                }
                withContext(Dispatchers.Main) {

                }
            }
        }

        override fun startupReg() {
            launch {
                when (val info = repository.startupReg()) {
                    is Result.Success -> {
                        RemoteConfig.setTryAutoSync(true)
                        RemoteConfig.setRegistered(true)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                            App.getContext(),
                            if (RemoteConfig.isRegistered()) App.getContext().getString(R.string.register_success)
                            else App.getContext().getString(R.string.register_failed),
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        override fun getUserData() {
        }

        override fun loadSections() {
            launch {
                if (discover_home == null) {
                    val discover = repository.discoverAll()
                    val list = ArrayList<Home>()
                    val recentArtistResult = listOf(
                        repository.topArtists(),
                        repository.topAlbums(),
                        repository.recentArtists(),
                        repository.recentAlbums(),
                        repository.favoritePlaylist()
                    )
                    when (discover) {
                        is Result.Success -> {
                            Log.d(TAG, "discoverAll: ${discover.data.status}")
                            if (discover.data.status == 200) {
                                // 如果在激活状态下请求成功，则不需要再次为激活而刷新数据库
                                val allData = discover.data.data
                                if (allData.hotSingerPlaylists.isNotEmpty()) {
                                    val hotSinger = Home(
                                        6,
                                        R.string.top_artists,
                                        allData.hotSingerPlaylists.toCommonData(),
                                        HomeAdapter.ONLINE_HOT_ARTISTS,
                                        R.drawable.ic_artist_white_24dp
                                    )
                                    list.add(hotSinger)
                                    Log.d(TAG, "hotSinger: ${allData.hotSingerPlaylists.size}")
                                }
                                if (allData.generalPlaylists.isNotEmpty()) {
                                    val generalPlaylist = Home(
                                        5,
                                        R.string.top_albums,
                                        allData.generalPlaylists.toCommonData(),
                                        HomeAdapter.GENERAL_PLAYLISTS,
                                        R.drawable.ic_album_white_24dp
                                    )
                                    list.add(generalPlaylist)
                                    Log.d(TAG, "generalPlaylist: ${allData.generalPlaylists.size}")
                                }
                            }
                        }
                    }
                    for (r in recentArtistResult) {
                        when (r) {
                            is Result.Success -> {
                                list.add(r.data)
                                Log.d(
                                    TAG,
                                    "recentArtistResult ${r.data.title}: ${r.data.arrayList.size}"
                                )
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (view == null) {
                            discover_home = list
                        } else if (list.isNotEmpty()) {
                            view?.sections(list)
                        } else {
                            view?.showEmptyView()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        discover_home?.let {
                            if (it.isNotEmpty()) {
                                view?.sections(it)
                            } else {
                                view?.showEmptyView()
                            }
                        }
                        discover_home = null
                    }
                }
            }
        }
    }
}