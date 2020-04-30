/*
 * Copyright 2020 Crown Copyright
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

package uk.gov.gchq.palisade.integrationtests.palisade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.integrationtests.palisade.mock.AuditServiceMock;
import uk.gov.gchq.palisade.integrationtests.palisade.mock.PolicyServiceMock;
import uk.gov.gchq.palisade.integrationtests.palisade.mock.ResourceServiceMock;
import uk.gov.gchq.palisade.integrationtests.palisade.mock.UserServiceMock;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;
import uk.gov.gchq.palisade.service.palisade.repository.PersistenceLayer;
import uk.gov.gchq.palisade.service.palisade.request.GetDataRequestConfig;
import uk.gov.gchq.palisade.service.palisade.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PalisadeApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) //reset db after each test
@EnableJpaRepositories(basePackages = {"uk.gov.gchq.palisade.service.palisade.repository"})
public class PalisadeComponentTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper serializer;
    @Autowired
    private PalisadeService palisadeService;
    @Autowired
    private PersistenceLayer persistenceLayer;

    @Rule
    public WireMockRule auditMock = AuditServiceMock.getRule();
    @Rule
    public WireMockRule policyMock = PolicyServiceMock.getRule();
    @Rule
    public WireMockRule resourceMock = ResourceServiceMock.getRule();
    @Rule
    public WireMockRule userMock = UserServiceMock.getRule();

    @Before
    public void setUp() throws JsonProcessingException {
        AuditServiceMock.stubRule(auditMock, serializer);
        AuditServiceMock.stubHealthRule(auditMock, serializer);
        PolicyServiceMock.stubRule(policyMock, serializer);
        PolicyServiceMock.stubHealthRule(policyMock, serializer);
        ResourceServiceMock.stubRule(resourceMock, serializer);
        ResourceServiceMock.stubHealthRule(resourceMock, serializer);
        UserServiceMock.stubRule(userMock, serializer);
        UserServiceMock.stubHealthRule(userMock, serializer);
        serializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void contextLoads() {
        assertNotNull(palisadeService);
    }

    @Test
    public void isUp() {
        final String health = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Ignore
    @Test
    public void allServicesDown() {
        //Given all services are down
        auditMock.stop();
        policyMock.stop();
        resourceMock.stop();
        userMock.stop();
        //Then the Palisade Service also reports down.
        final String downHealth = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(downHealth, is(equalTo("{\"status\":\"DOWN\"}")));

        //When the services start one by one
        auditMock.start();
        //Then Palisade service still shows as down
        final String auditDownHealth = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(auditDownHealth, is(equalTo("{\"status\":\"DOWN\"}")));

        //When the services start one by one
        policyMock.start();
        //Then Palisade service still shows as down
        final String policyDownHealth = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(policyDownHealth, is(equalTo("{\"status\":\"DOWN\"}")));

        //When the resource service starts
        resourceMock.start();
        //Then Palisade service still shows as down
        final String resourceDownHealth = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(resourceDownHealth, is(equalTo("{\"status\":\"DOWN\"}")));

        //When the final service starts
        userMock.start();
        //Then Palisade service shows as up
        final String allUpHealth = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(allUpHealth, is(equalTo("{\"status\":\"UP\"}")));
    }


    @Test
    public void registerDataRequestTest() {
        // Given all other services are mocked
        assumeTrue(auditMock.isRunning());
        assumeTrue(policyMock.isRunning());
        assumeTrue(resourceMock.isRunning());
        assumeTrue(userMock.isRunning());

        // When
        RegisterDataRequest request = new RegisterDataRequest().userId(new UserId().id("user-id")).resourceId("resource-id").context(new Context().purpose("purpose"));
        DataRequestResponse response = restTemplate.postForObject("/registerDataRequest", request, DataRequestResponse.class);

        // Then
        assertThat(response.getResources(), is(ResourceServiceMock.getResources()));
    }

    @Test
    public void getDataRequestConfigTest() throws InterruptedException {
        // Given all other services are mocked
        assumeTrue(auditMock.isRunning());
        assumeTrue(policyMock.isRunning());
        assumeTrue(resourceMock.isRunning());
        assumeTrue(userMock.isRunning());
        // Given a data request has been registered
        RegisterDataRequest dataRequest = new RegisterDataRequest().userId(new UserId().id("user-id")).resourceId("resource-id").context(new Context().purpose("purpose"));
        DataRequestResponse dataResponse = restTemplate.postForObject("/registerDataRequest", dataRequest, DataRequestResponse.class);
        assertNotNull(dataResponse);

        // When the data service requests the request config
        for (Resource resource : ResourceServiceMock.getResources().keySet()) {
            GetDataRequestConfig configRequest = (GetDataRequestConfig) new GetDataRequestConfig()
                    .token(new RequestId().id(dataResponse.getToken()))
                    .resource(resource)
                    .originalRequestId(dataResponse.getOriginalRequestId());
            DataRequestConfig configResponse = restTemplate.postForObject("/getDataRequestConfig", configRequest, DataRequestConfig.class);

            // Then the config response is consistent with the data request
            assertThat(configResponse.getUser(), is(UserServiceMock.getUser()));
            assertThat(configResponse.getRules().keySet(), is(Collections.singleton(resource)));
        }
    }
}
