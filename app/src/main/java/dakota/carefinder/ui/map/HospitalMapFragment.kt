package dakota.carefinder.ui.map

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dakota.carefinder.R
import dakota.carefinder.data.model.DEFAULT_LOCATION_COORD
import dakota.carefinder.data.model.Hospital
import dakota.carefinder.ui.detail.HospitalDetailFragment
import dakota.carefinder.ui.main.MainHospitalViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by dakota on 11/27/17.
 */
class HospitalMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    /**
     * The ViewModel to observe data changes from
     */
    private lateinit var viewModel: MainHospitalViewModel

    /**
     * A google map object to put markers on
     */
    private lateinit var googleMap: GoogleMap

    /**
     * A list of markers
     */
    private var markers: MutableList<Marker> = mutableListOf()

    /**
     * Connect to the ViewModel onCreate.
     *
     * @param savedInstanceState bundle from previous state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity).get(MainHospitalViewModel::class.java)
        this.setHasOptionsMenu(true)
    }

    /**
     * Inflate the view and register the SupportMapFragment.
     *
     * @param inflater layout inflater
     * @param container container ViewGroup
     * @param savedInstanceState bundle from previous state
     * @return a created view
     */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_map, container, false)
        val map = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        map.getMapAsync(this)
        return v
    }

    /**
     * When the map has been rendered, start observing the data sources from the ViewModel to listen
     * for changes so the UI can be updated accordingly.
     *
     * @param p0 a GoogleMap object
     */
    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0!!
        //fab.show()

        //Observe the hospitals for changes to the model - filter out those that have default location values
        viewModel.getCurrentHospitals().observe(this, Observer<MutableMap<String, Hospital>> { hospitals ->
            updateMap(hospitals?.values?.filter {
                it.location != null &&
                        (it.location?.latitude != DEFAULT_LOCATION_COORD && it.location?.longitude != DEFAULT_LOCATION_COORD)
            })
        })

        //Map settings
        googleMap.setOnInfoWindowClickListener(this)
        googleMap.uiSettings.isMapToolbarEnabled = false

    }

    /**
     * Update the map with markers, zoom to show the markers, and update the activity actionbar
     * subtitle.
     *
     * @param mappedHospitals a collection of hospitals to be put on the map
     */
    private fun updateMap(mappedHospitals: Collection<Hospital>?) {
        val hospitals = ArrayList<Hospital>(mappedHospitals)
        //Clear the map before placing new markers to avoid duplicate markers
        googleMap.clear()
        markers = mutableListOf()

        //Add the markers
        hospitals.forEach { hospital ->
            if (hospital.location?.latitude != null || hospital.location?.longitude != null) {
                val markerOptions = MarkerOptions().position(LatLng(hospital.location?.latitude!!, hospital.location?.longitude!!)).title(hospital.hospitalName)
                val marker = googleMap.addMarker(markerOptions)
                marker.tag = hospital
                markers.add(marker)
            }
        }

        //Zoom to show all markers and set the subtitle
        zoomMapToShowMarkers()
        val filteredHospitals = hospitals.filter {
            it.location != null &&
                    (it.location?.latitude != -0.00000000199 && it.location?.longitude != -0.00000000199)
        }
        (activity as AppCompatActivity).supportActionBar?.subtitle =
                activity.resources.getQuantityString(R.plurals.showingHospitals, filteredHospitals.size, filteredHospitals.size)
    }

    /**
     * When the info window is clicked, launch the detail fragment.
     *
     * @param p0 the marker the info window is attached to
     */
    override fun onInfoWindowClick(p0: Marker?) {
        viewModel.setSelectedHospital(p0!!.tag as Hospital)

        //We are viewing an existing hospital so newHospital is false
        val hospitalDetailFragment = HospitalDetailFragment.newInstance(false)
        val fragmentTag = activity.getString(R.string.hospital_detail_fragment)
        activity.supportFragmentManager
                .beginTransaction()
                .replace(activity.fragment_container.id, hospitalDetailFragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commit()
    }

    /**
     * Zooms to fit (mostly) all of the markers on the map at once. It has a little trouble when
     * there are 4500+ hospitals.
     */
    private fun zoomMapToShowMarkers() {
        //Build a LatLng bounds for the zoom
        if (markers.size > 0) {
            val builder = LatLngBounds.Builder()
            for (marker in markers) {
                builder.include(marker.position)
            }
            val bounds = builder.build()
            val padding = 200 // offset from edges of the map in pixels

            //Zoom the camera
            val cu = if (markers.size == 1) {
                CameraUpdateFactory.newLatLngZoom(markers[0].position, 15f)
            } else {
                CameraUpdateFactory.newLatLngBounds(bounds, padding)
            }
            googleMap.animateCamera(cu)
        }
    }

    /**
     * A companion object (like a static class) for exposing a newInstance method.
     */
    companion object {

        /**
         * Creates a new instance of this fragment.
         *
         * @return a HospitalMapFragment fragment
         */
        @JvmStatic
        fun newInstance(): HospitalMapFragment = HospitalMapFragment()
    }
}