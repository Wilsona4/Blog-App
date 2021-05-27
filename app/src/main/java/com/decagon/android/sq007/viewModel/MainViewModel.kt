package com.decagon.android.sq007.viewModel

import androidx.lifecycle.*
import com.decagon.android.sq007.model.Comment
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.repository.IRepository
import com.decagon.android.sq007.ui.Intents.MainIntent
import com.decagon.android.sq007.ui.State.MainState
import com.decagon.android.sq007.ui.State.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: IRepository) : ViewModel() {

    val userIntent = Channel<MainIntent>(Channel.UNLIMITED)
    private val _state = MutableStateFlow<MainState>(MainState.Idle)
    val state: StateFlow<MainState>
        get() = _state


    private var _postList = MutableLiveData<Resource<List<Post>>>()
    val postList: LiveData<Resource<List<Post>>> get() = _postList

    private var _commentList = MutableLiveData<Resource<List<Comment>>>()
    val commentList: LiveData<Resource<List<Comment>>> get() = _commentList

    private var _entireCommentList = MutableLiveData<Resource<List<Comment>>>()
    val entireCommentList: LiveData<Resource<List<Comment>>> get() = _entireCommentList

    init { handleIntent() }

    private fun handleIntent() {
        viewModelScope.launch {
            userIntent.consumeAsFlow().collect {
                when(it) {
                    is MainIntent.GetPosts -> {getPosts()}
                    is MainIntent.GetAllComments -> {getAllComments()}
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
            _entireCommentList.value = Resource.Loading
            val response = repository.getAllComments()
            response.collect {
                _entireCommentList.value = it
            }
        }
    }

    /*Function to get Comments*/
    fun getComments(postId: Int) {
        viewModelScope.launch {
            _commentList.value = Resource.Loading
            val response = repository.getComments(postId)
            response.collect {
                _commentList.value = it
            }
        }
    }

    /*Function to add Comments*/
    fun pushComment(comment: Comment) {
        viewModelScope.launch {
            _commentList.value = Resource.Loading
            val response = repository.pushComment(comment)
            response.collect {
                _commentList.value = it
            }
        }
    }

    /*Function to add Posts*/
    fun addPost(post: Post) {
        viewModelScope.launch {
            repository.addPost(post)
        }
    }

//    /*Search Posts*/
//    private var cachedPostList = MutableLiveData<Resource<List<Post>>>()
//    private var isSearchStarting = true
//    var isSearching = MutableStateFlow(false)
//
//    fun searchPostList(query: String) {
//
//        if (isSearchStarting) {
//            cachedPostList.value = _postList.value
//            isSearchStarting = false
//        }
//
//        val listToSearch = if (isSearchStarting) {
//            postList.value
//        } else {
//            cachedPostList.value
//        }
//
//        viewModelScope.launch {
//            if (query.isEmpty()) {
//                _postList.value = cachedPostList.value
//                isSearching.value = false
//                isSearchStarting = true
//                return@launch
//            } else {
//                val results = listToSearch?.data?.filter {
//                    it.title.contains(query.trim(), ignoreCase = true) ||
//                            it.id.toString().contains(query.trim())
//                }
//                results?.let {
//                    _postList.value = Resource.Success(results)
//                }
//            }
//
//            if (isSearchStarting) {
//                cachedPostList.value = _postList.value
//                isSearchStarting = false
//            }
//
//            isSearching.value = true
//        }
//    }

}