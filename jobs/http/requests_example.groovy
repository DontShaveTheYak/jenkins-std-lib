/* groovylint-disable DuplicateMapLiteral, DuplicateStringLiteral, UnnecessaryGetter, UnusedVariable */
@Library('pipeline-library')

import org.dsty.http.Requests
import org.dsty.http.Response

node() {
    String cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)

    Requests request = new Requests()

    Response response

    String url

    Map headers

    Map auth

    stage('Get Request') {
        url = 'https://httpbin.org/get'

        // These parameters will automaticly be added to the url
        // https://httpbin.org/get?Param1=Value1&Param1=Value2&Param2=Value%203
        Map params = [
            'Param1': [
                'Value1',
                'Value2'
            ],
            'Param2': 'Value 3'
        ]

        response = Requests.get(url, params: params)

        if (!response.isOkay()) {
            println(response)
            error('Should have made a proper HTTP GET request.')
        }

        if (response.json.args != params) {
            error('Should have set the correct parameters.')
        }

        String hello = 'hello world'

        // You can still pass parameters manually as well
        url = "https://httpbin.org/get?key1=${hello}"

        response = Requests.get(url)

        if (!response.isOkay()) {
            println(response)
            error('Should have made a proper HTTP GET request.')
        }

        if (response.json.args['key1'] != hello) {
            println(response)
            error('Should have set the correct parameters.')
        }
    }

    stage('Pass headers') {
        String headerName = 'Example'
        String headerValue = 'Value'

        url = 'https://httpbin.org/headers'

        response = request.get(url, headers: [(headerName): headerValue])

        if (response.json.headers[headerName] != headerValue) {
            println(response)
            error('Should have set headers correctly.')
        }
    }

    stage('Use JSON') {
        headers = [
            'content-type': 'application/json',
        ]

        Map json = [
            'TestKey': 'TestValue'
        ]

        response = request.post('https://httpbin.org/post', headers: headers, json: json)

        if (response.json['json'] != json) {
            println(response)
            error('Should have posted json.')
        }
    }

    stage('Basic Auth') {
        auth = [
            'type': 'Basic',
            'username': 'Test',
            'password': 'secret',
        ]

        url = 'https://httpbin.org/basic-auth/Test/secret'

        response = request.get(url, auth: auth)

        if (!response.json.authenticated) {
            println(response)
            error('Should have autheticated.')
        }
    }

    stage('Bearer Auth') {
        auth = [
            'type': 'Bearer',
            'token': 'Test',
        ]

        url = 'https://httpbin.org/bearer'

        response = request.get(url, auth: auth)

        if (!response.json.authenticated) {
            println(response)
            error('Should have autheticated.')
        }

        // Random delete request

        url = 'https://httpbin.org/delete'

        response = request.delete(url, auth: auth)

        if (!response.isOkay()) {
            println(response)
            error('Should have made a proper HTTP DELETE request.')
        }

        // Random put request

        url = 'https://httpbin.org/put'

        response = request.put(url, auth: auth)

        if (!response.isOkay()) {
            println(response)
            error('Should have made a proper HTTP PUT request.')
        }
    }

    cps = sh(script: '#!/bin/bash\nset +x; > /dev/null 2>&1\necho Test for CPS issue', returnStdout: true)
}
