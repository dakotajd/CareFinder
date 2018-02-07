package dakota.carefinder

import android.app.Application
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response

/**
 * Constant API key so you don't have to create one on the server and edit it here before running
 * the application. The server will always accept this key as it is hard coded there too.
 */
const val API_KEY = "ZN6MG9az6L2l131TSmwRdKs7kvzKAb6BTOT80ac2"

/**
 * Created by dakota on 11/29/17.
 */
class CareFinderApplication : Application() {

    /**
     * Set up the FuelInstance information
     */
    override fun onCreate() {
        super.onCreate()
        FuelManager.instance.basePath = "http://10.0.2.2:3001/api/$API_KEY"
        //FuelManager.instance.basePath = "http://131.210.23.146:3001/api/$API_KEY"
        FuelManager.instance.baseHeaders = mapOf("Content-Type" to "application/json")

        FuelManager.instance.addResponseInterceptor  { next: (Request, Response) -> Response ->
            { req: Request, res: Response ->
                Log.d("FUEL RESPONSE", res.toString())
                next(req, res)
            }
        }

    }
}