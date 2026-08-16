package com.commencis.interview.base;

import com.commencis.interview.api.ApiClient;
import com.commencis.interview.api.RequestSpecFactory;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;

/**
 * API testlerin ortak hazirligi: her test icin temiz bir RequestSpecification uretir.
 * Spec'in nasil kuruldugu {@link RequestSpecFactory} icindedir; Cucumber tarafi da ayni
 * factory'yi kullanir.
 */
public abstract class BaseApiTest {

    protected RequestSpecification spec;
    protected ApiClient api;

    @BeforeEach
    protected void prepareSpec() {
        spec = RequestSpecFactory.create();
        api = new ApiClient(spec);
    }
}
