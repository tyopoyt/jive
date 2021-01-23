package com.example.jive

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track
import kotlinx.android.synthetic.main.activity_main.pauseButton
import kotlinx.android.synthetic.main.activity_main.playButton
import kotlinx.android.synthetic.main.activity_main.resumeButton

enum class PlayingState {
    PAUSED, PLAYING, STOPPED
}

class MainActivity : AppCompatActivity() {

    private val clientId = "39629f4214764d369fdfde866ecde0f4"
    private val redirectUri = "com.example.jive://callback"
    private var spotifyAppRemote: SpotifyAppRemote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // connect user to Spotify
    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("MainActivity", "Connected! Yay!")
                // Now you can start interacting with App Remote
                setupViews()
                setupListeners()
            }
            override fun onFailure(throwable: Throwable) {
                Log.e("MainActivity", throwable.message, throwable)
            }
        })
    }

    // set up state and button listeners
    private fun setupViews () {
        playingState {
            when(it) {
                PlayingState.PLAYING -> showPauseButton()
                PlayingState.STOPPED -> showPlayButton()
                PlayingState.PAUSED -> showResumeButton()
            }
        }
    }
    private fun setupListeners() {
        playButton.setOnClickListener {
            play("spotify:playlist:37i9dQZF1DX7K31D69s4M1") // play "Piano in the Background" playlist
            showPauseButton()
        }
        pauseButton.setOnClickListener {
            pause()
            showResumeButton()
        }
        resumeButton.setOnClickListener {
            resume()
            showPauseButton()
        }
    }

    // handle button views
    private fun showPlayButton() {
        playButton.visibility = View.VISIBLE
        pauseButton.visibility = View.GONE
        resumeButton.visibility = View.GONE
    }
    private fun showPauseButton() {
        playButton.visibility = View.GONE
        pauseButton.visibility = View.VISIBLE
        resumeButton.visibility = View.GONE
    }
    private fun showResumeButton() {
        playButton.visibility = View.GONE
        pauseButton.visibility = View.GONE
        resumeButton.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }

    // implement the Spotify player
    fun play(uri: String) {
        spotifyAppRemote?.let {
            it.playerApi.play(uri)
            // Subscribe to PlayerState
            it.playerApi.subscribeToPlayerState().setEventCallback {
                val track: Track = it.track
                Log.d("MainActivity", track.name + " by " + track.artist.name)
            }
        }
    }
    fun resume() {
        spotifyAppRemote?.let {
            it.playerApi.resume()
        }
    }
    fun pause() {
        spotifyAppRemote?.let {
            it.playerApi.pause()
        }
    }
    fun playingState(handler: (PlayingState) -> Unit) {
        spotifyAppRemote?.let {
            it.playerApi.playerState.setResultCallback { result ->
                if (result.track.uri == null) {
                    handler(PlayingState.STOPPED)
                } else if (result.isPaused) {
                    handler(PlayingState.PAUSED)
                } else {
                    handler(PlayingState.PLAYING)
                } }
        }
    }

}