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

package code.name.monkey.retromusic.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.activities.saf.SAFGuideActivity
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.util.MusicUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.SAFUtil
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet

class DeleteSongsDialog : DialogFragment() {
    @JvmField
    var currentSong: CommonData? = null
    @JvmField
    var songsToRemove: List<CommonData>? = null

    private var deleteSongsAsyncTask: DeleteSongsAsyncTask? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songs: ArrayList<CommonData>? = requireArguments().getParcelableArrayList("songs")
        var title = 0
        var content: CharSequence = ""
        if (songs != null) {
            if (songs.size > 1) {
                title = R.string.delete_songs_title
                content = HtmlCompat.fromHtml(
                    getString(R.string.delete_x_songs, songs.size),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            } else {
                title = R.string.delete_song_title
                content = HtmlCompat.fromHtml(
                    getString(R.string.delete_song_x, songs[0].getSongTitle()),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }

        return MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(title)
            message(text = content)
            negativeButton(android.R.string.cancel) {
                dismissDialog()
            }
            cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
            noAutoDismiss()
            positiveButton(R.string.action_delete) {
                if (songs != null) {
                    if ((songs.size == 1) && MusicPlayerRemote.isPlaying(songs[0])) {
                        MusicPlayerRemote.playNextSong(activity)
                    }
                }
                songsToRemove = songs
                deleteSongsAsyncTask = DeleteSongsAsyncTask(this@DeleteSongsDialog)
                deleteSongsAsyncTask?.execute(DeleteSongsAsyncTask.LoadingInfo(songs, null))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SAFGuideActivity.REQUEST_CODE_SAF_GUIDE -> {
                SAFUtil.openTreePicker(this)
            }
            SAFUtil.REQUEST_SAF_PICK_TREE,
            SAFUtil.REQUEST_SAF_PICK_FILE -> {
                if (deleteSongsAsyncTask != null) {
                    deleteSongsAsyncTask?.cancel(true)
                }
                deleteSongsAsyncTask = DeleteSongsAsyncTask(this)
                deleteSongsAsyncTask?.execute(
                    DeleteSongsAsyncTask.LoadingInfo(
                        requestCode,
                        resultCode,
                        data
                    )
                )
            }
        }
    }

    fun deleteSongs(songs: List<CommonData>, safUris: List<Uri>?) {
        MusicUtil.deleteTracks(requireActivity(), songs, safUris) { this.dismiss() }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (ignore: IllegalStateException) {
            ignore.printStackTrace()
        }
    }

    private fun dismissDialog() {
        if (activity != null && !requireActivity().isFinishing) {
            dismissAllowingStateLoss()
        }
    }

    companion object {

        fun create(song: CommonData): DeleteSongsDialog {
            val list = ArrayList<CommonData>()
            list.add(song)
            return create(list)
        }

        fun create(songs: List<CommonData>): DeleteSongsDialog {
            val dialog = DeleteSongsDialog()
            val args = Bundle()
            args.putParcelableArrayList("songs", ArrayList(songs))
            dialog.arguments = args
            return dialog
        }
    }
}

