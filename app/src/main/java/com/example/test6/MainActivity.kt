package com.example.test6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.example.test6.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        view_pager.isUserInputEnabled = false
        val adapter = ViewPagerAdapter(supportFragmentManager,lifecycle)
        view_pager.adapter = adapter

        TabLayoutMediator(tabLayout,view_pager){tab,position->
            when(position){
                0->{
                    tab.text ="聊天室"
                }
                1->{
                    tab.text="點餐"
                }
                2->{
                    tab.text="營養資訊"
                }
                3->{
                    tab.text="Fourth"
                }
                4->{
                    tab.text="Fiveth"
                }
            }
        }.attach()

    }
}