package dakota.carefinder

import dakota.carefinder.data.model.Data
import junit.framework.Assert.*
import org.junit.Test

/**
 * Created by dakota on 2/4/18.
 */

private const val TEST_JSON_SINGLE = "{ \"data\": { \"_id\": \"5a2dfb95245b80e74b68f761\", \"providerId\": \"010007\", \"hospitalName\": \"MIZELL MEMORIAL HOSPITAL\", \"address\": \"702 N MAIN ST\", \"city\": \"OPP\", \"state\": \"AL\", \"zipCode\": \"36467\", \"countyName\": \"COVINGTON\", \"phoneNumber\": \"3344933541\", \"hospitalType\": \"Acute Care Hospitals\", \"hospitalOwnership\": \"Voluntary non-profit - Private\", \"emergencyServices\": true, \"dateModified\": \"2017-11-15T16:33:54.007Z\", \"dateCreated\": \"2017-11-15T16:33:54.007Z\", \"location\": { \"latitude\": 31.292159523000464, \"longitude\": -86.25539902199966 } } }"
private const val TESt_JSON_LIST = "{ \"data\": [ { \"_id\": \"5a2dfb95245b80e74b690995\", \"providerId\": \"520021\", \"hospitalName\": \"UNITED HOSPITAL SYSTEM\", \"address\": \"6308 EIGHTH AVE\", \"city\": \"KENOSHA\", \"state\": \"WI\", \"zipCode\": \"53143\", \"countyName\": \"KENOSHA\", \"phoneNumber\": \"2626562011\", \"hospitalType\": \"Acute Care Hospitals\", \"hospitalOwnership\": \"Government - Local\", \"emergencyServices\": true, \"dateModified\": \"2017-11-15T16:33:54.007Z\", \"dateCreated\": \"2017-11-15T16:33:54.007Z\", \"location\": { \"latitude\": 42.57730917900045, \"longitude\": -87.81929779399968 } }, { \"_id\": \"5a2dfb95245b80e74b6909c6\", \"providerId\": \"520189\", \"hospitalName\": \"AURORA MEDICAL CTR KENOSHA\", \"address\": \"10400 75TH ST\", \"city\": \"KENOSHA\", \"state\": \"WI\", \"zipCode\": \"53142\", \"countyName\": \"KENOSHA\", \"phoneNumber\": \"2629485600\", \"hospitalType\": \"Acute Care Hospitals\", \"hospitalOwnership\": \"Voluntary non-profit - Private\", \"emergencyServices\": true, \"dateModified\": \"2017-11-15T16:33:54.007Z\", \"dateCreated\": \"2017-11-15T16:33:54.007Z\", \"location\": { \"latitude\": 42.56742721000046, \"longitude\": -87.93592945499967 } } ] }"

class DataModelTest {

    @Test
    fun deserializeHospitalTest() {
        val data = Data.HospitalDeserializer().deserialize(TEST_JSON_SINGLE)
        assertNotNull(data)
        assertEquals(true, data.isNotEmpty())
    }

    @Test
    fun deserializeHospitalListTest() {
        val data = Data.HospitalListDeserializer().deserialize(TESt_JSON_LIST)
        assertNotNull(data)
        assertEquals(true, data.isNotEmpty())
    }

    @Test
    fun deserializeBadJSONTest() {
        Data.HospitalListDeserializer().deserialize("{BAD}")
        Data.HospitalDeserializer().deserialize("{JSON}")
    }
}