package dakota.carefinder.data.model

/**
 * Created by dakota on 12/4/17.
 */

/**
 * An enum class for modeling request methods.
 */
enum class NetworkMethod(val option: Int) {
    GetAll(0),
    GetByName(1),
    GetByCity(2),
    GetByState(3),
    GetByCityState(4),
    GetByCounty(5),
    GetById(6),
    CreateHospital(7),
    UpdateReplace(8),
    UpdateModify(9),
    DeleteHospital(10)
}