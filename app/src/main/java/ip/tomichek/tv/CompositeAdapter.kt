package ip.tomichek.tv

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class CompositeAdapter(
    private val channelAdapter: ChannelAdapter,
    private val filterAdapter: FilterAdapter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var useFilterAdapter = false
    var megachannellist = channelAdapter.channelList
    var superchannellist = filterAdapter.fullChannelList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (useFilterAdapter) {
            filterAdapter.onCreateViewHolder(parent, viewType)
        } else {
            channelAdapter.onCreateViewHolder(parent, viewType)
        }
    }

    override fun getItemCount(): Int {
        return if (useFilterAdapter) {
            filterAdapter.itemCount
        } else {
            channelAdapter.itemCount
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (useFilterAdapter) {
            filterAdapter.onBindViewHolder(holder as FilterAdapter.ViewHolder, position)
        } else {
            channelAdapter.onBindViewHolder(holder as ChannelAdapter.ViewHolder, position)
        }
    }



    fun setUseFilterAdapter(useFilterAdapter: Boolean) {
        this.useFilterAdapter = useFilterAdapter
        notifyDataSetChanged()
    }

    fun updateChannels(newChannels: List<Channel>) {

        val diffResult = DiffUtil.calculateDiff(ChannelDiffCallback(channelAdapter.channelList, newChannels))

        channelAdapter.channelList.clear()
        channelAdapter.channelList.addAll(newChannels)

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
