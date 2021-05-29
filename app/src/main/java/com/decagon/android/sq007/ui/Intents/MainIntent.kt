package com.decagon.android.sq007.ui.Intents

import com.decagon.android.sq007.model.Comment
import com.decagon.android.sq007.model.Post

sealed class MainIntent {

    object RefreshPostIntent : MainIntent()
    data class RefreshCommentIntent(val postId: Int) : MainIntent()
    object GetRemotePosts : MainIntent()
    object GetCachedPosts : MainIntent()
    object GetAllComments : MainIntent()
    data class GetComments(val postId: Int) : MainIntent()
    data class AddPost(val post: Post) : MainIntent()
    data class AddComment(val comment: Comment) : MainIntent()
    data class Search(val query: String) : MainIntent()
}