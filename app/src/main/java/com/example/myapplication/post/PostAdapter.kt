package com.example.myapplication.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import kotlinx.android.synthetic.main.post_cell.view.*

interface PostAdapterEvents {
    fun onClick(data: Post)
    fun onLongClick(data: Post)
}

class PostAdapter(private var posts: ArrayList<Post> = arrayListOf(),
                  private val postAdapterEvents: PostAdapterEvents
): RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun getItemCount(): Int {
        return posts.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.post_cell, parent, false)
        return PostViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val cellData = posts[position]

        holder.itemView.textView_title.text = cellData.title
        holder.itemView.textView_body.text = cellData.body

        holder.itemView.setOnClickListener {
            postAdapterEvents.onClick(cellData)
        }

        holder.itemView.setOnLongClickListener {
            postAdapterEvents.onLongClick(cellData)
            true
        }
    }

    fun setPost(posts: ArrayList<Post>) {
        this.posts = posts
    }

    fun appendPost(posts: Array<Post>) {
        this.posts.addAll(posts)
    }

    inner class PostViewHolder(v :View): RecyclerView.ViewHolder(v) {
    }

}
