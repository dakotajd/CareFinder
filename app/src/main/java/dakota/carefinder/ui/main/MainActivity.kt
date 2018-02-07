package dakota.carefinder.ui.main

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.wang.avi.AVLoadingIndicatorView
import dakota.carefinder.R
import dakota.carefinder.data.model.Hospital
import dakota.carefinder.data.model.NetworkMethod.*
import dakota.carefinder.data.model.ObservableFuelResponse
import dakota.carefinder.ui.detail.HospitalDetailFragment
import dakota.carefinder.ui.search.SEARCH_OPTION
import dakota.carefinder.ui.search.SEARCH_QUERY
import dakota.carefinder.ui.search.SearchDialog
import dakota.carefinder.ui.tabs.TabFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    /**
     * The ViewModel to observe data changes from
     */
    private lateinit var viewModel: MainHospitalViewModel

    /**
     * Coordinator layout for the activity
     */
    private lateinit var coordinatorLayout: CoordinatorLayout

    /**
     * Floating action button for initiating searches
     */
    private lateinit var fab: FloatingActionButton

    /**
     * A reference to the current response (used to avoid showing duplicate error/success messages)
     */
    private var currentResponse: ObservableFuelResponse<Hospital>? = null

    /**
     * A loading indicator
     */
    private lateinit var loadingIndicator: AVLoadingIndicatorView

    /**
     * Set up the floating action button, observe the response LiveData from the ViewModel and open
     * the root tab fragment.
     *
     * @param savedInstanceState a bundle from a previous state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingIndicator = findViewById(R.id.loading_indicator)

        fab = floating_action_button
        fab.setOnClickListener {
            showSearchFragment()
        }

        supportActionBar?.elevation = 0f

        //Set up the ViewModel and observe for responses from the ViewModel
        viewModel = ViewModelProviders.of(this).get(MainHospitalViewModel::class.java)

        viewModel.getCurrentResponse().observe(this, Observer<ObservableFuelResponse<Hospital>> { response ->
            if (currentResponse !== null && currentResponse !== response) {
                handleError(response)
            }
            currentResponse = response
        })

        coordinatorLayout = coordinator_layout

        //Open the root tab fragment
        if (savedInstanceState == null) {
            val hospitalMapFragment = TabFragment.newInstance()
            val ft = supportFragmentManager.beginTransaction()
            ft.add(fragment_container.id, hospitalMapFragment).commit()
        }

        //Set up a back stack listener to show the back button when the detail fragment opens
        supportFragmentManager.addOnBackStackChangedListener(this)
        onBackStackChanged()
    }

    /**
     * Inflates the options menu.
     *
     * @param menu the menu to inflate
     * @return a boolean
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_hospital, menu)
        return true
    }

    /**
     * Handles the popping from the back stack when the home (back) button is pressed and launches
     * the detail fragment when the add button is pressed.
     *
     * @param item a menu item that is selected
     * @return a boolean
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                dismissDetailFragment()
                false
            }
            R.id.add -> {
                viewModel.setSelectedHospital(Hospital())
                val hospitalDetailFragment = HospitalDetailFragment.newInstance(true)
                val fragmentTag = getString(R.string.hospital_detail_fragment)
                supportFragmentManager
                        .beginTransaction()
                        .replace(fragment_container.id, hospitalDetailFragment, fragmentTag)
                        .addToBackStack(fragmentTag)
                        .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Handles popping the back stack when the hardware button is pressed and the detail fragment is
     * open.
     *
     * @param keyCode an int for the keyCode
     * @param event the keyEvent
     * @return a boolean
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.repeatCount == 0) {
            if (dismissDetailFragment()) {
                return true
            }
            return super.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * When the back stack changes, check to see if the FAB should be hid and whether or not the
     * back button should be shown.
     */
    override fun onBackStackChanged() {
        val hospitalDetailFragment: Fragment? = supportFragmentManager.findFragmentByTag(getString(R.string.hospital_detail_fragment))
        shouldDisplayHomeUp(fragment = hospitalDetailFragment)
        shouldShowHideFab(fragment = hospitalDetailFragment)
    }

    /**
     * Enable home up (back) button only if there are entries in the back stack for the fragment
     * provided.
     *
     * @param fragment a fragment to check if it exists for showing the home up button
     */
    private fun shouldDisplayHomeUp(fragment: Fragment?) {
        var canBack = false
        if (fragment != null) {
            canBack = true
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(canBack)
    }

    /**
     * Shows or hides the FAB based on whether or not the fragment parameter exists.
     *
     * @param fragment a fragment to check if it exists
     */
    private fun shouldShowHideFab(fragment: Fragment?) {
        if (fragment == null) {
            fab.show()
        } else {
            fab.hide()
        }
    }

    /**
     * Handles responses that come in when a network request is made. SnackBars are shown to show
     * the appropriate message for a response.
     *
     * @param response a response to show an error for
     */
    private fun handleError(response: ObservableFuelResponse<Hospital>?) {
        loadingIndicator.smoothToHide()
        //On success show a success SnackBar for the appropriate action performed
        if (response?.success != null && response.success) {
            when (response.method) {
                GetAll, GetByCity, GetByState, GetById, GetByName, GetByCounty, GetByCityState -> {
                    showInfoSnackBar(resources.getQuantityString(R.plurals.hospitalsFound, response.size ?: 0, response.size ?: 0))
                }
                CreateHospital -> showInfoSnackBar(R.string.hospital_created)
                UpdateModify, UpdateReplace -> showInfoSnackBar(R.string.hospital_updated)
                DeleteHospital -> showInfoSnackBar(R.string.hospital_deleted)
                else -> { /* Do nothing */
                }
            }
        } else {
            //On error show an error SnackBar for the appropriate action performed
            when (response?.error?.response?.statusCode) {
                400 -> {
                    showInfoSnackBar(R.string.network_error)
                }
                404 -> {
                    when (response.method) {
                        GetAll -> showInfoSnackBar(R.string.no_hospitals_found)
                        GetById, GetByCity, GetByState, GetByCityState, GetByCounty, GetByName -> showInfoSnackBar(R.string.no_hospitals_found_param)
                        else -> showInfoSnackBar(R.string.network_error)
                    }
                }
                409 -> {
                    when (response.method) {
                        CreateHospital -> {
                            if (response.t != null) {
                                showRetrySnackBar(R.string.error_creating_hospital, response.t)
                            }
                        }
                        UpdateReplace, UpdateModify -> {
                            if (response.t != null) {
                                showRetrySnackBar(R.string.error_updating_hospital, response.t)
                            }
                        }
                        else -> {
                            if (response.t != null) {
                                showRetrySnackBar(R.string.error_executing_request, response.t)
                            }
                        }
                    }
                }
                else -> {
                    if (response?.method != null) {
                        showInfoSnackBar(R.string.network_error)
                    }
                }
            }
        }
    }

    /**
     * Shows an informational SnackBar with the message provided with a 'Dismiss' button.
     *
     * @param messageRes string resource for the message
     */
    private fun showInfoSnackBar(messageRes: Int) {
        val snackBar = Snackbar.make(coordinatorLayout, messageRes, Snackbar.LENGTH_LONG)
        snackBar.setAction(R.string.dismiss, { snackBar.dismiss() }).show()
    }

    /**
     * Shows an informational SnackBar with the message provided with a 'Dismiss' button.
     *
     * @param message string for the message
     */
    private fun showInfoSnackBar(message: String) {
        val snackBar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
        snackBar.setAction(R.string.dismiss, { snackBar.dismiss() }).show()
    }

    /**
     * Shows a retry SnackBar with the message provided. The SnackBar includes a 'Retry' button that
     * when pressed opens the detail fragment with the information filled in for a hospital so a
     * user can edit the data and retry their request.
     *
     * @param messageRes string resource for the message
     * @param hospital hospital data for retrying
     */
    private fun showRetrySnackBar(messageRes: Int, hospital: Hospital) {
        val snackBar = Snackbar.make(coordinatorLayout, messageRes, Snackbar.LENGTH_LONG)
        snackBar.setAction(R.string.retry, {
            viewModel.setSelectedHospital(hospital)
            val hospitalDetailFragment = HospitalDetailFragment.newInstance(true)
            val ft = supportFragmentManager.beginTransaction()
            ft.add(fragment_container.id, hospitalDetailFragment).commit()
            snackBar.dismiss()
        }).show()
    }

    /**
     * Shows the search fragment with the previous query/choice data that was used.
     */
    private fun showSearchFragment() {
        //Load previous query choices
        val (option, query) = MainActivity.readQueryFromSharedPreferences(this)
        val searchDialog = SearchDialog.newInstance(option, query)
        searchDialog.show(fragmentManager, "")
    }

    /**
     * Dismisses the detail fragment.
     *
     * @return a boolean for when the fragment is dismissed
     */
    private fun dismissDetailFragment(): Boolean {
        val hospitalDetailFragment: Fragment? = supportFragmentManager.findFragmentByTag(getString(R.string.hospital_detail_fragment))
        if (hospitalDetailFragment != null) {
            MainActivity.hideKeyboard(this)
            supportFragmentManager.beginTransaction().remove(hospitalDetailFragment).commit()
            supportFragmentManager.popBackStack()
            return true
        }
        return false
    }

    /**
     * A companion object (like a static class) that holds static methods for initiating network requests based on the
     * search query and search choice. Methods that save/read queries/choices to/from shared
     * preferences are also included.
     */
    companion object {
        /**
         * Performs a network call with the search provided based on the choice that was chosen in
         * the search dialog.
         *
         * @param option the search option chosen
         * @param query the query
         * @param viewModel a reference to the viewModel for making requests
         * @param activity a reference to an activity context for accessing shared preferences
         */
        @JvmStatic
        fun search(option: Int, query: String, viewModel: MainHospitalViewModel, activity: Activity) {
            when (option) {
                GetAll.option -> viewModel.requestAllHospitals()
                GetByName.option -> viewModel.requestHospitalsByName(query)
                GetByCity.option -> viewModel.requestHospitalsByCity(query)
                GetByState.option -> viewModel.requestHospitalsByState(query)
                GetByCityState.option -> {
                    val params = query.split(",").map { it.trim() }
                    viewModel.requestHospitalsByCityState(params[0], params[1])
                }
                GetByCounty.option -> viewModel.requestHospitalsByCounty(query)
            }
            saveQueryToSharedPreferences(option, query, activity)
            //(activity as MainActivity).loadingIndicator.visibility = View.VISIBLE
            (activity as MainActivity).loadingIndicator.smoothToShow()
        }

        /**
         * Saves the previous search option/query from shared preferences.
         *
         * @param option the search option
         * @param query the search query
         * @param context a reference to a context for accessing shared preferences
         */
        @JvmStatic
        private fun saveQueryToSharedPreferences(option: Int, query: String?, context: Context) {
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
            prefs.putInt(SEARCH_OPTION, option)
            prefs.putString(SEARCH_QUERY, query)
            prefs.apply()
        }

        /**
         * Reads the previous search option/query from shared preferences.
         *
         * @param context a reference to a context for accessing shared preferences
         * @return a pair of values containing the option/query
         */
        @JvmStatic
        fun readQueryFromSharedPreferences(context: Context): Pair<Int, String> {
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val option = prefs.getInt(SEARCH_OPTION, 0)
            val query = prefs.getString(SEARCH_QUERY, null)
            return Pair(option, query)
        }

        @JvmStatic
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
