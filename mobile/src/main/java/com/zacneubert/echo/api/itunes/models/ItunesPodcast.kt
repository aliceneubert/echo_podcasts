package com.zacneubert.echo.api.itunes.models

import java.util.*

class ItunesPodcast (
    val kind : String,
    val collectionPrice : Float,
    val trackCount : Int,
    val trackHdPrice : Int,
    val artistName : String,
    val feedUrl : String,
    val primaryGenreName : String,
    val trackId : Int,
    val trackCensoredName : String,
    val trackName : String,
    val collectionCensoredName : String,
    val artworkUrl100 : String,
    val trackRentalPrice : Int,
    val artworkUrl60 : String,
    val artworkUrl30 : String,
    val collectionName : String,
    val wrapperType : String,
    val collectionId : Int,
    val trackPrice : Float,
    val country : String,
    val artworkUrl600 : String,
    val collectionViewUrl : String,
    val trackViewUrl : String,
    val releaseDate : Date,
    val contentAdvisoryRating : String,
    val currency : String,
    val genres : Array<String>,
    val collectionExplicitness : String,
    val trackExplicitness : String,
    val collectionHdPrice : Int,
    val trackHdRentalPrice : Int,
    val genreIds : Array<String>
)