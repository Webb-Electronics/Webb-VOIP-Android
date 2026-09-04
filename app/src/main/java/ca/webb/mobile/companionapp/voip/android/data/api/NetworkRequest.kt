package ca.webb.mobile.companionapp.voip.android.data.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

/**
 * Network request listener interface
 *
 * @constructor Create empty Network request listener
 */
interface NetworkRequestListener {
    /**
     * On response
     *
     * @param response the response
     */
    fun onResponse(response: String)

    /**
     * On error
     *
     * @param error the error
     */
    fun onError(error: String)
}

/**
 * Network request abstract class
 *
 * @constructor to be implemented by subclasses
 *
 * @param context the activity context
 */
abstract class NetworkRequest(context: Context) {
    abstract val sendingHeaders: Map<String, String>
    abstract var url: String

    private val requestQueue = Volley.newRequestQueue(context)

    /**
     * Send request to the network
     *
     * @param body the request body
     * @param httpRequestMethod the HTTP request method default is GET
     * @param networkRequestListener the network request listener interface implementation
     */
    fun sendRequest(
        body: String? = null,
        httpRequestMethod: String = "GET",
        networkRequestListener: NetworkRequestListener
    ) {
        val method = when (httpRequestMethod.uppercase()) {
            "GET" -> Request.Method.GET
            "POST" -> Request.Method.POST
            "PUT" -> Request.Method.PUT
            "DELETE" -> Request.Method.DELETE
            "PATCH" -> Request.Method.PATCH
            "HEAD" -> Request.Method.HEAD
            "OPTIONS" -> Request.Method.OPTIONS
            "TRACE" -> Request.Method.TRACE
            else -> throw IllegalArgumentException("Invalid HTTP request method")
        }

        val stringRequest = object : StringRequest(method, url, Response.Listener { response ->
            networkRequestListener.onResponse(response)
        }, Response.ErrorListener { error ->
            networkRequestListener.onError(error.toString())
        }) {

            override fun getHeaders(): Map<String, String> {
                return sendingHeaders
            }

            override fun getBody(): ByteArray? {
                return body?.toByteArray() ?: super.getBody()
            }
        }
        requestQueue.add(stringRequest)
    }
}

