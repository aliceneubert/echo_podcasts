package com.zacneubert.echo.podcast_list

import com.zacneubert.echo.models.Episode

interface EpisodeSelectedListener {
    fun onEpisodeSelected(episode: Episode)
}
