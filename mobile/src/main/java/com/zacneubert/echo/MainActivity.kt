package com.zacneubert.echo

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val FRAGMENT_CHOICE_KEY = "FRAGMENT_CHOICE"
    enum class MainFragmentChoice(val num: Int) {
        PODCAST(R.id.podcasts),
        NOW_PLAYING(R.id.now_playing),
        SETTINGS(R.id.settings)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        swapToFragment(item.itemId)
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fragmentKey = MainFragmentChoice.PODCAST.num
        savedInstanceState?.apply {
            if(savedInstanceState.containsKey(FRAGMENT_CHOICE_KEY)) {
                fragmentKey = savedInstanceState.getInt(FRAGMENT_CHOICE_KEY)
            }
        }
        swapToFragment(fragmentKey)

        navigation_bar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    fun swapToFragment(fragmentType: Int) {
        when(fragmentType) {
            MainFragmentChoice.NOW_PLAYING.num -> swapToFragment(PodcastListFragment())
            MainFragmentChoice.SETTINGS.num -> swapToFragment(PodcastListFragment())
            MainFragmentChoice.PODCAST.num -> swapToFragment(PodcastListFragment())
            else -> {
                throw Exception("Bad Fragment Type %d".format(fragmentType))
            }
        }
    }

    fun swapToFragment(fragment: Fragment) {
        if(supportFragmentManager.findFragmentById(R.id.main_fragment_container) == null) {
            supportFragmentManager.beginTransaction().add(R.id.main_fragment_container, fragment).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, fragment).commit()
        }
    }
}
