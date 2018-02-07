package dakota.carefinder.data.model

import com.github.kittinunf.fuel.core.FuelError

/**
 * Created by dakota on 12/4/17.
 */

/**
 * A class for packaging information about a network response.
 */
data class ObservableFuelResponse<out T>(val success: Boolean, var error: FuelError?, val method: NetworkMethod?, val t: T?, val size: Int?)