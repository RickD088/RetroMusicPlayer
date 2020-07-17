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

import android.content.Context
import android.util.Log
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.Result.Error
import code.name.monkey.retromusic.Result.Success
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.abram.EventLog
import code.name.monkey.retromusic.model.MoreData
import code.name.monkey.retromusic.mvp.BaseView
import code.name.monkey.retromusic.mvp.Presenter
import code.name.monkey.retromusic.mvp.PresenterImpl
import code.name.monkey.retromusic.providers.interfaces.Repository
import code.name.monkey.retromusic.util.RewardManager
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Created by hemanths on 20/08/17.
 */

interface SearchView : BaseView {

    fun showKeywords(data: MutableList<Any>)

    fun showData(data: MutableList<Any>)
}

interface SearchPresenter : Presenter<SearchView> {

    fun search(query: String?)

    fun loadMore(searchCount: Int)

    class SearchPresenterImpl @Inject constructor(
            private val context: Context,
            private val repository: Repository
    ) : PresenterImpl<SearchView>(), SearchPresenter, CoroutineScope {

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        private var job: Job = Job()

        private val TAG = javaClass.simpleName
        private var curKey = ""
        private val searchList = mutableListOf<Any>()
        private val moreData: MoreData by lazy {
            MoreData(context.getString(R.string.load_more), false)
        }

        override fun attachView(view: SearchView) {
            super.attachView(view)
            launch {
                val data = mutableListOf<Any>()
                if (Constants.SEARCH_HISTORY_RECENT.isNotEmpty()) {
                    data.addAll(Constants.SEARCH_HISTORY_RECENT)
                }
                when (val result = repository.getKeywords()) {
                    is Success -> {
                        if (result.data.isNotEmpty()) {
                            Constants.SEARCH_HOT_KEYWORDS.clear()
                            Constants.SEARCH_HOT_KEYWORDS.addAll(result.data.map {
                                it.keyword?.let { keyword ->
                                    it.icon?.let { icon ->
                                        "$keyword${Constants.SEPARATOR_SEARCH_KEYWORDS}$icon"
                                    } ?: keyword
                                } ?: ""
                            })
                            data.addAll(Constants.SEARCH_HOT_KEYWORDS)
                        }
                    }
                    is Error -> {
                    }
                }
                withContext(Dispatchers.Main) {
                    if (data.isNotEmpty()) {
                        // 显示搜索热词
                        view?.showKeywords(data)
                    }
                }
            }
        }

        override fun detachView() {
            super.detachView()
            job.cancel()
        }

        override fun search(query: String?) {
            query?.let {
                if (it.isNotBlank()) {
                    launch {
                        val data = mutableListOf<Any>()
                        if (it.length >= Constants.MIN_SEARCH_KEYWORD_LENGTH) {
                            // 搜索后台曲库
                            val key = if (it.startsWith(Constants.SEPARATOR_SEARCH_KEYWORDS))
                                it.removePrefix(Constants.SEPARATOR_SEARCH_KEYWORDS)
                            else if (it.contains(Constants.SEPARATOR_SEARCH_KEYWORDS))
                                it.split(Constants.SEPARATOR_SEARCH_KEYWORDS)[0] else it
                            curKey = key
                            Log.d(TAG, Constants.STATS_MUSIC_SEARCH)
                            EventLog.log(Constants.STATS_MUSIC_SEARCH)
                            when (val result = repository.findSongs(key, 0)) {
                                is Success -> {
                                    if (result.data.isNotEmpty()) {
                                        Log.d(TAG, "${Constants.STATS_MUSIC_SEARCH}_${Constants.STATS_SUCCESS}")
                                        EventLog.log("${Constants.STATS_MUSIC_SEARCH}_${Constants.STATS_SUCCESS}")
                                        data.add(context.getString(R.string.songs))
                                        data.addAll(result.data)
                                        // 保存搜索记录
                                        if (it.length >= Constants.MIN_SEARCH_KEYWORD_LENGTH * 2
                                                && !Constants.SEARCH_HISTORY_RECENT.contains(Constants.SEPARATOR_SEARCH_KEYWORDS + it)
                                                && !Constants.SEARCH_HOT_KEYWORDS.contains(it)) {
                                            Constants.SEARCH_HISTORY_RECENT.add(Constants.SEPARATOR_SEARCH_KEYWORDS + it)
                                        }
                                        if (result.data.size >= Constants.PAGE_SIZE) {
                                            moreData.loading = false
                                            data.add(moreData)
                                        }
                                    } else {
                                        Log.d(TAG, "${Constants.STATS_MUSIC_SEARCH}_${Constants.STATS_NO_RESULT}")
                                        EventLog.log("${Constants.STATS_MUSIC_SEARCH}_${Constants.STATS_NO_RESULT}")
                                    }
                                }
                                is Error -> {
                                    Log.d(TAG, "${Constants.STATS_MUSIC_SEARCH}_${Constants.STATS_FAIL}")
                                    EventLog.log("${Constants.STATS_MUSIC_SEARCH}_${Constants.STATS_FAIL}")
                                }
                            }
                            // 搜索本地曲库
                            when (val result = repository.search(key)) {
                                is Success -> data.addAll(result.data)
                                is Error -> {
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            if (it.length >= Constants.MIN_SEARCH_KEYWORD_LENGTH) {
                                searchList.clear()
                                searchList.addAll(data)
                                RewardManager.addSearchCount(context)
                            }
                            if (data.isEmpty()) {
                                view?.showEmptyView()
                            } else {
                                view?.showData(data)
                            }
                        }
                    }
                }
            }
        }

        override fun loadMore(searchCount: Int) {
            launch {
                when (val result = repository.findSongs(curKey, searchCount)) {
                    is Success -> {
                        moreData.loading = false
                        if (searchList.indexOf(moreData) > -1) {
                            searchList.addAll(searchList.indexOf(moreData), result.data)
                            if (result.data.size < Constants.PAGE_SIZE) {
                                searchList.remove(moreData)
                            }
                        } else {
                            searchList.addAll(result.data)
                        }
                        withContext(Dispatchers.Main) {
                            view.showData(searchList)
                        }
                    }
                    is Error -> {

                    }
                }
            }
        }
    }
}


