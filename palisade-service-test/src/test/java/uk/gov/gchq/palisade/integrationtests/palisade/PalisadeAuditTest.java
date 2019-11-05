/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.integrationtests.palisade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.service.palisade.request.AuditRequest;
import uk.gov.gchq.palisade.service.palisade.request.AuditRequest.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.service.palisade.service.AuditService;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

//mocks an audit trail
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"test"})
public class PalisadeAuditTest extends BaseTestEnvironment {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockRule auditServiceMock = AUDIT_SERVICE_MOCK;

    private AuditRequest auditRequest = RegisterRequestCompleteAuditRequest.create(new RequestId().id("request-id"))
            .withUser(USER)
            .withLeafResources(Collections.singleton(LEAF_RESOURCE))
            .withContext(CONTEXT);

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

        Boolean result = this.auditService.audit(auditRequest);
        assertThat(result, is(true));
    }
}
