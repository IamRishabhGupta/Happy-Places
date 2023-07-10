package com.example.happyplace.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.happyplace.R
import com.example.happyplace.databinding.ActivityHappyPlaceDetailsBinding
import com.example.happyplace.models.HappyPlaceModel

class HappyPlaceDetailsActivity : AppCompatActivity() {

    var binding : ActivityHappyPlaceDetailsBinding ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)



        var happyPlaceDetailsMode : HappyPlaceModel ?= null
         if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
             happyPlaceDetailsMode = intent.getSerializableExtra(
                 MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
         }
        if (happyPlaceDetailsMode != null){
            setSupportActionBar(binding?.toolbarDetailsPlace)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Happy Place Details"
            binding?.toolbarDetailsPlace?.setNavigationOnClickListener{
                onBackPressed()
            }

            binding?.detailImage?.setImageURI(Uri.parse(happyPlaceDetailsMode.image))
            binding?.detailDescription?.text = happyPlaceDetailsMode.description
            binding?.detailTitle?.text = happyPlaceDetailsMode?.title
        }

        binding?.btnViewOnMap?.setOnClickListener{
            val intent = Intent(this,mapActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,happyPlaceDetailsMode)
            startActivity(intent)
        }
    }
}