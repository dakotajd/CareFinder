package dakota.carefinder.ui.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.github.kittinunf.fuel.core.FuelError
import dakota.carefinder.data.model.Hospital
import dakota.carefinder.data.model.NetworkMethod
import dakota.carefinder.data.model.NetworkMethod.*
import dakota.carefinder.data.model.ObservableFuelResponse
import dakota.carefinder.data.network.HospitalRepository
import dakota.carefinder.data.network.NetworkHospitalRepository
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by dakota on 11/27/17.
 */
class MainHospitalViewModel : ViewModel() {

    /**
     * A MutableLiveData to hold the hospitals to observe from
     */
    private var hospitals: MutableLiveData<MutableMap<String, Hospital>>

    /**
     * An instance of the hospital repository for making network calls
     */
    private val hospitalRepository: HospitalRepository

    /**
     * A hospital reference for keeping track of when a hospital is selected to be viewed/edited
     */
    private var selectedHospital: Hospital? = null

    /**
     * A composite (like a list) of disposables for keeping track of pending or in progress
     * operations
     */
    private var disposables: CompositeDisposable = CompositeDisposable()

    /**
     * A MutableLiveData to hold response from network calls to be passed onto the MainActivity for
     * appropriate handling
     */
    private var response: MutableLiveData<ObservableFuelResponse<Hospital>>

    /**
     * Set up the various members
     */
    init {
        hospitalRepository = NetworkHospitalRepository()
        hospitals = MutableLiveData()
        hospitals.value = mutableMapOf()
        response = MutableLiveData()
        response.value = ObservableFuelResponse(false, null, null, null, null)
    }

    /**
     * Gets the current MutableLiveData of hospitals to be observed from.
     *
     * @return the hospitals contained in MutableLiveData
     */
    fun getCurrentHospitals(): MutableLiveData<MutableMap<String, Hospital>> {
        return hospitals
    }

    /**
     * Sets the current hospitals in the MutableLiveData thus triggering an update for all
     * observers.
     *
     * @param hospitals a list of hospitals
     */
    private fun setCurrentHospitals(hospitals: List<Hospital>?) {
        this.hospitals.value = hospitals?.associateBy({ it._id }, { it })?.toMutableMap()
    }

    /**
     * Gets the selected hospital if there is one.
     *
     * @return a hospital
     */
    fun getSelectedHospital(): Hospital? {
        return selectedHospital
    }

    /**
     * Sets the selected hospital.
     *
     * @param hospital the selected hospital
     */
    fun setSelectedHospital(hospital: Hospital) {
        selectedHospital = hospital
    }

    /**
     * Updates a hospital in the current MutableLiveData container of hospitals thus triggering
     * an update for all observers.
     *
     * @param id the _id of the hospital to update
     * @param hospital the hospital to update
     */
    private fun updateHospitalInCurrentHospitals(id: String, hospital: Hospital) {
        hospitals.value?.set(id, hospital)
        hospitals.value = hospitals.value
    }

    /**
     * Gets the current response contained within a MutableLiveData so it can be observed.
     *
     * @return a MutableLiveData container with a response
     */
    fun getCurrentResponse(): MutableLiveData<ObservableFuelResponse<Hospital>> {
        return response
    }

    /**
     * Sets the current response inside the MutableLiveData container.
     *
     * @param response the response
     */
    private fun setCurrentResponse(response: ObservableFuelResponse<Hospital>?) {
        this.response.value = response
    }

    /**
     * Calls a method to perform a network request to get all of the hospitals.
     */
    fun requestAllHospitals() {
        performGetHospitalOperation(hospitalRepository.getAllHospitals(), GetAll)
    }

    /**
     * Calls a method to perform a network request to get all of the hospitals in the specified state.
     *
     * @param state the state to get hospitals from
     */
    fun requestHospitalsByState(state: String) {
        performGetHospitalOperation(hospitalRepository.getHospitalsByState(state), GetByState)
    }

    /**
     * Calls a method to perform a network request to get hospitals with the specified name.
     *
     * @param name the name of the hospital(s)
     */
    fun requestHospitalsByName(name: String) {
        performGetHospitalOperation(hospitalRepository.getHospitalsByName(name), GetByName)
    }

    /**
     * Calls a method to perform a network request to get all of the hospitals in a specified city.
     *
     * @param city the name of the city to get hospitals from
     */
    fun requestHospitalsByCity(city: String) {
        performGetHospitalOperation(hospitalRepository.getHospitalsByCity(city), GetByCity)
    }

    /**
     * Calls a method to perform a network request to get all of the hospitals in a specified county.
     *
     * @param county the name of the city to get hospitals from
     */
    fun requestHospitalsByCounty(county: String) {
        performGetHospitalOperation(hospitalRepository.getHospitalByCounty(county), GetByCounty)
    }

    /**
     * Calls a method to perform a network request to get all of the hospitals in a specified city, state.
     *
     * @param city the city to get hospitals from
     * @param state the state to get hospitals from
     */
    fun requestHospitalsByCityState(city: String, state: String) {
        performGetHospitalOperation(hospitalRepository.getHospitalByCityState(city, state), GetByCityState)
    }

    /**
     * Calls a method to perform a network request to create a hospital.
     *
     * @param hospital the hospital to create
     */
    fun requestCreateHospital(hospital: Hospital) {
        performHospitalCreateOperation(hospitalRepository.createHospital(hospital), CreateHospital, hospital)
    }

    /**
     * Calls a method to perform a network request to delete a hospital.
     *
     * @param hospital the hospital to delete
     */
    fun requestDeleteHospital(hospital: Hospital) {
        performHospitalDeleteOperation(hospitalRepository.deleteHospital(hospital), DeleteHospital, hospital)
    }

    /**
     * Calls a method to perform a network request to update a hospital by replacing it.
     *
     * @param hospital the hospital to update
     */
    fun requestUpdateReplaceHospital(hospital: Hospital) {
        performHospitalUpdateOperation(hospitalRepository.updateReplaceHospital(hospital), UpdateReplace, hospital)
    }

    /**
     * Requests that a GET operation be performed by the hospitalRepository.
     *
     * @param operation the hospitalRepository operation to perform
     * @param method the network method chosen
     */
    private fun performGetHospitalOperation(operation: Single<List<Hospital>?>, method: NetworkMethod) {
        disposables.add(operation.subscribe { result, error ->
            if (result != null) {
                //Update the MutableLiveData of hospitals to trigger any observers to update the UI
                setCurrentHospitals(result)
                //Update the MutableLiveData of response to trigger any observers to update the UI
                setCurrentResponse(ObservableFuelResponse(true, null, method, null, result.size))
            } else if (error != null) {
                //Update the MutableLiveData of response to trigger any observers to update the UI
                setCurrentResponse(ObservableFuelResponse(false, error as FuelError, method, null, null))
                if (error.response.statusCode == 404) {
                    setCurrentHospitals(listOf())
                }
            }
        })
    }

    /**
     * Requests that a POST operation be performed by the hospitalRepository.
     *
     * @param operation the hospitalRepository operation to perform
     * @param method the network method chosen
     * @param hospital the hospital attempted to be POST'ed
     */
    private fun performHospitalCreateOperation(operation: Single<List<Hospital>?>, method: NetworkMethod, hospital: Hospital?) {
        disposables.add(operation.subscribe { result, error ->
            if (result != null) {
                //Update the MutableLiveData of hospitals to trigger any observers to update the UI
                setCurrentHospitals(result)
                //Update the MutableLiveData of response to trigger any observers to update the UI
                setCurrentResponse(ObservableFuelResponse(true, null, method, hospital, null))
            } else if (error != null) {
                //Update the MutableLiveData of response to trigger any observers to update the UI
                setCurrentResponse(ObservableFuelResponse(false, error as FuelError, method, hospital, null))
                if (error.response.statusCode == 400) {
                    setCurrentHospitals(listOf())
                }
            }
        })
    }

    /**
     * Requests that a DELETE operation be performed by the hospitalRepository.
     *
     * @param operation the hospitalRepository operation to perform
     * @param method the network method chosen
     * @param hospital the hospital attempted to be DELETE'ed
     */
    private fun performHospitalDeleteOperation(operation: Single<List<Hospital>?>, method: NetworkMethod, hospital: Hospital?) {
        disposables.add(operation.subscribe { result, error ->
            if (result != null) {
                //Update the MutableLiveData of hospitals to trigger any observers to update the UI
                hospitals.value?.remove(hospital?._id)
                hospitals.value = hospitals.value
                //Update the MutableLiveData of response to trigger any observers to update the UI
                setCurrentResponse(ObservableFuelResponse(true, null, method, hospital, null))
            } else if (error != null) {
                //Update the MutableLiveData of response to trigger any observers to update the UI
                setCurrentResponse(ObservableFuelResponse(false, error as FuelError, method, hospital, null))
            }
        })
    }

    /**
     * Requests that an UPDATE operation be performed by the hospitalRepository.
     *
     * @param operation the hospitalRepository operation to perform
     * @param method the network method chosen
     * @param hospital the hospital attempted to be UPDATE'ed
     */
    private fun performHospitalUpdateOperation(operation: Single<List<Hospital>?>, method: NetworkMethod, hospital: Hospital?) {
        disposables.add(operation.subscribe { result, error ->
            if (result != null) {
                //Update the MutableLiveData of hospitals to trigger any observers to update the UI
                updateHospitalInCurrentHospitals(result[0]._id, result[0])
                //Update the MutableLiveData of response to trigger any observers to update the UI
                setCurrentResponse(ObservableFuelResponse(true, null, method, hospital, null))
            } else if (error != null) {
                //Update the MutableLiveData of response to trigger any observers to update the UI
                setCurrentResponse(ObservableFuelResponse(false, error as FuelError, method, hospital, null))
            }
        })
    }

    /**
     * Disposes of any active disposables (pending or in progress operation) onClear of the
     * ViewModel to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}