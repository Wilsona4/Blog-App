package com.decagon.android.sq007.remote

import com.decagon.android.sq007.model.Comment
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.room.PostEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PostApi {

    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("comments")
    suspend fun getComments(
        @Query("postId") postId: Int,
    ): List<Comment>

    @GET("comments")
    suspend fun getAllComments(): List<Comment>

    @POST("comments")
    suspend fun pushComment(
        @Body comment: Comment
    ) : Comment
}