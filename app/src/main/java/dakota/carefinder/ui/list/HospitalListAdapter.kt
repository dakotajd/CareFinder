package dakota.carefinder.ui.list

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import dakota.carefinder.R
import dakota.carefinder.data.model.Hospital
import dakota.carefinder.ui.detail.HospitalDetailFragment
import dakota.carefinder.ui.main.MainHospitalViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by dakota on 12/8/17.
 */
class HospitalListAdapter(private val hospitals: List<Hospital>?, private val viewModel: MainHospitalViewModel, private val activity: Activity) : RecyclerView.Adapter<HospitalListAdapter.HospitalViewHolder>() {

    /**
     * Handles the binding of the data to their views.
     *
     * @param holder a ViewHolder
     * @param position a position to bind at
     */
    override fun onBindViewHolder(holder: HospitalViewHolder?, position: Int) {
        if (hospitals != null) {
            val hospital = hospitals[position]
            holder?.hospitalName?.text = hospital.hospitalName
            val address = constructAddress(hospital)
            if (address == "") {
                holder?.hospitalAddress?.text = (activity as AppCompatActivity).getString(R.string.no_address_available)
            } else {
                holder?.hospitalAddress?.text = constructAddress(hospital)
            }
            if (hospital.phoneNumber != null) {
                @Suppress("DEPRECATION")
                holder?.hospitalPhone?.text = PhoneNumberUtils.formatNumber(hospital.phoneNumber)
            }
            if (position == hospitals.size - 1) {
                holder?.divider?.visibility = View.GONE
            }
            //Set up a click listener to launch the detail fragment onClick
            holder?.container?.setOnClickListener {
                viewModel.setSelectedHospital(hospital)
                val hospitalDetailFragment = HospitalDetailFragment.newInstance(false)
                val fragmentTag = activity.getString(R.string.hospital_detail_fragment)
                (activity as AppCompatActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(activity.fragment_container.id, hospitalDetailFragment, fragmentTag)
                        .addToBackStack(fragmentTag)
                        .commit()
            }
        }
    }

    /**
     * Handles the creation of the ViewHolders.
     *
     * @param parent the parent ViewGroup
     * @param viewType the viewType
     * @return a HospitalViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.row_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    /**
     * Gets the count of items in the data set.
     *
     * @return the count
     */
    override fun getItemCount(): Int {
        return hospitals?.size ?: 0
    }

    /**
     * Constructs a textual address from a hospital's split address fields.
     *
     * @param hospital the hospital to construct the address from
     * @return a string representation of a properly formatted postal address
     */
    private fun constructAddress(hospital: Hospital): String {
        return (if (hospital.address != null) { hospital.address + ", " } else { "" } +
                if (hospital.city != null) { hospital.city + ", " } else { "" } +
                if (hospital.state != null) { hospital.state + ", " } else { "" } +
                if (hospital.zipCode != null) { hospital.zipCode } else { "" })
                .trim()
                .trim(',')
    }

    /**
     * A ViewHolder for each row.
     */
    class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * TextView for the hospital name.
         */
        var hospitalName: TextView = itemView.findViewById(R.id.hospital_name)

        /**
         * TextView for the hospital address.
         */
        var hospitalAddress: TextView = itemView.findViewById(R.id.hospital_address)

        /**
         * TextView for the hospital phone.
         */
        var hospitalPhone: TextView = itemView.findViewById(R.id.hospital_phone)

        /**
         * View for the item divider.
         */
        var divider: View = itemView.findViewById(R.id.divider)

        /**
         * The container for the row item.
         */
        var container: ConstraintLayout = itemView.findViewById(R.id.hospital_row_container)
    }
}