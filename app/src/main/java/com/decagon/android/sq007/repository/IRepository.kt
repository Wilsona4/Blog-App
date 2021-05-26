package com.decagon.android.sq007.repository

import com.decagon.android.sq007.model.Comment
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.util.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface IRepository {

    suspend fun getPosts(): Flow<Resource<List<Post>>>

    suspend fun getComments(postId: Int): Flow<Resource<List<Comment>>>

    suspend fun getAllComments(): Flow<Resource<List<Comment>>>

    suspend fun pushComment(comment: Comment): Flow<Resource<Comment>>
}