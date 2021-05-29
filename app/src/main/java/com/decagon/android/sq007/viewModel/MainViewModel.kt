package com.decagon.android.sq007.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.android.sq007.model.Comment
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.repository.IRepository
import com.decagon.android.sq007.ui.Intents.MainIntent
import com.decagon.android.sq007.ui.State.MainState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: IRepository) : ViewModel() {

    val userIntent = Channel<MainIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<MainState>(MainState.Idle)
    val state: StateFlow<MainState>
        get() = _state


    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            userIntent.consumeAsFlow().collect {
                when (it) {
                    is MainIntent.GetPosts -> {
                        getPosts()
                    }
                    is MainIntent.GetAllComments -> {
                        getAllComments()
                    }
                    is MainIntent.GetComments -> {
                        getComments(it.postId)
                    }
                    is MainIntent.AddPost -> {
                        addPost(it.post)
                    }
                    is MainIntent.AddComment -> {
                        addComment(it.comment)
                    }
                    is MainIntent.Search -> {
//                        searchPost(it.query)
                    }
                }
            }
        }
    }

    /*Function to get post*/
    fun getPosts() {
        viewModelScope.launch {
            _state.value = MainState.Loading
            val response = repository.getPosts()
            response.collect {
                _state.value = it
            }
        }
    }

    /*Function to get All Comments*/
    fun getAllComments() {
        viewModelScope.launch {
            _state.value = MainState.Loading
            val response = repository.getAllComments()
            response.collect {
                _state.value = it
            }
        }
    }

    /*Function to get Comments*/
    fun getComments(postId: Int) {
        viewModelScope.launch {
            _state.value = MainState.Loading
            val response = repository.getComments(postId)
            response.collect {
                _state.value = it
            }
        }
    }

    /*Function to add Comments*/
    fun addComment(comment: Comment) {
        viewModelScope.launch {
            _state.value = MainState.Loading
            val response = repository.pushComment(comment)
            response.collect {
                _state.value = it
            }
        }
    }

    /*Function to add Posts*/
    fun addPost(post: Post) {
        viewModelScope.launch {
            _state.value = MainState.Loading
            val response = repository.addPost(post)
            response.collect {
                _state.value = it
            }
        }
    }

//    /*Search DB*/
//    fun searchPost(query: String) {
//        viewModelScope.launch {
//            _state.value = MainState.Loading
//            val response = repository.search(query)
//            response.collect {
//                _state.value = it
//            }
//        }
//    }

    /*Search Posts*/
    private var cachedPostList = MutableStateFlow<MainState>(MainState.Idle)
    private var isSearchStarting = true
    var isSearching = MutableStateFlow(false)

//    fun searchPostList(query: String) {
//
//        if (isSearchStarting) {
//            cachedPostList.value = _state.value
//            isSearchStarting = false
//        }
//
//        val listToSearch = if (isSearchStarting) {
//            _state.value
//        } else {
//            cachedPostList.value
//        }
//
//        viewModelScope.launch {
//            if (query.isEmpty()) {
//                _state.value = cachedPostList.value
//                isSearching.value = false
//                isSearchStarting = true
//                return@launch
//            } else {
//                val results = listToSearch.filter {
//                    it.title.contains(query.trim(), ignoreCase = true) ||
//                            it.id.toString().contains(query.trim())
//                }
//                results?.let {
//                    _state.value = MainState.EntirePost(results)
//                }
//            }
//
//            if (isSearchStarting) {
//                cachedPostList.value = _state.value
//                isSearchStarting = false
//            }
//
//            isSearching.value = true
//        }
//    }



}