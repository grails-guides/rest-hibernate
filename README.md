# grails-rest-library

Sample app for the apache/grails-static-website guide [grails-rest-library/v8](https://grails.apache.org/guides/grails-rest-library/8/guide/index.html).

A Book + Author REST API on Grails 8: `RestfulController`, JSON views, structured 422 validation responses, pagination, all-or-nothing bulk create, and URL versioning under `/v1/`.

`initial/` is a vanilla Grails 8 `rest_api` starter from https://prev-snapshot.grails.org. `complete/` adds the Book + Author domain model and the controller/view layer described in the guide.

```bash
git clone -b grails8 https://github.com/grails-guides/grails-rest-library.git
cd grails-rest-library/complete
./gradlew bootRun
curl http://localhost:8080/v1/books
```
