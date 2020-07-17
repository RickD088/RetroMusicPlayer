package code.name.monkey.retromusic.adapter.song

import androidx.appcompat.app.AppCompatActivity
import code.name.monkey.retromusic.interfaces.CabHolder
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.util.MusicUtil
import java.util.*

class SimpleSongAdapter(
    context: AppCompatActivity,
    songs: ArrayList<CommonData>,
    layoutRes: Int,
    cabHolder: CabHolder?
) : SongAdapter(context, songs, layoutRes, cabHolder) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (curType != CommonData.TYPE_NATIVE_ADS) {
            val data = dataSet[position]
            if (data.localSong()) {
                val fixedTrackNumber = MusicUtil.getFixedTrackNumber(data.getLocalSong().trackNumber)
                holder.imageText?.text = if (fixedTrackNumber > 0) fixedTrackNumber.toString() else "-"
                holder.time?.text = MusicUtil.getReadableDurationString(data.getLocalSong().duration)
            } else if (data.cloudSong()) {
                holder.time?.text =
                    MusicUtil.getReadableDurationString(data.getCloudSong().getDuration())
                holder.imageText?.text = "-"
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}
