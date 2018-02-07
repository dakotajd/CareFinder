package dakota.carefinder.data.model

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Created by dakota on 12/3/17.
 */

/**
 * A class for modeling the Data[] container that the network responses send for hospitals.
 */
data class Data<T>(var data: T) {

    @Suppress("UNCHECKED_CAST")
    /**
     * Deserializes a list of hospitals as JSON.
     *
     * @return a list of Hospitals
     */
    class HospitalListDeserializer : ResponseDeserializable<List<Hospital>> {
        override fun deserialize(content: String): List<Hospital> {
            val type: Type = object : TypeToken<Data<List<Hospital>>>() {}.type
            //val data: Data<List<Hospital>> = Gson().fromJson(content, type)
            return deserializeJSON(content, type, HOSPITAL_LIST)
        }
    }

    @Suppress("UNCHECKED_CAST")
    /**
     * Deserializes a single hospital into a list of hospitals as JSON.
     *
     * @return a list of Hospitals
     */
    class HospitalDeserializer : ResponseDeserializable<List<Hospital>> {
        override fun deserialize(content: String): List<Hospital> {
            val type: Type = object : TypeToken<Data<Hospital>>() {}.type
            //val data: Data<Hospital> = Gson().fromJson(content, type)
            return deserializeJSON(content, type, HOSPITAL_SINGLE)
        }
    }

    companion object {

        private const val HOSPITAL_LIST = 1

        private const val HOSPITAL_SINGLE = 2

        //Probably a better way to do this
        private fun deserializeJSON(content: String, type: Type, mode: Int): List<Hospital> {
            return try {
                when (mode) {
                    1 -> {
                        val data: Data<List<Hospital>> = Gson().fromJson(content, type)
                        data.data
                    }
                    2 -> {
                        val data: Data<Hospital> = Gson().fromJson(content, type)
                        listOf(data.data)
                    }
                    else -> listOf()
                }
            } catch (e: JsonSyntaxException) {
                listOf()
            }
        }
    }

}