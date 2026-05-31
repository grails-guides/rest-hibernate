package example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Functional test for the root application endpoint.
 */
@Integration
class ApplicationFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    HttpClient client = HttpClient.newHttpClient()

    private URI uri(String path) { URI.create("http://localhost:${serverPort}${path}") }

    void "GET / returns the welcome payload"() {
        when:
        HttpResponse<String> resp = client.send(
                HttpRequest.newBuilder(uri('/'))
                        .header('Accept', 'application/json')
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString())
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.message == 'Welcome to Grails!'
        json.environment == 'test'
        json.grailsversion != null
        json.groovyversion != null
        json.jvmversion != null
        json.artefacts != null
    }
}
