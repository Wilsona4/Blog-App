package com.decagon.android.sq007.ui.State

import com.decagon.android.sq007.model.Comment
import com.decagon.android.sq007.model.Post

sealed class MainState {

    data class Error(val exception: Exception): MainState()

    object Loading: MainState()
    object Idle: MainState()

    data class EntirePost(val post: List<Post>) : MainState()
    data class Comments(val comment: List<Comment>): MainState()
    data class EntireComment(val entireComment: List<Comment>): MainState()
    data class Search(val post: List<Post>): MainState()

}