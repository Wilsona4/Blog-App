package com.decagon.android.sq007.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.decagon.android.sq007.R
import com.decagon.android.sq007.databinding.ActivityMainBinding
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.repository.Repository
import com.decagon.android.sq007.room.CachedCommentMapper
import com.decagon.android.sq007.room.CachedPostMapper
import com.decagon.android.sq007.room.LocalDataBase
import com.decagon.android.sq007.ui.Intents.MainIntent
import com.decagon.android.sq007.ui.State.MainState
import com.decagon.android.sq007.ui.adapter.PostRvAdapter
import com.decagon.android.sq007.util.LocalListUtil.getPostList
import com.decagon.android.sq007.viewModel.MainViewModel
import com.decagon.android.sq007.viewModel.MainViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(), PostRvAdapter.Interaction {
    private lateinit var binding: ActivityMainBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var repository: Repository
    private lateinit var commentMapper: CachedCommentMapper
    private lateinit var postMapper: CachedPostMapper
    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelFactory: MainViewModelFactory
    private lateinit var postRvAdapter: PostRvAdapter

    private var localPostList = getPostList()

    private var isSearching = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*Set Status bar Color*/
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.statusBarColor = resources?.getColor(R.color.backgroundSecond)!!

        /*Initialise ViewModel*/
        val roomDatabase = LocalDataBase.getInstance(this)
        commentMapper = CachedCommentMapper()
        postMapper = CachedPostMapper()
        repository = Repository(roomDatabase, commentMapper, postMapper)
        viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        progressBar = binding.progressBar

        /*Initialise RecyclerView*/
        setupRecyclerView()
        launchView()
        loadPage()

        /*Set-up Search functionality*/
        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) {
                    isSearching.value = false
                    lifecycleScope.launch {
                        viewModel.userIntent.send(MainIntent.GetCachedPosts)
                    }
                } else {
                    isSearching.value = true
                    lifecycleScope.launch {
                        viewModel.userIntent.send(MainIntent.Search(query))
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText.isNullOrEmpty()) {
                    isSearching.value = false
                    lifecycleScope.launch {
                        viewModel.userIntent.send(MainIntent.GetCachedPosts)
                    }
                } else {
                    isSearching.value = true
                    lifecycleScope.launch {
                        viewModel.userIntent.send(MainIntent.Search(newText))
                    }
                }
                return false
            }
        })

        /*Add New Comment*/
        binding.fabPost.setOnClickListener {
            AddPostDialog().show(supportFragmentManager, "D")
        }

        /*Set-up Rv Swipe to Refresh*/
        binding.swipeRefresh.setOnRefreshListener {
            if (!isSearching.value) {
                lifecycleScope.launch {
                    viewModel.userIntent.send(MainIntent.RefreshPostIntent)
                }
                binding.swipeRefresh.isRefreshing = false
            } else {
                binding.swipeRefresh.isRefreshing = false
                return@setOnRefreshListener
            }
        }

    }

    /*Initialise RecyclerView*/
    private fun setupRecyclerView() {
        binding.rvPost.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            postRvAdapter = PostRvAdapter(this@MainActivity)
            adapter = postRvAdapter
        }

        /*Scroll to Position of New Post*/
        postRvAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.rvPost.scrollToPosition(positionStart)
            }
        })
    }

    /*Get Comments*/
    private fun launchView() {
        lifecycleScope.launch {
            viewModel.userIntent.send(MainIntent.GetCachedPosts)
        }
    }

    /*Update UI Based on App State*/
    private fun loadPage() {
        lifecycleScope.launch {
            viewModel.state.collect { response ->
                when (response) {
                    is MainState.Idle -> {
                    }
                    is MainState.RemotePost -> {
                        response.let {
                            postRvAdapter.submitList(it.post)
                            localPostList = it.post as MutableList<Post>
                        }
                        hideProgressBar()
                    }
                    is MainState.CachedPost -> {
                        response.let {
                            postRvAdapter.submitList(it.post)
                            localPostList = it.post as MutableList<Post>
                        }
                        hideProgressBar()
                    }
                    is MainState.Search -> {
                        response.let {
                            postRvAdapter.submitList(it.post)
                            localPostList = it.post as MutableList<Post>
                        }
                        hideProgressBar()
                    }
                    is MainState.Loading -> {
                        showProgressBar()
                    }
                    is MainState.Error -> {
                        hideProgressBar()
                        Toast.makeText(this@MainActivity, "Error: Occurred", Toast.LENGTH_SHORT)
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

    override fun onItemSelected(position: Int, item: Post) {
        val currentPost = localPostList[position]

        val intent = Intent(this, CommentActivity::class.java).apply {
            putExtra(POST, currentPost)
        }
        startActivity(intent)
    }

    companion object {
        const val POST = "post"
    }
}