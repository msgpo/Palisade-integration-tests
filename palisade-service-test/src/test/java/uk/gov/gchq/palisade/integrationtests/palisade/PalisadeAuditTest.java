package uk.gov.gchq.palisade.integrationtests.palisade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.palisade.request.AuditRequest;
import uk.gov.gchq.palisade.service.palisade.request.AuditRequest.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.service.palisade.service.AuditService;

import java.util.HashSet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

//mocks an audit trail
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"test"})
public class PalisadeAuditTest {
    @Autowired
    public AuditService auditService;

    @Autowired
    public ObjectMapper objectMapper;

    @ClassRule
    public static WireMockRule auditServiceMock = new WireMockRule(options().port(8086).notifier(new ConsoleNotifier(true)));

    private static User user = new User().userId("user-id");
    @Mock
    private static Context context = new Context().purpose("test-purpose");

    private static DirectoryResource DIRECTORY = new DirectoryResource().id("home");
    private static LeafResource LEAF_RESOURCE = new FileResource().id("data-to-access");
    static {
        DIRECTORY.parent(new SystemResource().id("nix"));
        LEAF_RESOURCE.parent(DIRECTORY);
        LEAF_RESOURCE.setSerialisedFormat("json");
        LEAF_RESOURCE.setType("string");
    }

    private static final AuditRequest.RegisterRequestCompleteAuditRequest.IUser REQUEST_CONSTRUCTOR = RegisterRequestCompleteAuditRequest.create(new RequestId().id("request-id"));
    private static final AuditRequest AUDIT_REQUEST = REQUEST_CONSTRUCTOR
            .withUser(user)
            .withLeafResources(new HashSet<LeafResource>() {{add(LEAF_RESOURCE);}})
            .withContext(context);

     @Test
     public void auditServiceTest() {
         auditServiceMock.stubFor(WireMock.post(urlPathMatching("/audit"))
                 .withRequestBody(containing("request-id"))
                 .willReturn(
                         aResponse()
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody("true")
                 ));

        Boolean result = this.auditService.audit(AUDIT_REQUEST);
        assertThat(result, is(true));
    }
}
