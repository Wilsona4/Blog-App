package com.decagon.android.sq007.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {
    /*Add Post to Database*/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPost(post: PostEntity)

    /*Read all Posts in the Database*/
    @Query("SELECT * FROM post_table")
    suspend fun readAllPost(): List<PostEntity>

    /*Read all Posts in the Database*/
    @Query("SELECT * FROM comment_table")
    suspend fun readAllComments(): List<CommentEntity>

    /*Add Comment to Database*/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addComment(comment: CommentEntity)

    /*Read a Post Comments*/
    @Query("SELECT * FROM comment_table WHERE postId = :postId ORDER BY postId DESC")
    suspend fun readComments(postId: Int): List<CommentEntity>
}