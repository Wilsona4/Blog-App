package com.decagon.android.sq007.ui.State

import com.decagon.android.sq007.model.Post

sealed class MainState {
    data class Success<out T>(val data: T): MainState()

    data class Error(val exception: Exception): MainState()
    data class Posts(val post: List<Post>) : MainState()

    object Loading: MainState()
    object Idle: MainState()


}