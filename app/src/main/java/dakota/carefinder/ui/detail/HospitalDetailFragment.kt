package dakota.carefinder.ui.detail

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import dakota.carefinder.R
import dakota.carefinder.data.model.DEFAULT_LOCATION_COORD
import dakota.carefinder.data.model.Hospital
import dakota.carefinder.ui.main.MainActivity
import dakota.carefinder.ui.main.MainHospitalViewModel
import kotlinx.android.synthetic.main.fragment_hospital_detail.*

/**
 * A request code for the PlacePicker
 */
private const val PLACE_PICKER_REQUEST = 1

/**
 * A key for the saving the newHospital boolean in a bundle
 */
private const val NEW_HOSPITAL = "add_new_hospital"

/**
 * Created by dakota on 11/28/17.
 */
class HospitalDetailFragment : Fragment(), OnMapReadyCallback {

    /**
     * The ViewModel to observe data changes from
     */
    private lateinit var viewModel: MainHospitalViewModel

    /**
     * The current hospital to pull data from
     */
    private lateinit var hospital: Hospital

    /**
     * A GoogleMap
     */
    private lateinit var googleMap: GoogleMap

    /**
     * Whether or not the fragment is in edit mode
     */
    private var editable = false

    /**
     * A reference to the options menu
     */
    private lateinit var optionsMenu: Menu

    /**
     * Whether or not the location has been modified
     */
    private var locationModified = false

    /**
     * Whether or not the fragment was created with the newHospital flag set
     */
    private var newHospital: Boolean = false

    /**
     * A TextInputLayout for the hospital name
     */
    private lateinit var hospitalNameInputLayout: TextInputLayout

    /**
     * A TextInputLayout for the hospital provider id
     */
    private lateinit var providerIdInputLayout: TextInputLayout

    /**
     * A TextInputLayout for the hospital location
     */
    private lateinit var hospitalLocationInputLayout: TextInputLayout

    /**
     * A TextInputLayout for the hospital phone number
     */
    private lateinit var phoneNumberInputLayout: TextInputLayout

    /**
     * A TextInputLayout for the hospital type
     */
    private lateinit var hospitalTypeInputLayout: TextInputLayout

    /**
     * A TextInputLayout for the hospital ownership
     */
    private lateinit var hospitalOwnershipInputLayout: TextInputLayout

    /**
     * A Spinner for the has emergency services options
     */
    private lateinit var emergencyServicesOptions: Spinner

    /**
     * A TextView for indicating that no location has been selected
     */
    private lateinit var noLocationTextView: TextView

    /**
     * Sets up the ViewModel and relevant information for the fragment.
     *
     * @param savedInstanceState a bundle from a previous state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity).get(MainHospitalViewModel::class.java)
        locationModified = false

        //Get the selected hospital
        hospital = viewModel.getSelectedHospital()!!

        //Check if this is an edit or a new hospital
        newHospital = arguments.getBoolean(NEW_HOSPITAL)
        setHasOptionsMenu(true)

        //Set the subtitle
        (activity as AppCompatActivity).supportActionBar?.subtitle = null
    }

    /**
     * Save whether or not the fragment is in edit mode and whether or not the location has been
     * modified.
     *
     * @param outState a bundle to save the state in
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean("EDITABLE", editable)
        outState?.putBoolean("LOCATION_MODIFIED", locationModified)
        super.onSaveInstanceState(outState)
    }

    /**
     * Restore relevant information contained within the savedInstanceStateBundle.
     *
     * @param savedInstanceState a bundle from a previous state
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            editable = savedInstanceState.getBoolean("EDITABLE")
            locationModified = savedInstanceState.getBoolean("LOCATION_MODIFIED")
        }
        super.onViewStateRestored(savedInstanceState)
    }

    /**
     * Handles the creation of the view. Sets up the map.
     *
     * @param inflater layout inflater
     * @param container container ViewGroup
     * @param savedInstanceState bundle from previous state
     * @return a created view
     */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_hospital_detail, container, false)
        val map = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        map.getMapAsync(this)
        return v
    }

    /**
     * When the view is created, setup the input fields.
     *
     * @param view the created view
     * @param savedInstanceState a bundle from a previous state
     */
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Instantiate the InputLayout members
        hospitalNameInputLayout = hospital_name_layout
        providerIdInputLayout = provider_id_layout
        hospitalLocationInputLayout = location_layout
        phoneNumberInputLayout = hospital_phone_layout
        hospitalTypeInputLayout = hospital_type_layout
        hospitalOwnershipInputLayout = hospital_ownership_layout
        emergencyServicesOptions = emergency_services_spinner
        noLocationTextView = no_location_selected_text_view

        //If the hospital is set to the default location value, show the no location text view
        if (hospital.location != null && hospital.location?.latitude == DEFAULT_LOCATION_COORD && hospital.location?.longitude == DEFAULT_LOCATION_COORD) {
            noLocationTextView.visibility = View.VISIBLE
        }

        hospitalNameInputLayout.editText?.setText(hospital.hospitalName, TextView.BufferType.EDITABLE)
        //Sets up a listener for hiding errors when the name input field text changes
        hospitalNameInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { /* Do nothing */ }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /* Do nothing */ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { hospitalNameInputLayout.isErrorEnabled = false }
        })

        providerIdInputLayout.editText?.setText(hospital.providerId, TextView.BufferType.EDITABLE)
        //Sets up a listener for hiding errors when the providerId input field text changes
        providerIdInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { /* Do nothing */ }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /* Do nothing */ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { providerIdInputLayout.isErrorEnabled = false }
        })

        //Set text based on what information is available
        hospitalLocationInputLayout.editText?.setText((
                        if (hospital.address != null) { hospital.address + ", " } else { "" } +
                        if (hospital.city != null) { hospital.city + ", " } else { "" } +
                        if (hospital.state != null) { hospital.state + ", " } else { "" } +
                        if (hospital.zipCode != null) { hospital.zipCode } else { "" })
                        .trim()
                        .trim(','), TextView.BufferType.EDITABLE)

        //Sets up a listener for hiding errors when the phone input field text changes
        phoneNumberInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { /* Do nothing */ }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /* Do nothing */ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { phoneNumberInputLayout.isErrorEnabled = false }
        })

        //Sets up a text changed listener to format phone numbers
        phoneNumberInputLayout.editText?.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        phoneNumberInputLayout.editText?.setText(hospital.phoneNumber, TextView.BufferType.EDITABLE)

        hospitalTypeInputLayout.editText?.setText(hospital.hospitalType, TextView.BufferType.EDITABLE)
        hospitalOwnershipInputLayout.editText?.setText(hospital.hospitalOwnership, TextView.BufferType.EDITABLE)
        hospitalLocationInputLayout.editText?.isFocusable = false

        /*
        Sets the location input field to launch a popup menu to allow the user to edit or clear the
        field. Editing it launches the Google Places API PlacePicker to allow the user to pick a
        location.
         */
        hospitalLocationInputLayout.editText?.setOnClickListener {
            val popup = PopupMenu(context, hospitalLocationInputLayout)
            popup.inflate(R.menu.location_options)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    //Launch the place picker
                    R.id.edit_location -> {
                        val builder = PlacePicker.IntentBuilder()
                        if (hospital.location == null || (hospital.location?.latitude == DEFAULT_LOCATION_COORD && hospital.location?.longitude == DEFAULT_LOCATION_COORD)) {
                            builder.setLatLngBounds(LatLngBounds(LatLng(23.241346102386135, -126.9140625), LatLng(49.38237278700955, -63.6328125)))
                        } else if (hospital.location != null && hospital.location?.latitude != null && hospital.location?.longitude != null) {
                            builder.setLatLngBounds(LatLngBounds(LatLng(hospital.location!!.latitude, hospital.location!!.longitude), LatLng(hospital.location!!.latitude, hospital.location!!.longitude)))
                        }
                        startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST)
                        false
                    }
                    //Clear the location field
                    R.id.clear_location -> {
                        hospitalLocationInputLayout.editText?.text = null
                        hospital.location?.latitude = DEFAULT_LOCATION_COORD
                        hospital.location?.longitude = DEFAULT_LOCATION_COORD
                        hospital.address = null
                        hospital.city = null
                        hospital.state = null
                        hospital.zipCode = null
                        noLocationTextView.visibility = View.VISIBLE
                        googleMap.clear()
                        locationModified = true
                        false
                    }
                    else -> { false /* Do nothing */}
                }
            }
            popup.show()
        }

        //Set up the choices available in the has emergency optinons spinner
        val adapter = ArrayAdapter.createFromResource(activity, R.array.emergency_services_options, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        emergencyServicesOptions.adapter = adapter

        //Set what choice is currently selected in the spinner
        if (hospital.emergencyServices != null && hospital.emergencyServices!!) {
            emergencyServicesOptions.setSelection(2)
        } else if (hospital.emergencyServices != null && !hospital.emergencyServices!!) {
            emergencyServicesOptions.setSelection(3)
        } else {
            emergencyServicesOptions.setSelection(1)
        }

        //If this is a new hospital make sure all fields are active and able to be edited
        if (!newHospital) {
            toggleFieldsEditable(editable)
        }
    }

    /**
     * Sets up the map.
     *
     * @param p0 a GoogleMap
     */
    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0!!
        //Disable touch and the toolbar on the map
        googleMap.uiSettings.setAllGesturesEnabled(false)
        googleMap.uiSettings.isMapToolbarEnabled = false

        //Set the position
        if (hospital.location?.latitude != null || hospital.location?.longitude != null) {
            setMapPosition()
        } else {
            no_location_selected_text_view.visibility = View.VISIBLE
        }
    }

    /**
     * Inflates the options menu.
     *
     * @param menu the menu
     * @param inflater the menu inflater
     */
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.hospital_detail_menu, menu)
        optionsMenu = menu!!
        toggleOptionsEditable(editable)
        //Hide the add button from the activity
        if (menu.findItem(R.id.add) != null && menu.findItem(R.id.add).isVisible) {
            menu.findItem(R.id.add).isVisible = false
        }
        //If it is a new hospital change the save text to submit
        if (newHospital) {
            menu.findItem(R.id.saveSubmit).title = activity.getString(R.string.submit)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Handles actions to perform when a menu item is selected.
     *
     * @param item the menu item selected
     * @return a boolean
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            //When edit is selected, toggle the options editable
            R.id.edit -> {
                editable = true
                toggleOptionsEditable(editable)
            }
            //When save or submit is selected request that the hospital be created or updated
            R.id.saveSubmit -> {
                editable = false
                toggleOptionsEditable(editable)
                if (isValidData() && newHospital) {
                    viewModel.requestCreateHospital(constructHospitalDataForRequest())
                    dismissFragment()
                } else if (isValidData() && !newHospital) {
                    viewModel.requestUpdateReplaceHospital(constructHospitalDataForRequest())
                    dismissFragment()
                }
            }
            //When cancel is selected dismiss the fragment
            R.id.cancel -> {
                dismissFragment()
            }
            //When delete is selected request that the hospital be deleted
            R.id.delete -> {
                viewModel.requestDeleteHospital(hospital)
                dismissFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Toggles the what menu options should be shown and whether or not input fields should be
     * editable based on the boolean provided.
     *
     * @param canEdit whether or not the fields should be editable
     */
    private fun toggleOptionsEditable(canEdit: Boolean) {
        if (newHospital) {
            optionsMenu.findItem(R.id.cancel).isVisible = !canEdit
            optionsMenu.findItem(R.id.saveSubmit).isVisible = !canEdit
            optionsMenu.findItem(R.id.edit).isVisible = canEdit
            toggleFieldsEditable(!canEdit)
        } else {
            optionsMenu.findItem(R.id.edit).isVisible = !canEdit
            optionsMenu.findItem(R.id.saveSubmit).isVisible = canEdit
            optionsMenu.findItem(R.id.delete).isVisible = canEdit
            toggleFieldsEditable(canEdit)
        }
    }

    /**
     * Toggles whether or not the fields should be editable.
     *
     * @param canEdit whether or not the fields should be editable
     */
    private fun toggleFieldsEditable(canEdit: Boolean) {
        hospitalNameInputLayout.editText?.isEnabled = canEdit
        providerIdInputLayout.editText?.isEnabled = canEdit
        hospitalLocationInputLayout.editText?.isEnabled = canEdit
        phoneNumberInputLayout.editText?.isEnabled = canEdit
        hospitalTypeInputLayout.editText?.isEnabled = canEdit
        hospitalOwnershipInputLayout.editText?.isEnabled = canEdit
        emergencyServicesOptions.isEnabled = canEdit
    }

    /**
     * Checks if the name, provider id, and phone number are exist or are valid.
     *
     * @return a boolean for if the data in the fields are valid
     */
    private fun isValidData(): Boolean {
        var validInput = true
        if (isEmpty(hospitalNameInputLayout.editText)) {
            hospitalNameInputLayout.error = activity.getText(R.string.required_field)
            validInput = false
        }
        if (isEmpty(providerIdInputLayout.editText)) {
            providerIdInputLayout.error = activity.getText(R.string.required_field)
            validInput = false
        }
        if (!phoneNumberInputLayout.editText?.text.toString().matches(Regex("(?:\\d{1}\\s)?\\(?(\\d{3})\\)?-?\\s?(\\d{3})-?\\s?(\\d{4})"))
                && !isEmpty(phoneNumberInputLayout.editText)) {
            phoneNumberInputLayout.error = activity.getText(R.string.phone_not_valid)
            validInput = false
        }
        return validInput
    }

    /**
     * Constructs a new hospital for a network request.
     *
     * @return a Hospital for a network request
     */
    private fun constructHospitalDataForRequest(): Hospital {
        val hospitalData = hospital.copy()

        hospitalData.hospitalName = hospitalNameInputLayout.editText?.text.toString()
        hospitalData.providerId = providerIdInputLayout.editText?.text.toString()
        hospitalData.phoneNumber = phoneNumberInputLayout.editText?.text.toString()

        //If the location was modified update it
        if (locationModified) {
            val address = splitAddress(hospital_location_text_input.text.toString())
            if (address != null) {
                hospitalData.address = address[0]
                hospitalData.city = address[1]
                hospitalData.state = address[2]
                hospitalData.zipCode = address[3]
            }
            //If the location data is the default values set the location to null
        } else if (hospitalData.location?.latitude == DEFAULT_LOCATION_COORD && hospitalData.location?.longitude == DEFAULT_LOCATION_COORD) {
            hospitalData.location = null
        }

        hospitalData.phoneNumber = setNullIfEmpty(phoneNumberInputLayout.editText?.text.toString())
        hospitalData.hospitalType = setNullIfEmpty(hospitalTypeInputLayout.editText?.text.toString())
        hospitalData.hospitalOwnership = setNullIfEmpty(hospitalOwnershipInputLayout.editText?.text.toString())

        //Get the options from the spinner for if the hospital has emergency services
        when (emergencyServicesOptions.selectedItemPosition) {
            2 -> hospitalData.emergencyServices = true
            3 -> hospitalData.emergencyServices = false
            else -> hospitalData.emergencyServices = null
        }
        return hospitalData
    }

    /**
     * Get the data returned from the PlacePicker and updates the hospital with the information.
     *
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the data returned
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(activity, data)
                //Split the address into its components
                val addressComponents = splitAddress(place.address.toString())

                //Check if the address is valid
                if (addressComponents == null) {
                    val toastMsg = String.format(activity.getString(R.string.address_not_valid))
                    Toast.makeText(activity, toastMsg, Toast.LENGTH_LONG).show()
                } else {
                    locationModified = true
                    if (noLocationTextView.visibility == View.VISIBLE) {
                        noLocationTextView.visibility = View.GONE
                    }
                    //Update the location field and the map
                    hospitalLocationInputLayout.editText?.setText(place.address, TextView.BufferType.EDITABLE)
                    hospital.location?.latitude = place.latLng.latitude
                    hospital.location?.longitude = place.latLng.longitude
                    googleMap.clear()
                    setMapPosition()
                }
            }
        }
    }

    /**
     * Method breaks apart a string address into its components provided it is in the valid format.
     * Must match format of:  'street address, city or county, state zip-code, country. Google
     * Places PlacePicker doesn't have a better way to get this info, so this will have to do. It
     * works, but I don't like it.
     *
     * Return array is null if it was not in the valid format. Otherwise it follows the below schema:
     * arr[0] = street address
     * arr[1] = city/county/town/etc
     * arr[2] = state
     * arr[3] = zip code
     *
     * @param address the address to split
     * @return an array of strings containing address components
     */
    private fun splitAddress(address: String): Array<String>? {
        val components = address.split(", ")
        if (components.size != 4) {
            return null
        } else {
            var state = ""
            var zipCode = ""
            val tempArr = components[2].split(" ")
            if (tempArr.size != 2) {
                return null
            }
            if (!tempArr[1].matches(Regex("[0-9]{5}(-[0-9]{4})?"))) {
                return null
            }
            tempArr.forEachIndexed { index, i ->
                if (index == tempArr.size - 1) {
                    zipCode = i
                } else {
                    state += "$i "
                }
            }
            state = state.trim()
            return arrayOf(components[0], components[1], state, zipCode)
        }
    }

    /**
     * Sets the map position to the hospital location.
     */
    private fun setMapPosition() {
        val hospitalLatLng = LatLng(hospital.location?.latitude!!, hospital.location?.longitude!!)
        googleMap.addMarker(MarkerOptions().position(hospitalLatLng).title(hospital.hospitalName))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hospitalLatLng, 15f))
    }

    /**
     * Checks if the provided EditText is empty.
     *
     * @param editText the EditText to check
     * @return a boolean for if the EditText is empty
     */
    private fun isEmpty(editText: EditText?) = editText?.text.toString().trim { it <= ' ' }.isEmpty()

    /**
     * Helper method for setting a string to null if it is empty.
     *
     * @param string a string to check if it is empty
     * @return null or a string if it isn't empty
     */
    private fun setNullIfEmpty(string: String): String? = when (string) { "" -> { null } else -> { string }}

    /**
     * Dismisses this fragment.
     */
    private fun dismissFragment() {
        MainActivity.hideKeyboard(activity)
        activity.supportFragmentManager.beginTransaction().remove(this).commit()
        activity.supportFragmentManager.popBackStack()
    }

    /**
     * A companion object (like a static class) for exposing a newInstance method.
     */
    companion object {
        /**
         * Creates a new instance of this fragment.
         *
         * @return a HospitalDetailFragment fragment
         */
        @JvmStatic
        fun newInstance(newHospital: Boolean): HospitalDetailFragment {
            val bundle = Bundle()
            if (newHospital) {
                bundle.putBoolean(NEW_HOSPITAL, newHospital)
            } else {
                bundle.putBoolean(NEW_HOSPITAL, false)
            }
            val fragment = HospitalDetailFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}