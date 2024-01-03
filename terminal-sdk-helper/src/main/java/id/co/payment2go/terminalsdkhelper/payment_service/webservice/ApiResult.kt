package id.co.payment2go.terminalsdkhelper.payment_service.webservice


/**
 * Sealed class representing the result of an API call.
 *
 * @param T The type of data returned by the API call.
 * @property data The data returned by the API call. It may be null for loading and error states.
 * @property message The message associated with the API result. Typically used for error messages.
 *                  It may be null for success and loading states.
 * @author Samuel Mareno
 */
sealed class ApiResult<T>(
    val data: T? = null,
    val message: String? = null,
    val isTimeoutConnection: Boolean = false
) {

    /**
     * Represents a successful API response with data.
     *
     * @param T The type of data returned by the API call.
     * @property data The data returned by the API call.
     */
    class Success<T>(data: T) : ApiResult<T>(data)

    /**
     * Represents an API error response with an error message and optional data.
     *
     * @param T The type of data returned by the API call.
     * @property message The error message describing the reason for the error.
     * @property data Optional data returned by the API call on error.
     */
    class Error<T>(message: String, data: T? = null, isTimeoutConnection: Boolean = false) :
        ApiResult<T>(data, message, isTimeoutConnection)

    /**
     * Represents a loading state during an ongoing API call.
     *
     * @param T The type of data returned by the API call.
     */
    class Loading<T> : ApiResult<T>()
}