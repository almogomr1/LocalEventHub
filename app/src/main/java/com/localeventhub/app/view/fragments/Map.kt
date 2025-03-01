package com.localeventhub.app.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.localeventhub.app.R
import com.localeventhub.app.databinding.MapBinding
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Map : Fragment(), OnMapReadyCallback {

    private lateinit var binding:MapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val postViewModel: PostViewModel by viewModels()
    private var posts = mutableListOf<Post>()


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                showCurrentLocation()
            } else {
                showPermissionDeniedMessage()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MapBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        postViewModel.loadPosts(Constants.isOnline(requireActivity()))
        viewLifecycleOwner.lifecycleScope.launch {
            postViewModel.posts.collect { postList ->
                if (postList.isNotEmpty()){
                    posts.clear()
                    posts.addAll(postList)
                    delay(2000)
                    if (isAdded) { // Ensure the fragment is attached
                        showMarkersOnMap()
                    }
                }
            }
        }

    }

    private fun showMarkersOnMap() {
        if (posts.isNotEmpty()) {
            if(isAdded){
                requireActivity().runOnUiThread {
                    for (post in posts) {
                        val latLng = LatLng(post.location!!.latitude, post.location!!.longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("Event at ${post.location!!.latitude}, ${post.location!!.longitude}")
                        )
                        marker?.tag = post
                    }

                    googleMap.setOnMarkerClickListener { marker ->
                        val post = marker.tag as? Post
                        if (post != null) {
                            val bundle = Bundle().apply {
                                putSerializable("post", post)
                            }
                            findNavController().navigate(R.id.action_mapView_to_postDetails, bundle)
                        }
                        true
                    }
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                showCurrentLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationale()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentLocation() {
        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)

                googleMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            }
        }
    }

    private fun showPermissionRationale() {
        Toast.makeText(
            requireContext(),
            "Location permission is required to show your current location.",
            Toast.LENGTH_LONG
        ).show()

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            requireContext(),
            "Permission denied. Cannot show your location.",
            Toast.LENGTH_LONG
        ).show()
    }

}