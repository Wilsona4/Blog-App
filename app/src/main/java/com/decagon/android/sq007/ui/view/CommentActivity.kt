package com.decagon.android.sq007.ui.view

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.decagon.android.sq007.databinding.ActivityCommentBinding
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.repository.Repository
import com.decagon.android.sq007.room.CachedCommentMapper
import com.decagon.android.sq007.room.CachedPostMapper
import com.decagon.android.sq007.room.LocalDataBase
import com.decagon.android.sq007.ui.Intents.MainIntent
import com.decagon.android.sq007.ui.State.MainState
import com.decagon.android.sq007.ui.adapter.CommentRvAdapter
import com.decagon.android.sq007.ui.view.MainActivity.Companion.POST
import com.decagon.android.sq007.util.ConnectivityLiveData
import com.decagon.android.sq007.util.LocalListUtil
import com.decagon.android.sq007.viewModel.MainViewModel
import com.decagon.android.sq007.viewModel.MainViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var repository: Repository
    private lateinit var commentMapper: CachedCommentMapper
    private lateinit var postMapper: CachedPostMapper
    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    private lateinit var commentRvAdapter: CommentRvAdapter
    private lateinit var connectivityLiveData: ConnectivityLiveData

    private var localCommentList = LocalListUtil.getCommentList()

    var postIds = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectivityLiveData = ConnectivityLiveData(application)

        val retrievedPost: Post? = intent?.extras?.getParcelable(POST)
        val postId = retrievedPost?.id
        if (postId != null) {
            postIds = postId
        }

        /*Initialise ViewModel*/
        val roomDatabase = LocalDataBase.getInstance(this)
        commentMapper = CachedCommentMapper()
        postMapper = CachedPostMapper()
        repository = Repository(roomDatabase, commentMapper, postMapper)
        viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        progressBar = binding.commentProgressBar

        if (retrievedPost != null) {
            binding.commentPostTitle.text = retrievedPost.title
            binding.commentPostBody.text = retrievedPost.body
            binding.commentPostId.text = postId.toString()
        }

        setupRecyclerView()

        /*Set-Up Internet Connection Awareness*/
        connectivityLiveData.observe(this, Observer { isAvailable ->
            when (isAvailable) {
                true -> {
                    if (postId != null) {
                        lifecycleScope.launch {
                            viewModel.userIntent.send(MainIntent.GetComments(postId))
                        }
                    }
                    binding.rvComments.visibility = View.VISIBLE
                    binding.commentStatusButton.visibility = View.INVISIBLE
                    loadPage()
                }
                false -> {
                    binding.rvComments.visibility = View.INVISIBLE
                    binding.commentStatusButton.visibility = View.VISIBLE
                    hideProgressBar()
                }
            }
        })

        /*Add New Comment*/
        binding.floatingActionButton.setOnClickListener {
            if (retrievedPost != null) {
                AddCommentDialog(retrievedPost).show(supportFragmentManager, "D")
            }
        }

        /*Set-up Rv Swipe to Refresh*/
        binding.swipeRefreshComment.setOnRefreshListener {

            postId?.let {
                lifecycleScope.launch {
                    viewModel.userIntent.send(MainIntent.GetComments(it))
                }
            }
            loadPage()
            binding.swipeRefreshComment.isRefreshing = false
        }
    }


    /*Initialise RecyclerView*/
    private fun setupRecyclerView() {
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@CommentActivity)
            commentRvAdapter = CommentRvAdapter()
            adapter = commentRvAdapter
        }
    }

    private fun loadPage() {
        lifecycleScope.launch {
            viewModel.userIntent.send(MainIntent.GetComments(postIds))

            viewModel.state.collect { response ->
                when (response) {
                    is MainState.Idle -> {

                    }
                    is MainState.Comments -> {
                        response.let {
                            commentRvAdapter.submitList(it.comment)
                            localCommentList = it.comment.toMutableList()
                        }
                        hideProgressBar()
                    }
                    is MainState.Loading -> {
                        showProgressBar()
                    }
                    is MainState.Error -> {
                        hideProgressBar()
                        Toast.makeText(this@CommentActivity, "Error Occurred", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

}