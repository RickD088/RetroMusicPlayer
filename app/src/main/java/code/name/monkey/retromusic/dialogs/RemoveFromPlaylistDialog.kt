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
import android.os.Bundle
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.R.string
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.service.MusicService
import code.name.monkey.retromusic.util.PlaylistsUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet

class RemoveFromPlaylistDialog : DialogFragment() {

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (ignore: IllegalStateException) {
            ignore.printStackTrace()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songs = requireArguments().getParcelableArrayList<CommonData>("songs")
        val playlistId = requireArguments().getLong("id")
        var title = 0
        var content: CharSequence = ""
        if (songs != null) {
            if (songs.size > 1) {
                title = R.string.remove_songs_from_playlist_title
                content = HtmlCompat.fromHtml(
                    getString(string.remove_x_songs_from_playlist, songs.size),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            } else {
                title = R.string.remove_song_from_playlist_title
                content = HtmlCompat.fromHtml(
                    getString(
                        code.name.monkey.retromusic.R.string.remove_song_x_from_playlist,
                        songs[0].getSongTitle()
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
        }


        return MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
            .show {
                title(title)
                message(text = content)
                negativeButton(android.R.string.cancel)
                positiveButton(R.string.remove_action) {
                    if (activity == null)
                        return@positiveButton
                    PlaylistsUtil.removeSongFromPlaylist(
                        requireContext(),
                        playlistId,
                        songs as MutableList<CommonData>
                    )
                    context.sendBroadcast(Intent(MusicService.MEDIA_STORE_CHANGED))
                }
                cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
            }
    }

    companion object {

        fun create(playlistId: Long, song: CommonData): RemoveFromPlaylistDialog {
            val list = ArrayList<CommonData>()
            list.add(song)
            return create(playlistId, list)
        }

        fun create(playlistId: Long, songs: ArrayList<CommonData>): RemoveFromPlaylistDialog {
            val dialog = RemoveFromPlaylistDialog()
            val args = Bundle()
            args.putParcelableArrayList("songs", songs)
            args.putLong("id", playlistId)
            dialog.arguments = args
            return dialog
        }
    }
}