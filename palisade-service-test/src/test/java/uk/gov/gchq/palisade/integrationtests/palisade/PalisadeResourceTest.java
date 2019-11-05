package uk.gov.gchq.palisade.integrationtests.palisade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.palisade.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.palisade.service.ResourceService;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"test"})
public class PalisadeResourceTest extends BaseTestEnvironment {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockRule resourceServiceMock = RESOURCE_SERVICE_MOCK;

    private LeafResource leafResource = LEAF_RESOURCE;
    private Map<LeafResource, ConnectionDetail> resourceInfo = RESOURCE_INFO;

    private GetResourcesByIdRequest getResourceRequest = new GetResourcesByIdRequest()
            .resourceId(LEAF_RESOURCE.getId());
    {
        getResourceRequest.originalRequestId(REQUEST_ID);
    }

    @Test
    public void resourceServiceTest() throws Exception {
        resourceServiceMock.stubFor(post(urlPathMatching("/getResourcesById"))
                .withRequestBody(containing(leafResource.getId()))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(resourceInfo))
                ));

        Map<LeafResource, ConnectionDetail> resource = this.resourceService.getResourcesById(getResourceRequest).get();
        assertThat(resource, equalTo(resourceInfo));
    }
}
