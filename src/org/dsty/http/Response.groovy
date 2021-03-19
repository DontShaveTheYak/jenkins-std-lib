package org.dsty.http

import com.cloudbees.groovy.cps.NonCPS

/**
 * Contains the results of a HTTP request.
 */
class Response implements Serializable {

  /**
   * The url used in the original request.
   */
  String url

  /**
   * The response headers.
   */
  Map headers

  /**
   * The raw response recieved from the server.
   */
  String body

  /**
   * The response status code.
   */
  Integer statusCode

  /**
   * Set if the reponse was valid json.
   */
  Map json

  Response(String url, Map headers, String body, Integer statusCode) {
    this.url = url
    this.headers = headers
    this.body = body
    this.statusCode = statusCode
  }

  /**
   * Checks if the statusCode is 2xx
   * @return True if the statusCode is 2xx.
   */
  Boolean isOkay() {
    return this.statusCode.intdiv(100) == 2
  }

  /**
   * Prints the response body.
   * @return The output from the bash script.
   */
  @Override
  @NonCPS
  String toString() {
      return this.body
  }

}
