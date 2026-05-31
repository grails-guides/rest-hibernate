package example

import grails.testing.mixin.integration.Integration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Functional tests for the Author REST resource (/v1/authors).
 * Follows the same pattern as BookFunctionalSpec.
 */
@Integration
class AuthorFunctionalSpec extends Specification {

    @Value('${local.server.port}')
    Integer serverPort

    HttpClient client = HttpClient.newHttpClient()

    private URI uri(String path) { URI.create("http://localhost:${serverPort}${path}") }

    private HttpResponse<String> getJson(String path) {
        client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> postJson(String path, String body) {
        client.send(HttpRequest.newBuilder(uri(path))
                .header('Content-Type', 'application/json')
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> putJson(String path, String body) {
        client.send(HttpRequest.newBuilder(uri(path))
                .header('Content-Type', 'application/json')
                .PUT(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString())
    }

    private HttpResponse<String> deleteResource(String path) {
        client.send(HttpRequest.newBuilder(uri(path)).DELETE().build(),
                HttpResponse.BodyHandlers.ofString())
    }

    void setup() {
        Author.withNewTransaction {
            if (!Author.findByName('Author Functional')) {
                new Author(name: 'Author Functional', biography: 'Seed author').save(failOnError: true)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Index (GET /v1/authors) – tested in BookFunctionalSpec
    // -------------------------------------------------------------------------

    void "GET /v1/authors/{id} returns a single author"() {
        given:
        Long authorId = Author.withNewTransaction { Author.findByName('Author Functional').id }

        when:
        HttpResponse<String> resp = getJson("/v1/authors/${authorId}")
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.name == 'Author Functional'
        json.biography == 'Seed author'
        json._links.self.href.contains("id=${authorId}")
        json._links.books.href.contains('/v1/books?author=')
    }

    void "GET /v1/authors/{id} returns 404 for non-existent author"() {
        when:
        HttpResponse<String> resp = getJson('/v1/authors/999999')

        then:
        resp.statusCode() == 404
    }

    // -------------------------------------------------------------------------
    // Save (POST /v1/authors)
    // -------------------------------------------------------------------------

    void "POST /v1/authors creates a new author"() {
        given:
        String body = JsonOutput.toJson([name: 'Created Author', biography: 'Brand new'])

        when:
        HttpResponse<String> resp = postJson('/v1/authors', body)
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 201
        json.name == 'Created Author'
        json.biography == 'Brand new'

        cleanup:
        Author.withNewTransaction { Author.findByName('Created Author')?.delete(flush: true) }
    }

    void "POST /v1/authors returns 422 when name is blank"() {
        given:
        String body = JsonOutput.toJson([name: ''])

        when:
        HttpResponse<String> resp = postJson('/v1/authors', body)

        then:
        resp.statusCode() == 422
    }

    // -------------------------------------------------------------------------
    // Update (PUT /v1/authors/{id})
    // -------------------------------------------------------------------------

    void "PUT /v1/authors/{id} updates an existing author"() {
        given:
        Long authorId = Author.withNewTransaction { Author.findByName('Author Functional').id }
        String body = JsonOutput.toJson([name: 'Updated Author', biography: 'Updated bio'])

        when:
        HttpResponse<String> resp = putJson("/v1/authors/${authorId}", body)
        Map json = new JsonSlurper().parseText(resp.body()) as Map

        then:
        resp.statusCode() == 200
        json.name == 'Updated Author'
        json.biography == 'Updated bio'

        cleanup:
        Author.withNewTransaction {
            Author a = Author.get(authorId)
            if (a) {
                a.name = 'Author Functional'
                a.biography = 'Seed author'
                a.save(flush: true, failOnError: true)
            }
        }
    }

    void "PUT /v1/authors/{id} returns 422 when name is blank"() {
        given:
        Long authorId = Author.withNewTransaction { Author.findByName('Author Functional').id }
        String body = JsonOutput.toJson([name: ''])

        when:
        HttpResponse<String> resp = putJson("/v1/authors/${authorId}", body)

        then:
        resp.statusCode() == 422
    }

    void "PUT /v1/authors/{id} returns 404 for non-existent author"() {
        when:
        HttpResponse<String> resp = putJson('/v1/authors/999999', JsonOutput.toJson([name: 'Nope']))

        then:
        resp.statusCode() == 404
    }

    // -------------------------------------------------------------------------
    // Delete (DELETE /v1/authors/{id})
    // -------------------------------------------------------------------------

    void "DELETE /v1/authors/{id} deletes an author with no books"() {
        given:
        Long disposableId = Author.withNewTransaction {
            new Author(name: 'Disposable Author').save(flush: true, failOnError: true).id
        }

        when:
        HttpResponse<String> resp = deleteResource("/v1/authors/${disposableId}")

        then:
        resp.statusCode() == 204
        Author.withNewTransaction { Author.get(disposableId) == null }
    }

    void "DELETE /v1/authors/{id} returns 404 for non-existent author"() {
        when:
        HttpResponse<String> resp = deleteResource('/v1/authors/999999')

        then:
        resp.statusCode() == 404
    }

}
