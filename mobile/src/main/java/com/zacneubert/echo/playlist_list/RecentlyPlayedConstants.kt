package com.zacneubert.echo.playlist_list

import android.app.Activity
import android.app.Service
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Playlist
import com.zacneubert.echo.models.PlaylistEpisode
import com.zacneubert.echo.models.Playlist_
import io.objectbox.Box

val RECENTLY_PLAYED_PLAYLIST_NAME = "Recently Played"
val RECENTLY_PLAYED_PLAYLIST_DESCRIPTION = "Most recently played episodes"
val RECENTLY_PLAYED_PLAYLIST_MAX_SIZE = 30

fun getRecentlyPlayedPlaylist(activity: Activity) : Playlist {
    return getRecentlyPlayedPlaylist(EchoApplication.instance(activity).playlistBox())
}

fun getRecentlyPlayedPlaylist(service: Service) : Playlist {
    return getRecentlyPlayedPlaylist(EchoApplication.instance(service).playlistBox())
}

fun getRecentlyPlayedPlaylist(playlistBox: Box<Playlist>) : Playlist {
    var recentlyPlayedPlaylist: Playlist? = playlistBox.query().equal(Playlist_.title, RECENTLY_PLAYED_PLAYLIST_NAME).build().findFirst()
    if(recentlyPlayedPlaylist == null) {
        recentlyPlayedPlaylist = Playlist()
        recentlyPlayedPlaylist.title = RECENTLY_PLAYED_PLAYLIST_NAME
        recentlyPlayedPlaylist.description = RECENTLY_PLAYED_PLAYLIST_NAME
        playlistBox.put(recentlyPlayedPlaylist)
    }
    return recentlyPlayedPlaylist
}

fun addToRecentlyPlayed(activity: Activity, episode: Episode) {
    addToRecentlyPlayed(EchoApplication.instance(activity), episode)
}

fun addToRecentlyPlayed(service: Service, episode: Episode) {
    addToRecentlyPlayed(EchoApplication.instance(service), episode)
}

fun addToRecentlyPlayed(echoApplication: EchoApplication, episode: Episode) {
    val recentlyPlayed = getRecentlyPlayedPlaylist(echoApplication.playlistBox())

    val playlistEpisode = PlaylistEpisode()
    playlistEpisode.episode.target = episode
    playlistEpisode.position = (-1 * recentlyPlayed.playlistEpisodes.count()).toLong()
    playlistEpisode.playlist.target = recentlyPlayed
    recentlyPlayed.playlistEpisodes.add(playlistEpisode)

    echoApplication.playlistBox().put(recentlyPlayed)
}
