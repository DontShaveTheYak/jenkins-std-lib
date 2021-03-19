/* groovylint-disable DuplicateStringLiteral, Instanceof, ParameterReassignment, ThrowException, UnnecessarySetter */
package org.dsty.http

import static groovy.json.JsonOutput.toJson

import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonSlurperClassic

/**
 * This class consists exclusively of static methods that perform HTTP requests.
 * It was designed to behave like Python's <a href="https://requests.readthedocs.io/en/master/">Requests</a> library.
 *
 * <p>You can import and use Requests in the following ways:
 * <pre>{@code
 * import org.dsty.http.Requests
 *Requests.get('https://httpbin.org/get')
 *{@literal /}{@literal /} also valid
 *def request = new Requests()
 *request.get('https://httpbin.org/get')}</pre>
 *
 * <p>Each HTTP method takes the same options of type {@link Map}. All options are optional arguments but some
 * of the HTTP methods may require one or more. You can pass in each option as a named parameter like in the examples
 * below or you can bundle them up into a single {@link Map} as the first parameter.
 * <pre>{@code
 * Map options = [:]
 *options['headers'] = ['Accept': 'application/json']
 *def response = Requests.get(options, 'https://httpbin.org/get')}</pre>
 *
 * <p>Parameters:
 * <pre>{@code
 * Map queryParams = ['key1': 'value1']
 *def response = Requests.get('https://httpbin.org/get', params: queryParams)
 *{@literal /}{@literal /} would create the following url https://httpbin.org/get?key1=value1}</pre>
 * All parameter keys must be of type {@link String} but the values can be either {@link String} or {@link List}.
 * <pre>{@code
 * queryParams = ['key1': 'value1', 'key2': ['value2', 'value3']]
 *def response = Requests.get('https://httpbin.org/get', params: queryParams)
 *{@literal /}{@literal /} would create the following url https://httpbin.org/get?key1=value1&key2=value2&key2=value3}</pre>
 * You can also create the url string manually.
 * <pre>{@code
 * String url = 'https://httpbin.org/get?key1=hello world'
 *def response = Requests.get(url)
 *{@literal /}{@literal /} would create the following url https://httpbin.org/get?key1=hello%20world}</pre>
 *
 * <p>Headers:
 * <pre>{@code
 * Map customHeaders = ['Cache-Control': 'no-cache']
 *def response = Requests.get('https://httpbin.org/get', headers: customHeaders)}</pre>
 *
 * <p>Body:
 * <pre>{@code
 * String payload = 'My String Payload'
 *Map customHeaders = ['Accept': 'text/plain']
 *def response = Requests.post('https://httpbin.org/post', headers: customHeaders, body: payload)}</pre>
 *
 * <p>JSON:
 * <pre>{@code
 * Map payload = ['TestKey': 'TestValue']
 *Map customHeaders = ['Accept': 'application/json']
 *def response = Requests.post('https://httpbin.org/post', json: payload, headers: customHeaders)}</pre>
 *
 * <p>Auth:
 * <pre>{@code
 * Map creds = ['type': 'Basic', 'username': 'john', 'password': 'secret!']
 *def response = Requests.get('https://httpbin.org/get', auth: creds)}</pre>
 * It's also possible to use a Bearer token.
 * <pre>{@code
 * Map creds = ['type': 'Bearer', 'token': '8LuKmhJcSoLME9RK5MVJ']
 *def response = Requests.get('https://httpbin.org/get', auth: creds)}</pre>
 *
 * <p>Every method will return a {@link org.dsty.http.Response Response} object.
 */
class Requests implements Serializable {

  /**
    * Performs an HTTP GET Request.
    * @param options A map of additional options.
    * @param url The url you want to target.
    * @return An HTTP {@link org.dsty.http.Response Response}.
    */
  static Response get(Map options = [:], String url) {
    return execute('GET', url, options)
  }

  /**
    * Performs an HTTP POST Request.
    * @param options A map of additional options.
    * @param url The url you want to target.
    * @return An HTTP {@link org.dsty.http.Response Response}.
    */
  static Response post(Map options = [:], String url) {
    return execute('POST', url, options)
  }

  /**
    * Performs an HTTP DELETE Request.
    * @param options A map of additional options.
    * @param url The url you want to target.
    * @return An HTTP {@link org.dsty.http.Response Response}.
    */
  static Response delete(Map options = [:], String url) {
    return execute('DELETE', url, options)
  }

  /**
    * Performs an HTTP PUT Request.
    * @param options A map of additional options.
    * @param url The url you want to target.
    * @return An HTTP {@link org.dsty.http.Response Response}.
    */
  static Response put(Map options = [:], String url) {
    return execute('PUT', url, options)
  }

  /**
    * Performs an HTTP Request.
    * @param method The HTTP method you want to execute.
    * @param url Th URL you want to target.
    * @param options A map of additional options.
    * @return An HTTP {@link org.dsty.http.Response Response}.
    */
  @NonCPS
  static private Response execute(String method, String url, Map options) {
    options.headers = options.headers ?: [:]

    if (options.params) {
      url = "${url}?${encodeParams(options.params)}"
    }

    if (options.json) {
      options['body'] = toJson(options.json)
    }

    if (options.auth) {
      options.headers['Authorization'] = configureAuth(options.auth)
    }

    URL validURL = validateURL(url)

    HttpURLConnection connection = validURL.openConnection() as HttpURLConnection

    connection.setRequestMethod(method)

    connection = options.headers ? applyHeaders(connection, options.headers) : connection

    if (options.body) {
      connection.setDoOutput(true)
      connection.getOutputStream().write(options.body.getBytes('UTF-8'))
    }

    Response response = handleRequest(connection)

    if (response.headers.find { it.value == 'application/json' } ) {
      JsonSlurperClassic slurper = new JsonSlurperClassic()
      response.json = slurper.parseText(response.body)
    }

    return response
  }

  /**
  * Applys a Map of Headers to an HTTP conntection.
  * @param connection Connection you want to set headers on.
  * @param headers The headers you want to set.
  * @return The connection with the headers set.
  */
  @NonCPS
  static private HttpURLConnection applyHeaders(HttpURLConnection connection, Map headers) {
    for (header in headers) {
      connection.setRequestProperty(header.key, header.value)
    }
    return connection
  }

  /**
  * Creates URL encoded parameters.
  * @param params The HTTP query parameters.
  * @return The HTTP query string.
  */
  @NonCPS
  static private String encodeParams(Map params) {
    return params.collect { it ->
      if ( !(it.value instanceof List) && !(it.value instanceof String) ) {
        throw new Exception('Parameter values must be String or List.')
      }

      if ( it.value instanceof List ) {
        return it.value.collect { value ->
          if ( !(value instanceof String) ) {
            throw new Exception('List values must be of type String.')
          }
          return "${it.key}=${value}"
        }.join('&')
      }
      return it
    }.join('&')
  }

  /**
  * Configure HTTP Authentication.
  * @param auth The auth configuration.
  * @return The correct Authorization header.
  */
  @NonCPS
  static private String configureAuth(Map auth) {
    if (auth.type == 'Basic') {
      String creds = "${auth.username}:${auth.password}".getBytes().encodeBase64()
      return "${auth.type} ${creds}"
    }

    if (auth.type == 'Bearer') {
      return "${auth.type} ${auth.token}"
    }
  }

  /**
  * Turns the raw server response into Response object.
  * @param connection Connection that is ready to make a request.
  * @return An HTTP {@link org.dsty.http.Response Response}.
  */
  @NonCPS
  static private Response handleRequest(HttpURLConnection connection) {
    Integer responseCode = connection.getResponseCode()

    String body

    if (connection.getErrorStream()) {
      body = connection.getErrorStream().getText()
    } else {
      body = connection.getInputStream().getText()
    }

    String url = connection.url

    Map rawHeaders = connection.getHeaderFields().collectEntries { k, v ->
      [(k): v.join(',')]
    }

    Map headers = rawHeaders.findAll { it.key != null }

    return new Response(url, headers, body, responseCode)
  }

  /**
  * Validates and encodes an HTTP URL.
  * @param urk A URL to an HTTP server.
  * @return A valid and properly encoded URL instance.
  */
  @NonCPS
  static private URL validateURL(String url) throws java.net.URISyntaxException {
    // best way I found to convert `https://httpbin.org/get?test=hello world`
    // to `https://httpbin.org/get?test=hello%20world` with little effort.
    URL tempURL = new URL(url)
    URI uri = new URI(
      tempURL.getProtocol(),
      tempURL.getUserInfo(),
      tempURL.getHost(),
      tempURL.getPort(),
      tempURL.getPath(),
      tempURL.getQuery(),
      tempURL.getRef()
    )

    return uri.toURL()
  }

}
