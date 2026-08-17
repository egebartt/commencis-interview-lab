package com.commencis.interview.api;

import com.commencis.interview.core.report.AllureAttachments;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rapor kanitini ortak HTTP noktasinda uretir.
 *
 * <p>Filtre {@code RequestSpecFactory} ile kurulan her spec'te calisir; boylece Cucumber adimlari
 * ve JUnit live testleri ayni ek'i alir, adim tarafinda elle attachment cagrisi kalmaz.
 *
 * <p>URI filtre zincirinde okunur: base URL cozulmus, path parametreleri yerlesmis ve query
 * parametreleri encode edilmis hali gorunur. Istek burada yeniden gonderilmez.
 */
public class ApiReportingFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {
        AllureAttachments.attachRequest(requestSpec.getMethod(), requestSpec.getURI(),
                headersOf(requestSpec), requestSpec.getBody());

        // Transport hatasinda yanit hic olusmaz; istek kaniti yukarida zaten yazildi.
        Response response = context.next(requestSpec, responseSpec);
        AllureAttachments.attachResponse(response);
        return response;
    }

    private static Map<String, String> headersOf(FilterableRequestSpecification requestSpec) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (Header header : requestSpec.getHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }
}
