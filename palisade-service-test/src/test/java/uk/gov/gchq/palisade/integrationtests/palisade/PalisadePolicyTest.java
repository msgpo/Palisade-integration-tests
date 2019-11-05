package uk.gov.gchq.palisade.integrationtests.palisade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.service.palisade.policy.MultiPolicy;
import uk.gov.gchq.palisade.service.palisade.request.GetPolicyRequest;
import uk.gov.gchq.palisade.service.palisade.service.PolicyService;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"test"})
public class PalisadePolicyTest extends BaseTestEnvironment {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockRule policyServiceMock = POLICY_SERVICE_MOCK;

    private MultiPolicy multiPolicy = MULTI_POLICY;
    private GetPolicyRequest policyRequest = new GetPolicyRequest()
            .user(USER)
            .resources(Collections.singletonList(LEAF_RESOURCE))
            .context(CONTEXT);
    {
        policyRequest.originalRequestId(REQUEST_ID);
    }

    @Test
    public void policyServiceTest() throws Exception {
        policyServiceMock.stubFor(post(urlPathMatching("/getPolicy"))
                .withRequestBody(containing("user-id"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(multiPolicy))
                ));

        MultiPolicy policies = this.policyService.getPolicy(policyRequest).get();
        assertThat(policies, equalTo(multiPolicy));

    }
}
