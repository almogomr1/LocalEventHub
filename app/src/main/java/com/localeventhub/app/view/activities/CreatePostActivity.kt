package com.localeventhub.app.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ActivityCreatePostBinding
import com.localeventhub.app.model.EventLocation
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var context: Context
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationPickerLauncher: ActivityResultLauncher<Intent>
    private val postViewModel: PostViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private var eventLocation: EventLocation? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.postImage.setImageURI(uri)
                binding.uploadImageWrapper.visibility = View.GONE
            } else {
                Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this, "Permission denied. Cannot fetch location.", Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        setUpToolbar()
        initPlaces()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationRequest()

        checkLocationPermission()

        binding.uploadPostImageBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.postImage.setOnClickListener {
            if (selectedImageUri != null) {
                pickImageLauncher.launch("image/*")
            }
        }

        binding.saveBtn.setOnClickListener {

            val description = binding.description.text.toString()
            val tags = binding.tags.text.toString().trim()
            val location = binding.location.text.toString().trim()
            if (validation(description, tags, location)) {
                Constants.startLoading(context)
                if (selectedImageUri != null) {
                    postViewModel.uploadImageToFirebaseStorage(selectedImageUri!!) { status, url ->
                        val post = Post(
                            postId = postViewModel.getPostId(),
                            userId = Constants.loggedUserId,
                            description = description,
                            imageUrl = url,
                            eventLocation,
                            tags = tags.split(",").toList(),
                        )
                       postViewModel.addPost(post){status,message->
                           Constants.dismiss()
                           Constants.showAlert(context,message
                           ) { p0, p1 -> resetViews() }
                       }
                    }
                } else {
                    val post = Post(
                        postId =postViewModel.getPostId(),
                        userId =Constants.loggedUserId,
                        description =description,
                        imageUrl = "",
                        eventLocation,
                        tags = tags.split(",").toList()
                    )
                    postViewModel.addPost(post){status,message->
                        Constants.dismiss()
                        Constants.showAlert(context,message
                        ) { p0, p1 -> resetViews() }
                    }
                }
            }

        }

    }

    private fun validation(description: String, tags: String, location: String): Boolean {

        if (description.isEmpty()) {
            Constants.showAlert(context, "Description field is required!")
            return false
        } else if (tags.isEmpty()) {
            Constants.showAlert(context, "Tags field is required!")
            return false
        } else if (location.isEmpty()) {
            Constants.showAlert(context, "Location field is required!")
            return false
        }

        return true
    }

    private fun resetViews(){
        binding.description.setText("")
        binding.tags.setText("")
        binding.location.setText("")
        selectedImageUri = null
        eventLocation = null
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitle(getString(R.string.create_post))
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
    }

    private fun initPlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_map_api_key))
        }

        locationPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                handleLocationPickerResult(result.resultCode, result.data)
            }

        binding.location.setOnClickListener {
            launchLocationPicker()

        }
    }

    private fun launchLocationPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)
        locationPickerLauncher.launch(intent)
    }

    private fun handleLocationPickerResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val locationName = place.address
                val locationLatLng = place.latLng
                eventLocation =
                    EventLocation(locationLatLng!!.latitude, locationLatLng.longitude, locationName)
                binding.location.setText(locationName)
            }

            AutocompleteActivity.RESULT_ERROR -> {
                val status: Status = Autocomplete.getStatusFromIntent(data!!)

            }

            RESULT_CANCELED -> {

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(8000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude

//                    Toast.makeText(context, "Lat: $latitude, Lon: $longitude", Toast.LENGTH_SHORT)
//                        .show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}