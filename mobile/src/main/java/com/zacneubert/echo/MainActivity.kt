package com.zacneubert.echo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.player.MediaPlayerService
import com.zacneubert.echo.player.PlayerFragment
import com.zacneubert.echo.podcast_list.EpisodeSelectedListener
import com.zacneubert.echo.podcast_list.PodcastListFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), EpisodeSelectedListener {
    companion object {
        private const val FRAGMENT_CHOICE_KEY: String = "FRAGMENT_CHOICE"
        fun ignitionIntent(context: Context, fragmentChoice: MainFragmentChoice) : Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(FRAGMENT_CHOICE_KEY, fragmentChoice)
            return intent
        }
    }

    enum class MainFragmentChoice(val num: Int) {
        PODCAST(R.id.podcasts),
        NOW_PLAYING(R.id.now_playing),
        SETTINGS(R.id.settings)
    }

    val podcastListFragment: PodcastListFragment = PodcastListFragment.newInstance(this)
    var playerFragment: PlayerFragment = PlayerFragment()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        swapToFragment(item.itemId)
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fragmentKey = MainFragmentChoice.PODCAST.num
        savedInstanceState?.apply {
            if (savedInstanceState.containsKey(FRAGMENT_CHOICE_KEY)) {
                fragmentKey = savedInstanceState.getInt(FRAGMENT_CHOICE_KEY)
            }
        }
        swapToFragment(fragmentKey)

        navigation_bar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onResume() {
        super.onResume()

        intent?.apply {
            this.extras?.apply {
                if(this.containsKey(FRAGMENT_CHOICE_KEY)) {
                    val fragmentChoice = this.get(FRAGMENT_CHOICE_KEY) as MainFragmentChoice
                    navigation_bar.selectedItemId = fragmentChoice.num
                }
            }
        }
    }

    private fun swapToFragment(fragmentType: Int) {
        when (fragmentType) {
            MainFragmentChoice.PODCAST.num -> swapToFragment(podcastListFragment)
            MainFragmentChoice.NOW_PLAYING.num -> {
                MediaPlayerService.mediaPlayerService?.apply {
                    playerFragment = PlayerFragment.newInstance(this@MainActivity, this.episode, false)
                }
                swapToFragment(playerFragment)
            }
            MainFragmentChoice.SETTINGS.num -> swapToFragment(podcastListFragment)
            else -> {
                throw Exception("Bad Fragment Type %d".format(fragmentType))
            }
        }
    }

    private fun swapToFragment(fragment: Fragment) {
        if (supportFragmentManager.findFragmentById(R.id.main_fragment_container) == null) {
            supportFragmentManager.beginTransaction().add(R.id.main_fragment_container, fragment).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).commit()
        }
    }

    override fun onEpisodeSelected(episode: Episode) {
        MediaPlayerService.mediaPlayerService?.apply {
            this.stopSelf()
        }
        playerFragment = PlayerFragment.newInstance(this, episode, true)
        navigation_bar.selectedItemId = R.id.now_playing
    }
}
