package dakota.carefinder.ui.tabs

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import dakota.carefinder.ui.list.HospitalListFragment
import dakota.carefinder.ui.map.HospitalMapFragment

/**
 * Created by dakota on 12/8/17.
 */
class FragmentPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    /**
     * Returns the correct fragment for a position.
     *
     * @return a fragment
     */
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HospitalMapFragment.newInstance()
            1 -> HospitalListFragment.newInstance()
            else -> Fragment()
        }
    }

    /**
     * Gets the count of items in the adapter.
     *
     * @return the count of items in the adapter
     */
    override fun getCount(): Int {
        return 2
    }
}