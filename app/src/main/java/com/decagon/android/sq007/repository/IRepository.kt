package com.decagon.android.sq007.repository

import com.decagon.android.sq007.model.Comment
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.ui.State.MainState
import com.decagon.android.sq007.ui.State.Resource
import kotlinx.coroutines.flow.Flow

interface IRepository {

    suspend fun getPosts(): Flow<MainState>

    suspend fun getComments(postId: Int): Flow<MainState>

    suspend fun getAllComments(): Flow<MainState>

    suspend fun pushComment(comment: Comment) : Flow<MainState>

    suspend fun addPost(post: Post): Flow<MainState>
    suspend fun search(query: String): Flow<MainState>
}