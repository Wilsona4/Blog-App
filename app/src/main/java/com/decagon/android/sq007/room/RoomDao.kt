package com.decagon.android.sq007.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPost(post: List<PostEntity>)

    /*Add Post to Database*/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPost(post: PostEntity)

    /*Read all Posts in the Database*/
    @Query("SELECT * FROM post_table")
    fun readAllPost(): Flow<List<PostEntity>>

    /*Read all Posts in the Database*/
    @Query("SELECT * FROM comment_table")
    fun readAllComments(): Flow<List<CommentEntity>>

    /*Add Comment to Database*/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addComment(comment: CommentEntity)

    /*Read a Post Comments*/
    @Query("SELECT * FROM comment_table WHERE postId = :postId ORDER BY postId DESC")
    fun readComments(postId: Int): Flow<List<CommentEntity>>


    @Query("SELECT * FROM post_table WHERE title LIKE :searchQuery OR id LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): Flow<List<PostEntity>>
}