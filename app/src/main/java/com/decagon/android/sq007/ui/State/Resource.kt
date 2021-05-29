package com.decagon.android.sq007.ui.State

import com.decagon.android.sq007.model.Post

// Generic Resource Class to handle network errors
//sealed class Resource<T>(val data: T? = null, val message: String? = null) {
//    class Success<T>(data: T) : Resource<T>(data)
//    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
//    class Loading<T>(data: T? = null) : Resource<T>(data)
//}

sealed class Resource<out T>() {

    data class Success<out T>(val data: T): Resource<T>()

    data class Error(val exception: Exception): Resource<Nothing>()

    object Loading: Resource<Nothing>()
}