package com.commencis.interview.test.api;

import com.commencis.interview.api.PostApi;
import com.commencis.interview.base.BaseApiTest;
import com.commencis.interview.util.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * JSONPlaceholder ornek API testleri.
 * Not: JSONPlaceholder sahte bir servistir; POST/PUT/DELETE gercekten kayit yapmaz,
 * sadece dogru status ve echo edilen govdeyi doner.
 */
@Tag("api")
@DisplayName("Posts API")
class PostApiTest extends BaseApiTest {

    private PostApi postApi;

    @BeforeEach
    void createClient() {
        postApi = new PostApi(spec);
    }

    @Test
    @DisplayName("GET /posts/1 dolu bir kayit doner")
    void getPostReturnsRecord() {
        String title = postApi.getPost(1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("userId", equalTo(1))
                .extract()
                .path("title");

        assertFalse(title.isBlank(), "title bos olmamali");
    }

    @Test
    @DisplayName("POST /posts JSON dosyasindaki govdeyi 201 ile doner")
    void createPostFromJsonFile() {
        String requestBody = JsonReader.read("testdata/create-post.json");

        postApi.createPost(requestBody)
                .then()
                .statusCode(201)
                .body("userId", equalTo(7))
                .body("title", equalTo("Commencis interview lab"))
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("PUT /posts/1 gonderilen govdeyi gunceller")
    void updatePost() {
        String requestBody = JsonReader.read("testdata/create-post.json");

        postApi.updatePost(1, requestBody)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Commencis interview lab"));
    }

    @Test
    @DisplayName("DELETE /posts/1 basarili doner")
    void deletePost() {
        postApi.deletePost(1)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Olmayan id 404 doner")
    void getUnknownPostReturnsNotFound() {
        postApi.getPost(999999)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET yanitindan alinan id sonraki PUT isteginde kullanilir")
    void useValueFromPreviousResponse() {
        // 1. istek: kaydi oku ve id'yi al.
        int id = postApi.getPost(1)
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        // 2. istek: alinan id ile guncelle.
        int updatedId = postApi.updatePost(id, JsonReader.read("testdata/create-post.json"))
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        assertEquals(id, updatedId, "guncellenen kayit ilk istekteki id ile ayni olmali");
    }
}
