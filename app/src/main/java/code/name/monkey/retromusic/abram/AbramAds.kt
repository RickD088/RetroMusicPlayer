/*
 * Copyright (c) 2020 Free Music Team.
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

package code.name.monkey.retromusic.abram

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.helper.menu.SongMenuHelper
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.util.FileUtil
import code.name.monkey.retromusic.util.MusicUtil
import code.name.monkey.retromusic.util.ViewUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton


object AbramAds {

    private val TAG = javaClass.simpleName

    fun showDialogBeforeOpenMusic(activity: Activity, song: CommonData, onClickListener: View.OnClickListener) {
        // 初始化对话框
        val dialog = Dialog(activity)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = ViewUtil.convertDpToPixel(300f, activity.resources).toInt() // context.resources.displayMetrics.widthPixels * 4 / 5
        lp.height = ViewUtil.convertDpToPixel(400f, activity.resources).toInt() // context.resources.displayMetrics.heightPixels / 2

        // 设置内部样式
        val v: View = LayoutInflater.from(activity).inflate(R.layout.dialog_ads_alert, null)
        // 广告容器
        val cover = v.findViewById<View>(R.id.cover) as ImageView
        val img = when {
            song.localSong() -> {
                MusicUtil.getMediaStoreAlbumCoverUri(song.getLocalSong().albumId)
            }
            song.cloudSong() -> {
                song.getCloudSong().artworkBigUrl
            }
            else -> {
                ""
            }
        }
        img?.let {
            Glide.with(activity).load(it).asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false).into(cover)
        }
        cover.visibility = View.VISIBLE
        val ads = v.findViewById<View>(R.id.ads) as ConstraintLayout
        // 歌曲标题
        val title = v.findViewById<View>(R.id.title) as TextView
        title.text = "${song.getSongTitle()} - ${song.getSongSinger()}"
        // 播放按钮
        val play = v.findViewById<View>(R.id.play)
        play.setOnClickListener {
            onClickListener.onClick(it)
            dialog.dismiss()
        }
        // 下载按钮
        val download = v.findViewById<View>(R.id.download) as MaterialButton
        if (song.cloudSong()) {
            val inDownloadingQueue = Constants.DOWNLOAD_INPROGRESS_QUEUE.contains(song.getSongId().toString())
            val inDownloadList = FileUtil.isMusicInDownloadList(activity, song.getSongId())
            if (inDownloadList) {
                val downloadProgress = FileUtil.getMusicDownloadProgress(activity, song.getSongId(), song.getCloudSong().cache)
                if (downloadProgress > Constants.DOWNLOAD_ASSUMED_COMPLETION_PERCENTAGE) {
                    // 已下载成功
                    download.icon = activity.getDrawable(R.drawable.ic_offline_white_24dp)
                    download.text = activity.getString(R.string.saved)
                    download.isEnabled = false
                } else {
                    // 已部分下载（可继续下载）
                    if (inDownloadingQueue) {
                        download.text = if (downloadProgress > 0) {
                            "${activity.getString(R.string.downloading)} ( $downloadProgress% )"
                        } else {
                            activity.getString(R.string.waiting_to_download)
                        }
                        download.isEnabled = false
                    } else {
                        download.text = activity.getString(R.string.continue_downloading)
                        download.setOnClickListener {
                            SongMenuHelper.downloadSong(activity, song)
                            dialog.dismiss()
                        }
                    }
                }
            } else {
                val cached = FileUtil.isMusicCached(song.getCloudSong().cache)
                if (cached) {
                    // 已缓存（可下载）
                    download.icon = activity.getDrawable(R.drawable.ic_offline_white_24dp)
                    download.text = "${activity.getString(R.string.download)} ( ${activity.getString(R.string.cached)} )"
                }
                download.setOnClickListener {
                    SongMenuHelper.downloadSong(activity, song)
                    dialog.dismiss()
                }
            }
        } else {
            download.icon = activity.getDrawable(R.drawable.ic_offline_white_24dp)
            download.text = activity.getString(R.string.saved)
            download.isEnabled = false
        }
        dialog.setContentView(v, lp)
        dialog.show()
    }
}
