package dakota.carefinder.ui.tabs

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dakota.carefinder.R
import dakota.carefinder.ui.main.MainHospitalViewModel

/**
 * Created by dakota on 12/8/17.
 */
class TabFragment : Fragment() {

    /**
     * The ViewModel to observe data changes from
     */
    private lateinit var viewModel: MainHospitalViewModel

    /**
     * Connect to the ViewModel onCreate.
     *
     * @param savedInstanceState bundle from previous state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity).get(MainHospitalViewModel::class.java)
    }

    /**
     * Set up the ViewPager and the tabs.
     *
     * @param inflater the layout inflater
     * @param container the parent ViewGroup
     * @param savedInstanceState a bundle from a previous state
     * @return a view
     */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_tabs, container, false)
        val viewPager = v?.findViewById<ViewPager>(R.id.view_pager)

        val tabLayout = v?.findViewById<TabLayout>(R.id.tab_layout)
        viewPager?.adapter = FragmentPagerAdapter(childFragmentManager)
        tabLayout?.setupWithViewPager(viewPager)
        tabLayout?.getTabAt(0)?.setText(R.string.hospital_map)
        tabLayout?.getTabAt(1)?.setText(R.string.hospital_list)

        //Tab listener for altering the subtitle to be accurate depending on the fragment thats open
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) { /* Do nothing */ }
            override fun onTabUnselected(tab: TabLayout.Tab?) { /* Do nothing */ }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    val hospitals = viewModel.getCurrentHospitals().value?.values?.filter {
                        it.location != null &&
                                (it.location?.latitude != -0.00000000199 &&
                                it.location?.longitude != -0.00000000199)
                    }
                    (activity as AppCompatActivity).supportActionBar?.subtitle =
                            activity.resources.getQuantityString(R.plurals.showingHospitals, hospitals?.size ?: 0, hospitals?.size ?: 0)
                } else if (tab?.position == 1) {
                    val hospitals = viewModel.getCurrentHospitals().value?.values
                    (activity as AppCompatActivity).supportActionBar?.subtitle =
                            activity.resources.getQuantityString(R.plurals.showingHospitals, hospitals?.size ?: 0, hospitals?.size ?: 0)
                }
            }
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
         * @return a TabFragment fragment
         */
        @JvmStatic
        fun newInstance(): TabFragment = TabFragment()
    }
}