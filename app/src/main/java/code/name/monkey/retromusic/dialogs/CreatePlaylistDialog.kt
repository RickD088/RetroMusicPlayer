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
import android.provider.MediaStore
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import code.name.monkey.appthemehelper.util.MaterialUtil
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.R.layout
import code.name.monkey.retromusic.R.string
import code.name.monkey.retromusic.extensions.appHandleColor
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.service.MusicService
import code.name.monkey.retromusic.util.PlaylistsUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CreatePlaylistDialog : DialogFragment() {

    private lateinit var playlistView: TextInputEditText
    private lateinit var actionNewPlaylistContainer: TextInputLayout

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        val materialDialog = MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
            .show {
                cornerRadius(PreferenceUtil.getInstance(requireContext()).dialogCorner)
                title(string.new_playlist_title)
                customView(layout.dialog_playlist)
                negativeButton(android.R.string.cancel)
                positiveButton(string.create_action) {
                    if (activity == null) {
                        return@positiveButton
                    }
                    val songs = requireArguments().getParcelableArrayList<CommonData>("songs")
                        ?: return@positiveButton

                    if (playlistView.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                        val playlistId = PlaylistsUtil.createPlaylist(
                            requireContext(),
                            playlistView.text.toString()
                        )
                        if (playlistId != -1L) {
                            PlaylistsUtil.addToPlaylist(requireContext(), songs, playlistId, true)
                        }
                        context.sendBroadcast(Intent(MusicService.MEDIA_STORE_CHANGED))
                    }
                }
            }

        val dialogView = materialDialog.getCustomView()
        playlistView = dialogView.findViewById(R.id.actionNewPlaylist)
        actionNewPlaylistContainer = dialogView.findViewById(R.id.actionNewPlaylistContainer)

        MaterialUtil.setTint(actionNewPlaylistContainer, false)

        val playlistId = requireArguments().getLong(MediaStore.Audio.Playlists.Members.PLAYLIST_ID)
        playlistView.appHandleColor()
            .setText(
                PlaylistsUtil.getNameForPlaylist(requireContext(), playlistId),
                TextView.BufferType.EDITABLE
            )
        return materialDialog
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (ignore: IllegalStateException) {
            ignore.printStackTrace()
        }
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun create(song: CommonData? = null): CreatePlaylistDialog {
            val list = ArrayList<CommonData>()
            if (song != null) {
                list.add(song)
            }
            return create(list)
        }

        @JvmStatic
        fun create(songs: ArrayList<CommonData>): CreatePlaylistDialog {
            val dialog = CreatePlaylistDialog()
            val args = Bundle()
            args.putParcelableArrayList("songs", songs)
            dialog.arguments = args
            return dialog
        }
    }
}