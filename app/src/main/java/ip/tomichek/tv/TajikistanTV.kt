package ip.tomichek.tv

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.Locale

class TajikistanTV : AppCompatActivity() {

    private lateinit var channelRecyclerView: RecyclerView
    private lateinit var channelAdapter: ChannelAdapterForChannels
    private lateinit var filterAdapter: FilterAdapterForChannels
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var compositeAdapter: CompositeAdapterForChannels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchEditText = findViewById(R.id.searchView)


        val okHttpClient = OkHttpClient.Builder()
            .dispatcher(Dispatcher().apply {
                maxRequests = 1000
            })
            .build()


        val picasso = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(okHttpClient))
            .build()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        channelRecyclerView = findViewById(R.id.channelRecyclerView)

        channelAdapter = ChannelAdapterForChannels(emptyList(), picasso)
        filterAdapter = FilterAdapterForChannels(emptyList(), picasso)
        compositeAdapter = CompositeAdapterForChannels(channelAdapter, filterAdapter)

        channelRecyclerView.layoutManager = LinearLayoutManager(this)
        channelRecyclerView.adapter = compositeAdapter
        val layoutManager = GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false)
        channelRecyclerView.layoutManager = layoutManager
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.textView12)

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Ваши действия перед изменением текста
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Ваши действия при изменении текста
                charSequence?.let { filter(it.toString()) }
            }

            override fun afterTextChanged(editable: Editable?) {
                // Ваши действия после изменения текста
            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                loadPartialPlaylist()

            } catch (e: Exception) {
                // Error handling
                Log.e("MainActivity", "Error loading playlist: ${e.message}", e)
            }
        }

    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<Channel>()
        Log.d("Filter", "Filtering with text: $text")
        // running a for loop to compare elements.
        for (item in compositeAdapter.superchannellist) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name.toLowerCase().contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            Log.d("Filter", "Filtered list: " + compositeAdapter.superchannellist)
            // Первоначальная установка данных

            compositeAdapter.filterList(filteredlist)
        }
    }

    private suspend fun loadPartialPlaylist(): List<Channel> = withContext(Dispatchers.IO) {
        val playlistUrl =
            "https://dl.dropbox.com/scl/fi/5l92pftxkfku23vp2ruug/main.m3u8?rlkey=ut43c37g1llap4g3x6g8x44dl"
        try {

            val connection = URL(playlistUrl).openConnection()
            val inputStream = connection.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))

            val partialPlaylist = mutableListOf<String>()
            while (true) {
                val line = reader.readLine() ?: break
                partialPlaylist.add(line)
            }

            // Parse and return the partial playlist
            val initialChannels = parsePlaylist(partialPlaylist.joinToString("\n"))

            // Update RecyclerView with the initial channels
            withContext(Dispatchers.Main) {
                compositeAdapter.updateChannels(initialChannels)
                channelAdapter.updateChannels(initialChannels)
                filterAdapter.updateChannels(initialChannels)
                searchEditText.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                textView.visibility = View.GONE
                channelRecyclerView.visibility = View.VISIBLE
            }

            // Continue loading and updating in real-time
            for (i in 1 until partialPlaylist.size) {
                val line = partialPlaylist[i]
                if (line.startsWith("#EXTINF")) {
                    val newChannels = parsePlaylist(line)
                    // Update RecyclerView with the new channels in real-time
                    withContext(Dispatchers.Main) {
                        newChannels.forEach { newChannel ->
                            updateRecyclerView(newChannel)
                        }
                        compositeAdapter.megachannellist.toMutableList().apply { addAll(newChannels) }
                        channelAdapter.channelList.toMutableList().apply { addAll(newChannels) }
                        filterAdapter.fullChannelList.toMutableList().apply { addAll(newChannels) }
                    }
                }
            }
            emptyList()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading partial playlist: ${e.message}", e)
            emptyList()
        }
    }
    private fun updateRecyclerView(newChannel: Channel) {
        val currentChannels = channelAdapter.channelList.toMutableList()
        currentChannels.add(newChannel)
        val currentChannels2 = filterAdapter.fullChannelList.toMutableList()
        currentChannels2.add(newChannel)

        compositeAdapter.updateChannels(currentChannels)

    }
}


private fun parsePlaylist(playlist: String): List<Channel> {
    val channels = mutableListOf<Channel>()

    val lines = playlist.lines()

    var imageUrl = ""
    var name = ""
    var videoURL = ""
    var groupTitle = ""

    for (line in lines) {
        when {
            line.startsWith("#EXTGRP") -> {
                // Обработка строки с группой
                groupTitle = line.substringAfter(":")
            }
            line.startsWith("http") -> {
                videoURL = line
                if (groupTitle == "Таджикистан TV") {
                    Log.d("PlaylistParsing", "groupTitle: $groupTitle, Name: $name, Image URL: $imageUrl, Video URL: $videoURL")
                    channels.add(Channel(name, imageUrl, videoURL))
                }
                // Сбросить переменные для следующего канала
                imageUrl = ""
                name = ""
                videoURL = ""
            }
            line.startsWith("#EXTINF") -> {
                // Обработка строки с информацией о канале
                imageUrl = line.substringAfter("tvg-logo=\"").substringBefore("\"")
                name = if ("#EXTINF:0," in line) {
                    line.substringAfter(":0,").substringBefore(",").trim()
                } else {
                    line.substringAfter("\",").substringBefore("\",").trim()
                }
            }
        }
    }

    return channels
}




