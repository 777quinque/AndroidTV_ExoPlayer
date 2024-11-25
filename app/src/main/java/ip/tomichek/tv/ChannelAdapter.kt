package ip.tomichek.tv

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ChannelAdapter(channels: List<Channel>, private val picasso: Picasso) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {
    var channelList = ArrayList<Channel>()

    init {
        channelList.addAll(channels)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_films, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {

        return channelList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoPreview: ImageView = itemView.findViewById(R.id.videoPreview)
        val channelName: TextView = itemView.findViewById(R.id.channelName)
        val c1: ConstraintLayout = itemView.findViewById(R.id.c1)
        init {
            videoPreview.setOnClickListener {
                openFullScreenPlayer()
            }
            c1.setOnClickListener {
                openFullScreenPlayer()
            }
        }

        private fun openFullScreenPlayer() {
            val channel = channelList.getOrNull(adapterPosition)
            if (channel != null) {
                Log.d("ChannelAdapter", "Clicked on channel: $channel")
                val intent = Intent(itemView.context, FullScreenPlayerActivity::class.java)
                intent.putExtra("videoUrl", channel.videoUrl)
                itemView.context.startActivity(intent)
            } else {
                Log.e("ChannelAdapter", "Channel not found at position $adapterPosition")
            }
        }
    }

    fun updateChannels(newChannels: List<Channel>) {

        val diffResult = DiffUtil.calculateDiff(ChannelDiffCallback(channelList, newChannels))

        channelList.clear()
        channelList.addAll(newChannels)

        diffResult.dispatchUpdatesTo(this)
    }

    class ChannelDiffCallback(
        private val oldList: List<Channel>,
        private val newList: List<Channel>,

    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].name == newList[newItemPosition].name
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channel = channelList[position]



        if (!channel.imageUrl.isNullOrEmpty()) {

            picasso
                .load(channel.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.videoPreview)
        } else {

            holder.videoPreview.setImageResource(R.drawable.placeholder_image)
        }


        holder.channelName.text = channel.name
    }
}

