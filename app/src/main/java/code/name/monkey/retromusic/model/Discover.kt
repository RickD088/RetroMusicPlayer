package code.name.monkey.retromusic.model

import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import code.name.monkey.retromusic.adapter.HomeAdapter

class Discover(
    val priority: Int,
    @StringRes
    val title: Int,
    val arrayList: ArrayList<*>,
    @DiscoverSection
    val homeSection: Int,
    @DrawableRes
    val icon: Int
) {

    companion object {
        @IntDef(CHARTS, GENERAL_PLAYLISTS, GENRE, TOP_ARTISTS, PLAYLISTS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class DiscoverSection

        const val CHARTS = 0
        const val GENERAL_PLAYLISTS = 1
        const val GENRE = 2
        const val TOP_ARTISTS = 3
        const val PLAYLISTS = 4
    }
}