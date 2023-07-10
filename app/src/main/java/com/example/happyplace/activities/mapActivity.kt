package com.example.happyplace.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplace.R
import com.example.happyplace.databinding.ActivityMapBinding
import com.example.happyplace.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class mapActivity : AppCompatActivity(), OnMapReadyCallback {
    var binding : ActivityMapBinding ?= null
    private var mHappyPlaceModel : HappyPlaceModel ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarMap)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarMap?.setNavigationOnClickListener{
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)
            as HappyPlaceModel
        }

        if (mHappyPlaceModel!= null){
            supportActionBar?.title = mHappyPlaceModel?.title
        }
        val supportMapFragment : SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as
                    SupportMapFragment
        supportMapFragment.getMapAsync(this,)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(mHappyPlaceModel!!.latitude,mHappyPlaceModel!!.longitude)
        googleMap!!.addMarker(MarkerOptions().position(position).title(mHappyPlaceModel!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position,15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}