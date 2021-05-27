package com.decagon.android.sq007.ui.Intents

sealed class MainIntent {

    object GetPosts: MainIntent()
    object GetAllComments: MainIntent()
}