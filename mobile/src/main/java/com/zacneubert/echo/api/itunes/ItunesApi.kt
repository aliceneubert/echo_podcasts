package com.zacneubert.echo.api.itunes

import android.content.Context
import com.zacneubert.echo.api.callback.HandledCallback
import com.zacneubert.echo.api.itunes.models.SearchResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search")
    fun search(@Query("term") term: String, @Query("entity") entity: String): Call<SearchResult>

    class Meta {
        companion object {
            fun searchPodcasts(api: ItunesApi, context: Context, term: String, callback: HandledCallback<SearchResult>) {
                api.search(term, "podcast").enqueue(callback)
            }
        }
    }
}
