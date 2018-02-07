package dakota.carefinder.ui.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dakota.carefinder.R
import dakota.carefinder.data.model.Hospital
import dakota.carefinder.ui.main.MainHospitalViewModel

/**
 * Created by dakota on 12/8/17.
 */
class HospitalListFragment : Fragment() {

    /**
     * The ViewModel to observe data changes from
     */
    private lateinit var viewModel: MainHospitalViewModel

    /**
     * Set up the ViewModel.
     *
     * @param savedInstanceState a bundle from a previous state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity).get(MainHospitalViewModel::class.java)
    }

    /**
     * Inflate the view, setup the RecyclerView, and observe hospitals from the ViewModel.
     *
     * @param inflater layout inflater
     * @param container container ViewGroup
     * @param savedInstanceState bundle from previous state
     * @return a created view
     */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_hospital_list, container, false)
        val recyclerView = v?.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = HospitalListAdapter(viewModel.getCurrentHospitals().value?.values?.toList()?.sortedBy { it.hospitalName }, viewModel, activity)

        //Observe changes from the ViewModel and update the UI accordingly
        viewModel.getCurrentHospitals().observe(this, Observer<MutableMap<String, Hospital>> { hospitals ->
            recyclerView?.adapter = HospitalListAdapter(hospitals?.values?.toList()?.sortedBy { it.hospitalName }, viewModel, activity)
            recyclerView?.adapter?.notifyDataSetChanged()
        })
        return v
    }

    /**
     * A companion object (like a static class) to expose a newInstance method.
     */
    companion object {
        /**
         * Creates a new instance of this fragment.
         *
         * @return a HospitalListFragment fragment
         */
        @JvmStatic
        fun newInstance(): HospitalListFragment = HospitalListFragment()
    }
}