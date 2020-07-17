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

package code.name.monkey.retromusic.service;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSink;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.abram.Constants;
import code.name.monkey.retromusic.abram.EventLog;
import code.name.monkey.retromusic.abram.RemoteConfig;
import code.name.monkey.retromusic.service.playback.Playback;
import code.name.monkey.retromusic.util.RewardManager;

/**
 * @author Andrew Neal, Karim Abou Zeid (kabouzeid)
 */
public class MultiPlayer implements Playback, Player.EventListener {
    public static final String TAG = MultiPlayer.class.getSimpleName();

    private SimpleExoPlayer mCurrentMediaPlayer;
    private SimpleExoPlayer mNextMediaPlayer;

    private Context context;
    @Nullable
    private Playback.PlaybackCallbacks callbacks;

    private boolean mIsInitialized = false;

    private long playingTime = 0;

    private boolean seek = false;

    /**
     * Constructor of <code>MultiPlayer</code>
     */
    MultiPlayer(final Context context) {
        this.context = context;
        mCurrentMediaPlayer = ExoPlayerFactory.newSimpleInstance(context);
    }

    /**
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     * @return True if the <code>player</code> has been prepared and is
     * ready to play, false otherwise
     */
    @Override
    public boolean setDataSource(@NonNull final String path) {
        mIsInitialized = false;
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
        Log.d(TAG, Constants.STATS_MUSIC_PLAY);
        EventLog.INSTANCE.log(Constants.STATS_MUSIC_PLAY);
        if (mIsInitialized) {
            setNextDataSource(null);
        }
        return mIsInitialized;
    }

    /**
     * @param player The {@link MediaPlayer} to use
     * @param path   The path of the file, or the http/rtsp URL of the stream
     *               you want to play
     * @return True if the <code>player</code> has been prepared and is
     * ready to play, false otherwise
     */
    private boolean setDataSourceImpl(@NonNull final ExoPlayer player, @NonNull final String path) {
        if (context == null) {
            return false;
        }
        try {
            player.stop(true);
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, Constants.APPLICATION_ID));
            DataSink.Factory cacheWriteDataSinkFactory = new CacheDataSinkFactory(Constants.INSTANCE.getPLAY_MUSIC_CACHE(), Long.MAX_VALUE);
            CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(Constants.INSTANCE.getPLAY_MUSIC_CACHE(),
                    dataSourceFactory, new FileDataSourceFactory(), cacheWriteDataSinkFactory,
                    CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);

            MediaSource source = new ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(Uri.parse(path));
            player.prepare(source);
        } catch (Exception e) {
            return false;
        }
        player.addListener(this);
        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
        context.sendBroadcast(intent);
        return true;
    }

    /**
     * Set the MediaPlayer to start when this MediaPlayer finishes playback.
     *
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     */
    @Override
    public void setNextDataSource(@Nullable final String path) {
        if (context == null) {
            return;
        }
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
        // FIXME: 此功能有严重 BUG，暂时屏蔽
//        if (path == null) {
//            return;
//        }
//        if (PreferenceUtil.getInstance(context).gaplessPlayback()) {
//            mNextMediaPlayer = ExoPlayerFactory.newSimpleInstance(context);
//            if (!setDataSourceImpl(mNextMediaPlayer, path)) {
//                if (mNextMediaPlayer != null) {
//                    mNextMediaPlayer.release();
//                    mNextMediaPlayer = null;
//                }
//            }
//        }
    }

    /**
     * Sets the callbacks
     *
     * @param callbacks The callbacks to use
     */
    @Override
    public void setCallbacks(@Nullable final Playback.PlaybackCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * @return True if the player is ready to go, false otherwise
     */
    @Override
    public boolean isInitialized() {
        return mIsInitialized;
    }

    /**
     * Starts or resumes playback.
     */
    @Override
    public boolean start() {
        try {
            mCurrentMediaPlayer.setPlayWhenReady(true);
            Log.d(TAG, Constants.STATS_MUSIC_START);
            EventLog.INSTANCE.log(Constants.STATS_MUSIC_START);
            if (context != null) context.sendBroadcast(new Intent(MusicService.ACTION_PLAY));
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Resets the MediaPlayer to its uninitialized state.
     */
    @Override
    public void stop() {
        mCurrentMediaPlayer.stop(true);
        mIsInitialized = false;
    }

    /**
     * Releases resources associated with this MediaPlayer object.
     */
    @Override
    public void release() {
        stop();
        mCurrentMediaPlayer.release();
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
        }
    }

    /**
     * Pauses playback. Call start() to resume.
     */
    @Override
    public boolean pause() {
        try {
            mCurrentMediaPlayer.setPlayWhenReady(false);
            Log.d(TAG, Constants.STATS_MUSIC_PAUSE);
            EventLog.INSTANCE.log(Constants.STATS_MUSIC_PAUSE);
            if (context != null) context.sendBroadcast(new Intent(MusicService.ACTION_PAUSE));
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Checks whether the MultiPlayer is playing.
     */
    @Override
    public boolean isPlaying() {
        return mIsInitialized && mCurrentMediaPlayer.getPlayWhenReady();
    }

    /**
     * Gets the duration of the file.
     *
     * @return The duration in milliseconds
     */
    @Override
    public int duration() {
        if (!mIsInitialized) {
            return -1;
        }
        try {
            return (int) mCurrentMediaPlayer.getDuration();
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    /**
     * Gets the current playback position.
     *
     * @return The current position in milliseconds
     */
    @Override
    public int position() {
        if (!mIsInitialized) {
            return -1;
        }
        try {
            return (int) mCurrentMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    /**
     * Gets the current playback position.
     *
     * @param whereto The offset in milliseconds from the start to seek to
     * @return The offset in milliseconds from the start to seek to
     */
    @Override
    public int seek(final int whereto) {
        try {
            seek = true;
            mCurrentMediaPlayer.seekTo(whereto);
            Log.d(TAG, Constants.STATS_MUSIC_SEEK);
            EventLog.INSTANCE.log(Constants.STATS_MUSIC_SEEK);
            return whereto;
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    @Override
    public boolean setVolume(final float vol) {
        try {
            mCurrentMediaPlayer.setVolume(vol);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId The audio session ID
     */
    @Override
    public boolean setAudioSessionId(final int sessionId) {
        try {
//            mCurrentMediaPlayer.setAudioSessionId(sessionId);
            return true;
        } catch (@NonNull IllegalArgumentException | IllegalStateException e) {
            return false;
        }
    }

    /**
     * Returns the audio session ID.
     *
     * @return The current audio session ID.
     */
    @Override
    public int getAudioSessionId() {
        return mCurrentMediaPlayer.getAudioSessionId();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        mIsInitialized = false;
        mCurrentMediaPlayer.release();
        mCurrentMediaPlayer = ExoPlayerFactory.newSimpleInstance(context);
        if (context != null) {
            context.sendBroadcast(new Intent(MusicService.ACTION_ERROR));
            Toast.makeText(context, context.getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, Constants.STATS_MUSIC_ERROR + error.getMessage());
        EventLog.INSTANCE.log(Constants.STATS_MUSIC_ERROR + error.getMessage());
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playWhenReady) {
            if (playbackState == Player.STATE_READY) {
                playingTime = System.currentTimeMillis();
                RewardManager.INSTANCE.setPlayingState(context, playingTime, true);
            }
        } else {
            if (playingTime > 0) {
                RewardManager.INSTANCE.setPlayingState(context, playingTime, false);
                playingTime = 0;
            }
        }
        switch (playbackState) {
            case Player.STATE_ENDED:
                if (playWhenReady) {
                    if (!seek) {
                        RemoteConfig.INSTANCE.incFullPlayedCount();
                        RewardManager.INSTANCE.addPlayCount(context);
                    }
                    seek = false;
                    if (mNextMediaPlayer != null) {
                        mIsInitialized = false;
                        mCurrentMediaPlayer.release();
                        mCurrentMediaPlayer = mNextMediaPlayer;
                        mIsInitialized = true;
                        mNextMediaPlayer = null;
                        start();
                        if (callbacks != null)
                            callbacks.onTrackWentToNext();
                    } else {
                        if (callbacks != null)
                            callbacks.onTrackEnded();
                    }
                }
                break;
        }
    }

}