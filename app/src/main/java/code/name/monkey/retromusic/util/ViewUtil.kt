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

package code.name.monkey.retromusic.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.core.animation.doOnEnd
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.view.ViewCompat
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.abram.Constants
import com.google.android.material.slider.Slider
import java.util.*

object ViewUtil {

    const val RETRO_MUSIC_ANIM_TIME = 1000

    fun setProgressDrawable(progressSlider: SeekBar, newColor: Int, thumbTint: Boolean = false) {

        if (thumbTint) {
            progressSlider.thumbTintList = ColorStateList.valueOf(newColor)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val layerDrawable = progressSlider.progressDrawable as LayerDrawable
            val progressDrawable = layerDrawable.findDrawableByLayerId(android.R.id.progress)
            progressDrawable.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(newColor, SRC_IN)
        } else {
            progressSlider.progressTintList = ColorStateList.valueOf(newColor)
        }
    }

    fun setProgressDrawable(progressSlider: Slider, color: Int, thumbTint: Boolean = false) {
        if (thumbTint) {
            progressSlider.thumbColor = ColorStateList.valueOf(color)
        }
        val colorWithAlpha = ColorUtil.withAlpha(color, 0.25f)
        progressSlider.haloColor = ColorStateList.valueOf(colorWithAlpha)
        progressSlider.trackColorActive = ColorStateList.valueOf(color)
        progressSlider.trackColorInactive = ColorStateList.valueOf(colorWithAlpha)
    }

    fun setProgressDrawable(progressSlider: ProgressBar, newColor: Int) {

        val layerDrawable = progressSlider.progressDrawable as LayerDrawable

        val progress = layerDrawable.findDrawableByLayerId(android.R.id.progress)
        progress.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(newColor, SRC_IN)

        val background = layerDrawable.findDrawableByLayerId(android.R.id.background)
        val primaryColor =
                ATHUtil.resolveColor(progressSlider.context, android.R.attr.windowBackground)
        background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                MaterialValueHelper.getPrimaryDisabledTextColor(
                        progressSlider.context,
                        ColorUtil.isColorLight(primaryColor)
                ), SRC_IN
        )

        val secondaryProgress = layerDrawable.findDrawableByLayerId(android.R.id.secondaryProgress)
        secondaryProgress?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        ColorUtil.withAlpha(
                                newColor,
                                0.65f
                        ), SRC_IN
                )
    }

    fun hitTest(v: View, x: Int, y: Int): Boolean {
        val tx = (ViewCompat.getTranslationX(v) + 0.5f).toInt()
        val ty = (ViewCompat.getTranslationY(v) + 0.5f).toInt()
        val left = v.left + tx
        val right = v.right + tx
        val top = v.top + ty
        val bottom = v.bottom + ty

        return x in left..right && y >= top && y <= bottom
    }

    fun convertDpToPixel(dp: Float, resources: Resources): Float {
        val metrics = resources.displayMetrics
        return dp * metrics.density
    }

    fun getDurationHourString(songDurationMillis: Long): String? {
        var minutes = songDurationMillis / 1000 / 60
        val hours = minutes / 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes % 60)

    }

    fun startFadingIn(
            view: View?,
            durationMs: Long = Constants.ONE_SECOND_MS,
            finalVisibility: Int = View.VISIBLE
    ) {
        view?.let {
            it.clearAnimation()
            if (it.visibility != View.VISIBLE) {
                val alphaAnimation = AlphaAnimation(0f, 1f)
                alphaAnimation.duration = durationMs
                alphaAnimation.interpolator = LinearInterpolator()
                alphaAnimation.repeatCount = 0
                it.startAnimation(alphaAnimation)
            }
            it.visibility = finalVisibility
        }
    }

    fun startFadingOut(
            view: View?,
            durationMs: Long = Constants.ONE_SECOND_MS,
            finalVisibility: Int = View.GONE
    ) {
        view?.let {
            it.clearAnimation()
            if (it.visibility == View.VISIBLE) {
                val alphaAnimation = AlphaAnimation(1f, 0f)
                alphaAnimation.duration = durationMs
                alphaAnimation.interpolator = LinearInterpolator()
                alphaAnimation.repeatCount = 0
                it.startAnimation(alphaAnimation)
            }
            it.visibility = finalVisibility
        }
    }

    fun floatAsBubble(
        view: View?,
        up: Float = -10f,
        down: Float = 10f,
        duration: Long = Constants.ONE_SECOND_MS
    ) {
        view?.let {
            it.clearAnimation()
            val animator = ObjectAnimator.ofFloat(
                it,
                "translationY", up, down
            )
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
            animator.duration = duration
            animator.start()
        }
    }

    fun startRewardAnimate(imageView: View, bgView: View?, btnView: View?) {
        val xAnimator = ObjectAnimator.ofFloat(imageView, "scaleX", 5f, 1f)
        xAnimator.interpolator = BounceInterpolator()
        val yAnimator = ObjectAnimator.ofFloat(imageView, "scaleY", 5f, 1f)
        yAnimator.interpolator = BounceInterpolator()
        val rotationAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f)
        rotationAnimator.interpolator = DecelerateInterpolator()
        val animatorSet = AnimatorSet().apply {
            doOnEnd {
                bgView?.let {
                    it.visibility = View.VISIBLE
                    val animator = ObjectAnimator.ofFloat(it, "rotation", 0f, 360f)
                    animator.repeatCount = ValueAnimator.INFINITE
                    animator.repeatMode = ValueAnimator.RESTART
                    animator.duration = 1000
                    animator.interpolator = LinearInterpolator()
                    animator.start()
                }

                btnView?.postDelayed({
                    btnView?.let {
                        it.visibility = View.VISIBLE
                        val translationAnimator = ObjectAnimator.ofFloat(it, "translationY", it.context.resources.getDimension(R.dimen.btn_margin), 0f)
                        translationAnimator.interpolator = BounceInterpolator()
                        translationAnimator.duration = 500
                        translationAnimator.start()
                    }
                }, 500)
            }
        }
        animatorSet.duration = 800
        animatorSet.playTogether(xAnimator, yAnimator, rotationAnimator)
        animatorSet.start()
    }
}