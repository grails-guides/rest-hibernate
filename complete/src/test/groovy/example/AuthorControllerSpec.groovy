package example

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Controller unit test for the AuthorController pagination clamping.
 */
class AuthorControllerSpec extends Specification
        implements ControllerUnitTest<AuthorController>, DataTest {

    Class[] getDomainClassesToMock() { [Author, Book] }

    @Unroll
    void "index clamps max #requested to #expected"() {
        when:
        controller.index(requested)

        then:
        controller.params.max == expected

        where:
        requested || expected
        500       || 100
        10        || 10
        null      || 25
        -5        || 25
    }

    void "index never produces a negative offset"() {
        given:
        controller.params.offset = -10

        when:
        controller.index(25)

        then:
        controller.params.offset == 0
    }
}
