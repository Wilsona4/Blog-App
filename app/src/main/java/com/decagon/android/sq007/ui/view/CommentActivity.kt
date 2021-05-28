package com.decagon.android.sq007.ui.view

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.decagon.android.sq007.R
import com.decagon.android.sq007.databinding.ActivityCommentBinding
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.repository.Repository
import com.decagon.android.sq007.room.CachedCommentMapper
import com.decagon.android.sq007.room.CachedPostMapper
import com.decagon.android.sq007.room.LocalDataBase
import com.decagon.android.sq007.ui.adapter.CommentRvAdapter
import com.decagon.android.sq007.ui.view.MainActivity.Companion.POST
import com.decagon.android.sq007.util.ConnectivityLiveData
import com.decagon.android.sq007.util.LocalListUtil
import com.decagon.android.sq007.util.Resource
import com.decagon.android.sq007.viewModel.MainViewModel
import com.decagon.android.sq007.viewModel.MainViewModelFactory

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

        /*Set Status bar Color*/
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.statusBarColor = resources?.getColor(R.color.backgroundSecond)!!

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
                        viewModel.getComments(postId)
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
                commentRvAdapter.notifyDataSetChanged()
            }
        }

        /*Set-up Rv Swipe to Refresh*/
        binding.swipeRefreshComment.setOnRefreshListener {
            postId?.let { viewModel.getComments(it) }
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
        viewModel.getComments(postIds)
        viewModel.commentList.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let {
                        commentRvAdapter.submitList(it)
                        localCommentList = it.toMutableList()
                    }
                    hideProgressBar()
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(this, "Error: Occurred", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

}