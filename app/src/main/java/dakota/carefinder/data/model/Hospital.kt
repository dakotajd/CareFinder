package dakota.carefinder.data.model

import java.util.*

/**
 * Created by dakota on 11/27/17.
 */

const val DEFAULT_LOCATION_COORD = -0.00000000199
/**
 * A class to model a Hospital.
 */
data class Hospital(
        var _id: String = "",
        var providerId: String = "",
        var hospitalName: String = "",
        var address: String? = null,
        var city: String? = null,
        var state: String? = null,
        var zipCode: String? = null,
        var countyName: String? = null,
        var phoneNumber: String? = null,
        var hospitalType: String? = null,
        var hospitalOwnership: String? = null,
        var emergencyServices: Boolean? = null,
        //Default location values are arbitrary
        var location: Location? = Location(DEFAULT_LOCATION_COORD, DEFAULT_LOCATION_COORD),
        var dateCreated: Date = Date(),
        var dateModified: Date = Date())