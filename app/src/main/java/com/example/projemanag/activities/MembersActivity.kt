package com.example.projemanag.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityMembersBinding
import com.example.projemanag.models.Board
import com.example.projemanag.utils.Constants

class MembersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMembersBinding
    private lateinit var mBoardDetails: Board


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarMembersActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }
        binding.toolbarMembersActivity.setNavigationOnClickListener { onBackPressed() }
    }
}