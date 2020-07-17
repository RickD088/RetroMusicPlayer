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

package code.name.monkey.retromusic.helper.menu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.abram.AbramAds
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.abram.EventLog
import code.name.monkey.retromusic.activities.tageditor.AbsTagEditorActivity
import code.name.monkey.retromusic.activities.tageditor.SongTagEditorActivity
import code.name.monkey.retromusic.dialogs.AddToPlaylistDialog
import code.name.monkey.retromusic.dialogs.DeleteSongsDialog
import code.name.monkey.retromusic.dialogs.SongDetailDialog
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.interfaces.PaletteColorHolder
import code.name.monkey.retromusic.misc.WeakContextAsyncTask
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.providers.HistoryStore
import code.name.monkey.retromusic.rest.music.MusicClient
import code.name.monkey.retromusic.service.MusicService
import code.name.monkey.retromusic.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.*
import java.lang.Exception
import java.util.*

object SongMenuHelper {
    private val TAG = javaClass.simpleName
    var MENU_RES = R.menu.menu_item_song

    fun getMenuRes(type: Int): Int {
        MENU_RES = if (type == CommonData.TYPE_CLOUD_SONG) {
            R.menu.menu_item_cloud_song
        } else {
            R.menu.menu_item_song
        }
        return MENU_RES
    }

    fun handleMenuClick(activity: FragmentActivity, data: CommonData, menuItemId: Int): Boolean {
        when (menuItemId) {
            R.id.action_set_as_ringtone -> {
                if (data.localSong()) {
                    if (RingtoneManager.requiresDialog(activity)) {
                        RingtoneManager.getDialog(activity)
                    } else {
                        val ringtoneManager = RingtoneManager(activity)
                        ringtoneManager.setRingtone(data.getLocalSong())
                    }
                }
                return true
            }
            R.id.action_share -> {
                activity.startActivity(
                        Intent.createChooser(
                                MusicUtil.createShareSongFileIntent(data, activity),
                                null
                        )
                )
                return true
            }
            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(data)
                        .show(activity.supportFragmentManager, "DELETE_SONGS")
                return true
            }
            R.id.action_add_to_playlist -> {
                AddToPlaylistDialog.create(data)
                        .show(activity.supportFragmentManager, "ADD_PLAYLIST")
                return true
            }
            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(data)
                return true
            }
            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(data)
                return true
            }
            R.id.action_tag_editor -> {
                if (data.localSong()) {
                    val tagEditorIntent = Intent(activity, SongTagEditorActivity::class.java)
                    tagEditorIntent.putExtra(AbsTagEditorActivity.EXTRA_ID, data.getLocalSong().id)
                    if (activity is PaletteColorHolder)
                        tagEditorIntent.putExtra(
                                AbsTagEditorActivity.EXTRA_PALETTE,
                                (activity as PaletteColorHolder).paletteColor
                        )
                    activity.startActivity(tagEditorIntent)
                }
                return true
            }
            R.id.action_details -> {
                SongDetailDialog.create(data)
                        .show(activity.supportFragmentManager, "SONG_DETAILS")
                return true
            }
            R.id.action_go_to_album -> {
                if (data.localSong()) {
                    NavigationUtil.goToAlbum(activity, data.getLocalSong().albumId, data)
                }
                return true
            }
            R.id.action_go_to_artist -> {
                if (data.localSong()) {
                    NavigationUtil.goToArtist(activity, data.getLocalSong().artistId, data)
                }
                return true
            }
            R.id.action_save_song -> {
                if (data.cloudSong()) {
                    if (FileUtil.getMusicDownloadProgress(activity, data.getSongId(), data.getCloudSong().cache) > Constants.DOWNLOAD_ASSUMED_COMPLETION_PERCENTAGE
                            || FileUtil.isMusicCached(data.getCloudSong().cache)) {
                        Toast.makeText(activity, "${activity.getString(R.string.saved)}: ${data.getSongTitle()}", Toast.LENGTH_LONG).show()
                    } else {
                        downloadSong(activity, data)
                    }
                } else {
                    Toast.makeText(activity, "${activity.getString(R.string.saved)}: ${data.getSongTitle()}", Toast.LENGTH_LONG).show()
                }
                return true
            }
        }
        return false
    }

    fun downloadSong(activity: Activity, song: CommonData) {
        if (!activity.isDestroyed && !activity.isFinishing) {
            doDownloadSong(activity, song)
        }
    }

    fun doDownloadSong(activity: Activity, song: CommonData) {
        Log.d(TAG, Constants.STATS_MUSIC_DOWNLOAD)
        EventLog.log(Constants.STATS_MUSIC_DOWNLOAD)
        song?.getCloudSong()?.cache?.let { url ->
            Toast.makeText(activity, "${activity.getString(R.string.downloading)}: ${song.getSongTitle()}", Toast.LENGTH_LONG).show()
            val downloadProgress = FileUtil.getMusicDownloadProgress(activity, song.getSongId(), url)
            // 如果进度已经完成，则提示下载完成
            if (downloadProgress > Constants.DOWNLOAD_ASSUMED_COMPLETION_PERCENTAGE) {
                Toast.makeText(activity, "${activity.getString(R.string.saved)}: ${song.getSongTitle()}", Toast.LENGTH_LONG).show()
            } else {
                // 先更新下载数据库（如果已存在，则无副作用）
                HistoryStore.getInstance(activity).addOrUpdateDownload("", song, if (downloadProgress > 0) downloadProgress else 0)
                activity.sendBroadcast(Intent(MusicService.MEDIA_STORE_CHANGED))
                if (RetroUtil.getNetWorkState(activity) != Constants.PLAY_ON_NONE_NETWORK) {
                    // 然后有网络则开始下载
                    Constants.DOWNLOAD_INPROGRESS_QUEUE.add(song.getSongId().toString())
                    val task = SaveSongAsyncTask(activity).executeOnExecutor(Constants.DOWNLOAD_THREAD_POOL_EXECUTOR, song)
                    Handler().postDelayed({
                        // 超时还没有进度则取消任务，重新添加一次
                        val downloadProgressNew = FileUtil.getMusicDownloadProgress(activity, song.getSongId(), url)
                        if (task.status == AsyncTask.Status.RUNNING && downloadProgressNew == downloadProgress) {
                            task.cancel(true)
                            Constants.DOWNLOAD_INPROGRESS_QUEUE.remove(song.getSongId().toString())
                            HistoryStore.getInstance(activity).addOrUpdateDownload("", song, -1)
                            activity.sendBroadcast(Intent(MusicService.MEDIA_STORE_CHANGED))
                            Toast.makeText(activity, "${activity.getString(R.string.save_failed)}: ${song.getSongTitle()}", Toast.LENGTH_LONG).show()
                        }
                    }, Constants.ONE_MINUTE_MS)
                } else {
                    // 无网络则提示失败，等待用户手动操作
                    Toast.makeText(activity, activity.getString(R.string.network_unavailable), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    class SaveSongAsyncTask internal constructor(context: Context) :
            WeakContextAsyncTask<CommonData, String, String>(context) {

        override fun doInBackground(vararg params: CommonData): String {
            val song = params[0]
            song?.getCloudSong()?.cache?.let { url ->
                val response =
                        MusicClient.getApiService().downloadFile(url).execute()
                if (response.isSuccessful) {
                    var fileName = ""
                    if (Constants.DOWNLOAD_READABLE_NAME) {
                        if (!song.getSongTitle().isNullOrBlank() && !song.getSongSinger().isNullOrBlank()) {
                            fileName = "${song.getSongTitle()}-${song.getSongSinger()}.mp3"
                        } else if (!song.getSongTitle().isNullOrBlank()) {
                            fileName = "${song.getSongTitle()}.mp3"
                        }
                    } else {
                        fileName = url.split("/").last()
                        try {
                            response.headers()["Content-Disposition"]?.let {
                                fileName = it.split("filename=").last()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (fileName.isNullOrBlank()
                            || !fileName.endsWith(".mp3", false)
                    ) {
                        fileName = "${UUID.randomUUID()}.mp3"
                    }
                    return saveFile(fileName, response.body(), song)
                } else {
                    Constants.DOWNLOAD_INPROGRESS_QUEUE.remove(song.getSongId().toString())
                    context?.let {
                        HistoryStore.getInstance(it).addOrUpdateDownload("", song, -2)
                        it.sendBroadcast(Intent(MusicService.MEDIA_STORE_CHANGED))
                    }
                    Log.d(TAG, "${Constants.STATS_MUSIC_DOWNLOAD}_${Constants.STATS_FAIL}")
                    EventLog.log("${Constants.STATS_MUSIC_DOWNLOAD}_${Constants.STATS_FAIL}")
                }
            }
            return (context?.getString(R.string.save_failed)
                    ?: "Failed to download") + ": ${song.getSongTitle()}"
        }

        private fun saveFile(
                fileName: String,
                response: ResponseBody?,
                data: CommonData
        ): String {
            context?.let { ctx ->
                val dir = File(Environment.getExternalStorageDirectory(), "Songs")
                if (!dir.exists())
                    dir.mkdirs()
                val targetFile = File(dir, fileName)
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    response?.let { body ->
                        val size = body.contentLength()
                        if (targetFile.length() < size) {
                            val buffer = ByteArray(4096)
                            var downloaded = 0L
                            var progress = 0L
                            inputStream = body.byteStream()
                            outputStream = FileOutputStream(targetFile)
                            inputStream?.let { input ->
                                while (true) {
                                    val read = input.read(buffer)
                                    if (read == -1) {
                                        break
                                    }
                                    outputStream!!.write(buffer, 0, read)
                                    downloaded += read
                                    val newProgress = downloaded * 100 / size
                                    if (newProgress > progress) {
                                        progress = newProgress
                                        HistoryStore.getInstance(ctx).addOrUpdateDownload(
                                                targetFile.absolutePath,
                                                data,
                                                progress.toInt()
                                        )
                                        ctx.sendBroadcast(Intent(MusicService.MEDIA_STORE_CHANGED))
                                    }
                                }
                                outputStream!!.flush()
                            }
                        }
                        Constants.DOWNLOAD_INPROGRESS_QUEUE.remove(data.getSongId().toString())
                        HistoryStore.getInstance(ctx).addOrUpdateDownload(targetFile.absolutePath, data, 100)
                        Handler(Looper.getMainLooper()).post() {
                            RewardManager.addDownloadCount(ctx)
                        }
                        MusicUtil.refreshMediaGallery(ctx, targetFile.absolutePath)
                        Log.d(TAG, "${Constants.STATS_MUSIC_DOWNLOAD}_${Constants.STATS_SUCCESS}")
                        EventLog.log("${Constants.STATS_MUSIC_DOWNLOAD}_${Constants.STATS_SUCCESS}")
                        return "${ctx.getString(R.string.saved)}: ${data.getSongTitle()}"
                    }
                } catch (exception: IOException) {
                    HistoryStore.getInstance(ctx).addOrUpdateDownload(targetFile.absolutePath, data, -2)
                    Log.d(TAG, "${Constants.STATS_MUSIC_DOWNLOAD}_${Constants.STATS_FAIL}_${exception.message}")
                    EventLog.log("${Constants.STATS_MUSIC_DOWNLOAD}_${Constants.STATS_FAIL}_${exception.message}")
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                    Constants.DOWNLOAD_INPROGRESS_QUEUE.remove(data.getSongId().toString())
                    ctx.sendBroadcast(Intent(MusicService.MEDIA_STORE_CHANGED))
                }
                return "${ctx.getString(R.string.save_failed)}: ${data.getSongTitle()}"
            }
            return ""
        }

        override fun onPostExecute(string: String) {
            super.onPostExecute(string)
            val context = context
            if (context != null && string.isNotBlank()) {
                Toast.makeText(context, string, Toast.LENGTH_LONG).show()
            }
        }
    }

    abstract class OnClickSongMenu protected constructor(private val activity: AppCompatActivity) :
            View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        open val menuRes: Int
            get() = MENU_RES

        abstract val song: CommonData

        override fun onClick(v: View) {
            val popupMenu = PopupMenu(activity, v)
            popupMenu.inflate(menuRes)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.show()
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            return handleMenuClick(activity, song, item.itemId)
        }
    }
}
