package com.example.happyplace.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.happyplace.R
import com.example.happyplace.database.DatabaseHandler
import com.example.happyplace.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplace.models.HappyPlaceModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.happyplaces.utils.GetAddressFromLatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity() , View.OnClickListener {

    var binding : ActivityAddHappyPlaceBinding ?= null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternlStorge : Uri ?= null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mHappyPlaceDetails : HappyPlaceModel ?= null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener{
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity,resources.getString(R.string.APIKey))
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)
            as HappyPlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }

        updateDateInView()

        if (mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"

            binding?.etTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)
            binding?.etLocation?.setText(mHappyPlaceDetails!!.location)
            mLongitude = mHappyPlaceDetails!!.longitude
            mLatitude = mHappyPlaceDetails!!.latitude
            saveImageToInternlStorge = Uri.parse(mHappyPlaceDetails!!.image)
            binding?.InputImage?.setImageURI(saveImageToInternlStorge)
            binding?.btnAddHappyPlace?.text = "UPDATE"

        }

        binding?.etDate?.setOnClickListener(this)
        binding?.AddImage?.setOnClickListener(this)
        binding?.btnAddHappyPlace?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)
    }

    private fun isLocationEnabled():Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as
                LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        val mlocationRequest = com.google.android.gms.location.LocationRequest()
        mlocationRequest.priority = LocationRequest.QUALITY_HIGH_ACCURACY
        mlocationRequest.interval = 0
        mlocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(mlocationRequest,
            mLocationCallBack,Looper.myLooper())
    }

    private val mLocationCallBack = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation : Location = locationResult.lastLocation!!
            mLatitude = mLastLocation.latitude
            mLongitude = mLastLocation.longitude

            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity,mLatitude,mLongitude)
            addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener{
                override fun onAddressFound(address: String?) {
                    binding?.etLocation?.setText(address)
                }
                override fun onError() {
                    Log.e("Get Address :: " , "Something went wrong")
                }
            })
            addressTask.getAddress()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.etDate -> {
                DatePickerDialog(this@AddHappyPlaceActivity,
                dateSetListener,cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.AddImage ->{
                val pictureDialog = AlertDialog.Builder(this).setTitle("Select Action")
                val PictureDialogItems = arrayOf("Select Photo From Gallery" ,
                "Cature Photo from Camera")
                pictureDialog.setItems(PictureDialogItems){
                        _, which ->
                    when(which){
                        0->choosePhotoFromGallery()
                        1->takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.btnAddHappyPlace -> {
                d("hbbkbh","nihhbn")
                when {
                    binding?.etTitle?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT)
                            .show()
                    }
                    binding?.etLocation?.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                            .show()
                    }
                    saveImageToInternlStorge == null -> {
                        Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
                    }
                    else ->{
                        val happyPlaceModal = HappyPlaceModel(
                            if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            binding?.etTitle?.text.toString(),
                            saveImageToInternlStorge.toString(),
                            binding?.etDescription?.text.toString(),
                            binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        if(mHappyPlaceDetails == null){
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModal)

                            if (addHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }else{
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModal)

                            if (updateHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            }
            R.id.etLocation ->{

                val placeDialog = AlertDialog.Builder(this).setTitle("Select Action")
                val placeDialogItems = arrayOf("Enter the location manually" ,
                    "choose from google maps")
                placeDialog.setItems(placeDialogItems){
                        _, which ->
                    when(which){
                        0->{
                            binding?.etLocation?.setOnClickListener(null)
                            binding?.etLocation?.focusable = View.FOCUSABLE
                            binding?.etLocation?.setFocusableInTouchMode(true)
                        }
                        1->selectLocationFromMaps()
                    }
                }
                placeDialog.show()

            }
            R.id.tv_select_current_location -> {
                if (!isLocationEnabled()){
                    Toast.makeText(this,"hai mere pass tera",Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else{
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener{
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()){
                                Toast.makeText(this@AddHappyPlaceActivity,
                                    "kaam ho gaya",Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationaleDialogPermissions()
                        }
                    })
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY){
                if (data != null){
                    val contentURI = data.data
                    try{
                        var SelectedImageBitmap = MediaStore.Images.Media.
                        getBitmap(this.contentResolver,contentURI)
                        saveImageToInternlStorge = saveImageToInternalStorage(SelectedImageBitmap)

                        Log.e("Saved Image : ", "Path :: $saveImageToInternlStorge")

                        binding?.InputImage?.setImageBitmap(SelectedImageBitmap)
                    }catch (e : IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,
                        "Fail to load the image from storage",Toast.LENGTH_SHORT).show()
                    }
                }
            }else if (requestCode == CAMERA){
                val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternlStorge = saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image : ","Path :: $saveImageToInternlStorge")
                binding?.InputImage?.setImageBitmap(thumbnail)
            }
            else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

                val place: Place = Autocomplete.getPlaceFromIntent(data!!)

                binding?.etLocation?.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }else if (resultCode == Activity.RESULT_CANCELED){
            Log.e("Cancelled","Cancelled")
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener{
            override fun onPermissionsChecked (report : MultiplePermissionsReport?){
                if (report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationaleDialogPermissions()
            }
        }).onSameThread().check()
    }

            private fun choosePhotoFromGallery() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener{
            override fun onPermissionsChecked (report : MultiplePermissionsReport?){
                if (report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationaleDialogPermissions()
            }
        }).onSameThread().check()
    }

    private fun selectLocationFromMaps(){

        try{
            val fields = listOf(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this@AddHappyPlaceActivity)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun showRationaleDialogPermissions() {
        androidx.appcompat.app.AlertDialog.Builder(this).
        setMessage("It looks like you have turned off permission").
        setPositiveButton("Go To Setting")
        {_,_ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",packageName,null)
                intent.data = uri
                startActivity(intent)
            }catch (e:ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){ dialog, _ ->
            dialog.dismiss()
        }.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat,Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap : Bitmap) : Uri{
        val wrapper  = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jgp")

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e : IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}