package dakota.carefinder.data.network

import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.rx.rx_object
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import dakota.carefinder.data.model.Data
import dakota.carefinder.data.model.Hospital
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by dakota on 11/27/17.
 */

class NetworkHospitalRepository : HospitalRepository {

    /**
     * A GSON instance for going from Hospital -> JSON
     */
    private val gson = GsonBuilder().addSerializationExclusionStrategy(ExcludeServerData()).create()

    /**
     * Performs a GET operation to get all of the hospitals. Network requests and deserialization
     * are done on a background thread and the result is to be observed for on the main thread.
     *
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun getAllHospitals(): Single<List<Hospital>?> {
        return "/hospitals".httpGet().rx_object(Data.HospitalListDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Performs a GET operation to get all of the hospitals with a certain name. Network requests
     * and deserialization are done on a background thread and the result is to be observed for on
     * the main thread.
     *
     * @param name the name to get hospitals with
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun getHospitalsByName(name: String): Single<List<Hospital>?> {
        return "/hospitals/name/$name".httpGet().rx_object(Data.HospitalListDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Performs a GET operation to get all of the hospitals in a certain city. Network requests and
     * deserialization are done on a background thread and the result is to be observed for on the
     * main thread.
     *
     * @param city the city to get hospitals from
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun getHospitalsByCity(city: String): Single<List<Hospital>?> {
        return "/hospitals/city/$city".httpGet().rx_object(Data.HospitalListDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Performs a GET operation to get all of the hospitals in a certain state. Network requests and
     * deserialization are done on a background thread and the result is to be observed for on the
     * main thread.
     *
     * @param state the state to get hospitals from
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun getHospitalsByState(state: String): Single<List<Hospital>?> {
        return "/hospitals/state/$state".httpGet().rx_object(Data.HospitalListDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Performs a GET operation to get all of the hospitals in a certain city, state. Network
     * requests and deserialization are done on a background thread and the result is to be observed
     * for on the main thread.
     *
     * @param city the city to get hospitals from
     * @param state the state to get hospitals from
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun getHospitalByCityState(city: String, state: String): Single<List<Hospital>?> {
        return "/hospitals/city/$city/state/$state".httpGet().rx_object(Data.HospitalListDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Performs a GET operation to get all of the hospitals in a certain county. Network requests
     * and deserialization are done on a background thread and the result is to be observed for on
     * the main thread.
     *
     * @param county the county to get hospitals from
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun getHospitalByCounty(county: String): Single<List<Hospital>?> {
        return "/hospitals/county/$county".httpGet().rx_object(Data.HospitalListDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Performs a POST operation to create a hospital. Network requests and deserialization are done
     * on a background thread and the result is to be observed for on the main thread.
     *
     * @param hospital the hospital to create
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun createHospital(hospital: Hospital): Single<List<Hospital>?> {
        return "/hospitals".httpPost().body(gson.toJson(hospital)).rx_object(Data.HospitalDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Performs a DELETE operation to delete a hospital. Network requests and deserialization are
     * done on a background thread and the result is to be observed for on the main thread.
     *
     * @param hospital the hospital to delete
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun deleteHospital(hospital: Hospital): Single<List<Hospital>?> {
        return "/hospitals/id/${hospital._id}".httpDelete().rx_object(Data.HospitalDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Performs a POST operation to update a hospital. Network requests and deserialization are done
     * on a background thread and the result is to be observed for on the main thread.
     *
     * @param hospital the hospital to uodate
     * @return a single (an observable source of data that can emit a result from an operation)
     * containing a list of hospitals
     */
    override fun updateReplaceHospital(hospital: Hospital): Single<List<Hospital>?> {
        return "/hospitals/id/${hospital._id}".httpPut().body(gson.toJson(hospital)).rx_object(Data.HospitalDeserializer())
                .map { it.component1() ?: throw it.component2() ?: throw Exception() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Excludes _id and date modified/created fields so they don't get sent back to the server
     * when you POST/PUT/PATCH
     */
    class ExcludeServerData : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }

        override fun shouldSkipField(f: FieldAttributes?): Boolean {
            return (f?.name?.equals("_id") ?: false || f?.name?.contains("date") ?: false)
        }

    }
}