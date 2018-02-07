package dakota.carefinder.ui.search

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import dakota.carefinder.R
import dakota.carefinder.ui.main.MainActivity
import dakota.carefinder.ui.main.MainHospitalViewModel
import dakota.carefinder.data.model.NetworkMethod.*

/**
 * Tag for the search option
 */
const val SEARCH_OPTION = "choice"

/**
 * Tag for the search query
 */
const val SEARCH_QUERY = "query"

/**
 * Created by dakota on 11/28/17.
 */
class SearchDialog : DialogFragment() {

    /**
     * The ViewModel to observe data changes from
     */
    private lateinit var viewModel: MainHospitalViewModel

    /**
     * The current search option
     */
    private var option: Int = 0

    /**
     * The current search query
     */
    private var query: String? = null

    /**
     * A boolean to track whether or not the spinner has been initialized
     */
    private var spinnerInit = true

    /**
     * Set up the ViewModel, search option, and search query.
     *
     * @param savedInstanceState a bundle from a previous state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity as AppCompatActivity).get(MainHospitalViewModel::class.java)
        option = arguments.getInt(SEARCH_OPTION)
        query = arguments.getString(SEARCH_QUERY)
    }

    /**
     * Create the dialog and setup the input field, spinner, and buttons.
     *
     * @param savedInstanceState a bundle from a previous state
     * @return a dialog
     */
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity.layoutInflater.inflate(R.layout.dialog_search, null)
        val searchOptions = view.findViewById<Spinner>(R.id.search_options_spinner)
        //Get the search option strings
        val adapter = ArrayAdapter.createFromResource(activity, R.array.search_options, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        searchOptions.adapter = adapter

        val queryTextInputLayout = view.findViewById<TextInputLayout>(R.id.search_text_input_layout)

        //Set the spinner to disable the input when option 0 (search all hopsitals) is chosen
        searchOptions.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                //Do nothing
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == GetAll.option) {
                    queryTextInputLayout.isEnabled = false
                } else if (!queryTextInputLayout.isEnabled) {
                    queryTextInputLayout.isEnabled = true
                }
                if (!spinnerInit) {
                    queryTextInputLayout.editText?.text = null
                }
                spinnerInit = false
            }

        }
        searchOptions.setSelection(option)
        queryTextInputLayout.editText?.setText(query, TextView.BufferType.EDITABLE)

        //Disable hide visible errors when the text changes in the input
        queryTextInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { /* Do nothing */ }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { /* Do nothing */ }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                queryTextInputLayout.isErrorEnabled = false
            }
        })

        //Build the dialog
        val dialog = AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, { _, _ -> dismiss() })
                .setTitle(R.string.search_for_hospitals)
                .create()

        //Override the onClickListener of the positive button to stop it from auto dismissing
        dialog.setOnShowListener {
            val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positive.setOnClickListener {
                //Validate the search
                if (validateSearch(searchOptions.selectedItemPosition, queryTextInputLayout.editText?.text.toString())) {
                    MainActivity.search(searchOptions.selectedItemPosition, queryTextInputLayout.editText?.text.toString(), viewModel, activity)
                    dismiss()
                } else {
                    queryTextInputLayout.error = activity.getString(R.string.valid_city_state)
                }
            }
        }

        return dialog
    }

    /**
     * Validates that search option 4's query (get hospitals by city state) matches the required.
     * regex.
     *
     * @param option the search option
     * @param query the search query
     * @return a boolean for if the query matches the regex
     */
    private fun validateSearch(option: Int, query: String): Boolean {
        return if (option == GetByCityState.option) {
            query.matches(Regex("[\\da-zA-Z\\s]*,\\s?[\\da-zA-Z\\s]*"))
        } else {
            true
        }
    }

    /**
     * A companion object for exposing a newInstance method.
     */
    companion object {
        /**
         * Creates a new instance of this fragment.
         *
         * @param option the search option
         * @param query the search query
         * @return a SearchDialog fragment
         */
        @JvmStatic
        fun newInstance(option: Int, query: String?): SearchDialog {
            val bundle = Bundle()
            bundle.putInt(SEARCH_OPTION, option)
            bundle.putString(SEARCH_QUERY, query)
            val fragment = SearchDialog()
            fragment.arguments = bundle
            return fragment
        }
    }


}