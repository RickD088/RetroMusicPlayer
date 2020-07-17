package code.name.monkey.retromusic.model

import code.name.monkey.retromusic.rest.music.model.Prize

data class TaskData(
        val prize: Prize,
        val target: Int,
        var current: Int,
        var claimed: Boolean
)