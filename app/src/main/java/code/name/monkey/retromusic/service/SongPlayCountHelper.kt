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

package code.name.monkey.retromusic.service

import code.name.monkey.retromusic.helper.StopWatch
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.model.Song

class SongPlayCountHelper {

    private val stopWatch = StopWatch()
    var song = CommonData(CommonData.TYPE_EMPTY)
        private set

    fun shouldBumpPlayCount(): Boolean {
        if (song.localSong()) {
            return song.getLocalSong().duration * 0.5 < stopWatch.elapsedTime
        } else if (song.cloudSong()) {
            return song.getCloudSong().getDuration() * 0.5 < stopWatch.elapsedTime
        } else {
            return false
        }
    }

    fun notifySongChanged(song: CommonData) {
        synchronized(this) {
            stopWatch.reset()
            this.song = song
        }
    }

    fun notifyPlayStateChanged(isPlaying: Boolean) {
        synchronized(this) {
            if (isPlaying) {
                stopWatch.start()
            } else {
                stopWatch.pause()
            }
        }
    }

    companion object {
        val TAG: String = SongPlayCountHelper::class.java.simpleName
    }
}