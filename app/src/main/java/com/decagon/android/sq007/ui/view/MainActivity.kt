package com.decagon.android.sq007.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.decagon.android.sq007.databinding.ActivityMainBinding
import com.decagon.android.sq007.model.Post
import com.decagon.android.sq007.repository.Repository
import com.decagon.android.sq007.room.CachedCommentMapper
import com.decagon.android.sq007.room.CachedPostMapper
import com.decagon.android.sq007.room.LocalDataBase
import com.decagon.android.sq007.ui.Intents.MainIntent
import com.decagon.android.sq007.ui.State.MainState
import com.decagon.android.sq007.ui.adapter.PostRvAdapter
import com.decagon.android.sq007.util.ConnectivityLiveData
import com.decagon.android.sq007.util.LocalListUtil.getPostList
import com.decagon.android.sq007.viewModel.MainViewModel
import com.decagon.android.sq007.viewModel.MainViewModelFactory
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
    private lateinit var connectivityLiveData: ConnectivityLiveData

    private var localPostList = getPostList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectivityLiveData = ConnectivityLiveData(application)

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
        displayItems()

//        /*Set-up Search functionality*/
//        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
//            OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let { viewModel.searchPostList(it) }
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                newText?.let { viewModel.searchPostList(it) }
//                return false
//            }
//        })

        /*Add New Comment*/
        binding.fabPost.setOnClickListener {
            AddPostDialog().show(supportFragmentManager, "D")
        }

        /*Set-up Rv Swipe to Refresh*/
        binding.swipeRefresh.setOnRefreshListener {
            displayItems()
            binding.swipeRefresh.isRefreshing = false
        }

    }

    private fun displayItems() {
        /*Set-Up Internet Connection Awareness*/
        connectivityLiveData.observe(this, Observer { isAvailable ->
            when (isAvailable) {
                true -> {
                    hideProgressBar()
                    binding.rvPost.visibility = View.VISIBLE
                    binding.fabPost.visibility = View.VISIBLE
                    binding.statusButton.visibility = View.INVISIBLE
                    launchView()
                    loadPage()
                }
                false -> {
                    binding.rvPost.visibility = View.INVISIBLE
                    binding.fabPost.visibility = View.INVISIBLE
                    binding.statusButton.visibility = View.VISIBLE
                    hideProgressBar()
                }
            }
        })
    }

    /*Initialise RecyclerView*/
    private fun setupRecyclerView() {
        binding.rvPost.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            postRvAdapter = PostRvAdapter(this@MainActivity)
            adapter = postRvAdapter
        }
    }

    private fun launchView() {
        lifecycleScope.launch {
            viewModel.userIntent.send(MainIntent.GetPosts)
        }
    }

    private fun loadPage() {
        lifecycleScope.launch {
            viewModel.state.collect { response ->
                when (response) {
                    is MainState.Idle -> {
                    }
                    is MainState.Posts -> {
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