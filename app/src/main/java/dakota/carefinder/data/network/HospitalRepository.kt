package dakota.carefinder.data.network

import dakota.carefinder.data.model.Hospital
import io.reactivex.Single

/**
 * Created by dakota on 11/27/17.
 */
interface HospitalRepository {

    fun getAllHospitals() : Single<List<Hospital>?>
    fun getHospitalsByName(name: String) : Single<List<Hospital>?>
    fun getHospitalsByCity(city: String) : Single<List<Hospital>?>
    fun getHospitalsByState(state: String) : Single<List<Hospital>?>
    fun getHospitalByCityState(city: String, state: String) : Single<List<Hospital>?>
    fun getHospitalByCounty(county: String) : Single<List<Hospital>?>

    fun createHospital(hospital: Hospital) : Single<List<Hospital>?>
    fun deleteHospital(hospital: Hospital) : Single<List<Hospital>?>
    fun updateReplaceHospital(hospital: Hospital) : Single<List<Hospital>?>
}