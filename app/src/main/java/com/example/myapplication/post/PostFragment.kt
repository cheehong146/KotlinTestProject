package com.example.myapplication.post

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.myapplication.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_post.*
import okhttp3.*
import java.io.IOException


class PostFragment : Fragment(), PostAdapterEvents {

    var postAdapter = PostAdapter(arrayListOf(), this)
    var isFetchingPost = false

    val FETCH_POST_PAGINATION_AMT = 8

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView_post.apply {
            // add layout manager
            var layoutManager = LinearLayoutManager(this.context)
            layoutManager.orientation = RecyclerView.VERTICAL
            this.layoutManager = layoutManager

            // add divider
            val cellDivider = DividerItemDecoration(
                this.context,
                layoutManager.orientation
            )
            this.addItemDecoration(cellDivider)

            this.adapter = postAdapter

            //pagination implementation
            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> return
                        RecyclerView.SCROLL_STATE_DRAGGING -> return
                        RecyclerView.SCROLL_STATE_SETTLING -> return
                    }

                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int){
                    Log.d("T", "dy: $dy")
                    if(dy > 0) {
                        val visibleItemCount = layoutManager.childCount;
                        val totalItemCount = layoutManager.itemCount;
                        val pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                        Log.d("T", "visibleItemCount: $visibleItemCount")
                        Log.d("T", "totalItemCount: $totalItemCount")
                        Log.d("T", "pastVisibleItems: $pastVisibleItems")
                        if (!isFetchingPost)
                        {
                            if ( (visibleItemCount + pastVisibleItems) >= totalItemCount)
                            {
                                fetchPost(totalItemCount)
                            }
                        }
                    }
                }
            })
        }

        fetchPost(0)
    }

    //startIndex to fetch the start index of the post
    //toAppend when true, append data to dataSource, while false it reset the dataSource
    private fun fetchPost(startIndex: Int, toAppend: Boolean = true) {

        Log.d("T", "fetching Post from $startIndex")
        var apiClient = OkHttpClient()

        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("jsonplaceholder.typicode.com")
            .addPathSegment("posts")
            .addQueryParameter("_start", startIndex.toString())
            .addQueryParameter("_limit", FETCH_POST_PAGINATION_AMT.toString())
            .build()

        val request = Request.Builder()
            .url(urlBuilder)
            .get()
            .build()

        isFetchingPost = true
        val call = apiClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isFetchingPost = false
                println("failed to execute request")
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                isFetchingPost = false
                val body = response.body?.string()

                val jsonResult: String = body ?: return
                val posts = Gson().fromJson(jsonResult, Array<Post>::class.java)

                Log.d("T", "fetched: ${posts.count()}")
                activity?.runOnUiThread {
                    postAdapter.appendPost(posts)
                    postAdapter.notifyDataSetChanged()
                }
            }

        })
    }

    private fun putPost(post: Post) {
        Log.d("T", "PUT to")

        var apiClient = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("postId", post.id.toString())
            .add("userId", post.userId.toString())
            .add("title", post.title)
            .add("body", post.body)
            .build()

        val request = Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts")
            .put(requestBody)
            .build()

        val call = apiClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("T", "PUT failed")
                println("failed to execute request")
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("T", "PUT success")
                //post success
                // at this point, u can update the data locally by getting the post ID, OR fetch all posts again. Depending on whether the mobile or server side processing/network limiting factor
                // i'll chose to fetch all the data again
                // but since PUT and POST request to jsonplaceholder.com doesn't actually create or update the data, fetching the data again won't contain the changes,
                fetchPost(0, false)
            }

        })
    }

    override fun onClick(data: Post) {
        Toast.makeText(this.context, "${data.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onLongClick(data: Post) {

        val dialog = this.activity?.let {
            MaterialDialog(it)
                .customView(R.layout.fragment_post_dialog)
        }

        dialog?.findViewById<EditText>(R.id.tf_edit_title)?.setText(data.title)
        dialog?.findViewById<EditText>(R.id.tf_edit_body)?.setText(data.body)

        dialog?.findViewById<Button>(R.id.btn_edit_apply)?.setOnClickListener {
            val updatedTitle = dialog.findViewById<EditText>(R.id.tf_edit_title).text.toString()
            val updatedBody = dialog.findViewById<EditText>(R.id.tf_edit_body).text.toString()

            val updatedPost = Post(data.userId, data.id, updatedTitle, updatedBody)

            this.putPost(updatedPost)
            dialog?.dismiss()
        }

        dialog?.show()
    }
}
