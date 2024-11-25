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

private lateinit var channelAdapter: ChannelAdapter

class FilterAdapterForChannels (channels: List<Channel>, private val picasso: Picasso) : RecyclerView.Adapter<FilterAdapterForChannels.ViewHolder>() {
    var fullChannelList = ArrayList<Channel>()

    init {

        fullChannelList.addAll(channels)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channels, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)  {
        val channel = fullChannelList[position]



        if (!channel.imageUrl.isNullOrEmpty()) {
            // Загрузка изображения с помощью Picasso в превью
            picasso
                .load(channel.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.videoPreview)
        } else {

            holder.videoPreview.setImageResource(R.drawable.placeholder_image)
        }


        holder.channelName.text = channel.name
    }

    override fun getItemCount(): Int {

        return fullChannelList.size
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
            val channel = fullChannelList.getOrNull(adapterPosition)
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

        val diffResult = DiffUtil.calculateDiff(ChannelDiffCallback(fullChannelList, newChannels))

        fullChannelList.clear()
        fullChannelList.addAll(newChannels)

        diffResult.dispatchUpdatesTo(this)
    }

    fun filterList(filterList: ArrayList<Channel>) {
        // below line is to add our filtered
        // list in our course array list.
        channelAdapter.channelList = filterList
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
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

}

