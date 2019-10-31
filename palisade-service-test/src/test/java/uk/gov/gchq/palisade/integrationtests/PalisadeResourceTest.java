package uk.gov.gchq.palisade.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.integrationtests.PalisadeRegisterRequestTest.StubService;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.palisade.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.palisade.service.ResourceService;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"test"})
public class PalisadeResourceTest {

    @Autowired
    public ResourceService resourceService;

    @Autowired
    public ObjectMapper objectMapper;

    @ClassRule
    public static WireMockRule resourceServiceMock = new WireMockRule(options().port(8082).notifier(new ConsoleNotifier(true)));

    private static DirectoryResource DIRECTORY = new DirectoryResource().id("home");
    private static LeafResource LEAF_RESOURCE = new FileResource().id("data-to-access");
    static {
        DIRECTORY.parent(new SystemResource().id("nix"));
        LEAF_RESOURCE.parent(DIRECTORY);
        LEAF_RESOURCE.setSerialisedFormat("json");
        LEAF_RESOURCE.setType("string");
    }
    private static SimpleConnectionDetail CONNECTION_DETAIL = new SimpleConnectionDetail().service(new StubService());
    private static Map<LeafResource, ConnectionDetail> RESOURCE_INFO = Stream.of(new AbstractMap.SimpleEntry<>(LEAF_RESOURCE, CONNECTION_DETAIL)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    private static GetResourcesByIdRequest GET_RESOURCE_REQUEST = new GetResourcesByIdRequest().resourceId("data-to-access");
    static {
        GET_RESOURCE_REQUEST.setOriginalRequestId(new RequestId().id(UUID.randomUUID().toString()));
    }

    @Test
    public void resourceServiceTest() throws Exception {
        resourceServiceMock.stubFor(post(urlPathMatching("/getResourcesById"))
                .withRequestBody(containing("data-to-access"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(RESOURCE_INFO))
                ));

        Map<LeafResource, ConnectionDetail> resource = this.resourceService.getResourcesById(GET_RESOURCE_REQUEST).get();
        assertThat(resource, equalTo(RESOURCE_INFO));
    }
}
