package com.decagon.android.sq007.repository

import android.util.Log
import com.decagon.android.sq007.model.Comment
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.remote.RetrofitInstance
import com.decagon.android.sq007.room.CachedCommentMapper
import com.decagon.android.sq007.room.CachedPostMapper
import com.decagon.android.sq007.room.LocalDataBase
import com.decagon.android.sq007.ui.State.MainState
import com.decagon.android.sq007.ui.State.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository(
    private val db: LocalDataBase,
    private val cachedCommentMapper: CachedCommentMapper,
    private val cachedPostMapper: CachedPostMapper,
) : IRepository {

    /*Get All Posts*/
    override suspend fun getPosts(): Flow<MainState> = flow {
        emit(MainState.Loading)
        try {
            /*Retrieve Remote Posts*/
            val remotePosts = RetrofitInstance.postApi.getPosts()
            /*Map posts to Local Database*/
            for (post in remotePosts) {
                db.userDao().addPost(cachedPostMapper.mapToEntity(post))
            }
            /*Retrieve Posts Local DataBAse*/
            val cachedPosts = db.userDao().readAllPost()
            emit(MainState.EntirePost(cachedPostMapper.mapFromEntityList(cachedPosts)))
        } catch (e: Exception) {
            emit(MainState.Error(e))
        }
    }

    /*Get Specific Comments*/
    override suspend fun getComments(postId: Int): Flow<MainState> = flow {
        emit(MainState.Loading)
        try {
            /*Retrieve Posts Local DataBAse*/
            val cachedComments = db.userDao().readComments(postId)
            emit(MainState.Comments(cachedCommentMapper.mapFromEntityList(cachedComments)))
        } catch (e: Exception) {
//            error exception
            emit(MainState.Error(e))
        }
    }

    /*Get All Comments*/
    override suspend fun getAllComments(): Flow<MainState> = flow {
        emit(MainState.Loading)
        try {
            /*Retrieve Remote Comments*/
            val remoteComments = RetrofitInstance.postApi.getAllComments()
            /*Map posts to Local Database*/
            for (comment in remoteComments) {
                db.userDao().addComment(cachedCommentMapper.mapToEntity(comment))
            }
            /*Retrieve Posts Local DataBAse*/
            val cachedComments = db.userDao().readAllComments()
            emit(MainState.EntireComment(cachedCommentMapper.mapFromEntityList(cachedComments)))
        } catch (e: Exception) {
            emit(MainState.Error(e))
        }
    }

    /*Add Comment*/
    override suspend fun pushComment(comment: Comment): Flow<MainState> = flow {
        emit(MainState.Loading)
        try {
            /*Add Comment to Local Database*/
            db.userDao().addComment(cachedCommentMapper.mapToEntity(comment))

            /*Retrieve Posts Local DataBase*/
            val cachedComments = db.userDao().readComments(comment.postId)
            emit(MainState.Comments(cachedCommentMapper.mapFromEntityList(cachedComments)))
        } catch (e: Exception) {
            Log.d("COM", "pushComment: ${e.message}")
        }
    }

    override suspend fun addPost(post: Post): Flow<MainState> = flow {
        emit(MainState.Loading)
        try {
            /*Add Comment to Local Database*/
            db.userDao().addPost(cachedPostMapper.mapToEntity(post))
            /*Retrieve Posts Local DataBase*/
            val cachedPosts = db.userDao().readAllPost()
            emit(MainState.EntirePost(cachedPostMapper.mapFromEntityList(cachedPosts)))
        } catch (e: Exception) {
            Log.d("Post", "Add Post: ${e.message}")
        }
    }


}