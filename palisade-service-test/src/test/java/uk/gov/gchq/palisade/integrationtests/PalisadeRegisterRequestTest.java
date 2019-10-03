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
package uk.gov.gchq.palisade.integrationtests;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.palisade.policy.MultiPolicy;
import uk.gov.gchq.palisade.service.palisade.policy.Policy;
import uk.gov.gchq.palisade.service.palisade.request.GetPolicyRequest;
import uk.gov.gchq.palisade.service.palisade.request.GetUserRequest;
import uk.gov.gchq.palisade.service.palisade.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"test"})
public class PalisadeRegisterRequestTest {

    @Autowired
    private PalisadeService palisadeService;

    private static ObjectMapper mapper = JSONSerialiser.getMapper();

    @ClassRule
    public static WireMockRule userServiceMock = new WireMockRule(options().port(8083).notifier(new ConsoleNotifier(true)));

    @ClassRule
    public static WireMockRule resourceServiceMock = new WireMockRule(options().port(8082).notifier(new ConsoleNotifier(true)));

    @ClassRule
    public static WireMockRule policyServiceMock = new WireMockRule(options().port(8081).notifier(new ConsoleNotifier(true)));

    @ClassRule
    public static WireMockRule auditServiceMock = new WireMockRule(options().port(8086).notifier(new ConsoleNotifier(true)));

    private static GetUserRequest GET_USER_REQUEST = new GetUserRequest().userId(new UserId().id("user-id"));
    static {
        GET_USER_REQUEST.setOriginalRequestId(new RequestId().id(UUID.randomUUID().toString()));
    }
    private static User USER = new User().userId("user-id");

    private static RegisterDataRequest REGISTER_DATA_REQUEST = new RegisterDataRequest();
    static {
        REGISTER_DATA_REQUEST.setUserId(USER.getUserId());
        REGISTER_DATA_REQUEST.setContext(new Context().purpose("integration test"));
        REGISTER_DATA_REQUEST.setResourceId("data-to-access");
    }

    @JsonPropertyOrder(value = {"class"}, alphabetic = true)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "class"
    )
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class StubService implements Service {
        @Override
        public CompletableFuture<String> process(final Request request) {
            return CompletableFuture.completedFuture(request.getId().getId());
        }
    }

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

    private static GetPolicyRequest POLICY_REQUEST = new GetPolicyRequest().user(USER).context(REGISTER_DATA_REQUEST.getContext()).resources(Collections.singletonList(LEAF_RESOURCE));

    @JsonPropertyOrder(value = {"class"}, alphabetic = true)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "class"
    )
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    private static class StubRule implements Rule<String> {
        @Override
        public String apply(String s, User user, Context context) {
            return null;
        }
    }

    private static Rule RULE = new StubRule();
    private static Policy POLICY = new Policy().owner(USER).resourceLevelRule("test rule", RULE);
    private static MultiPolicy MULTI_POLICY = new MultiPolicy().policies(Stream.of(new AbstractMap.SimpleEntry<>(LEAF_RESOURCE, POLICY)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Test
    public void registerDataRequestTest() throws JsonProcessingException, ExecutionException, InterruptedException {
        userServiceMock.stubFor(post(urlPathMatching("/getUser"))
                .withRequestBody(containing("user-id"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(getMapper().writeValueAsString(USER))
                ));

        resourceServiceMock.stubFor(post(urlPathMatching("/getResourcesById"))
                .withRequestBody(containing("data-to-access"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(getMapper().writeValueAsString(RESOURCE_INFO))
                ));

        auditServiceMock.stubFor(post(urlPathMatching("/audit"))
                .withRequestBody(containing("user-id"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("true")
                ));

        policyServiceMock.stubFor(post(urlPathMatching("/getPolicy"))
                .withRequestBody(containing("user-id"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(getMapper().writeValueAsString(MULTI_POLICY))
                ));

        DataRequestResponse result = this.palisadeService.registerDataRequest(REGISTER_DATA_REQUEST).get();
        assertThat(result, is(not(equalTo("{\"{\\\"class\\\":\\\"uk.gov.gchq.palisade.resource.impl.FileResource\\\",\\\"id\\\":\\\"data-to-access\\\",\\\"attributes\\\":{},\\\"parent\\\":{\\\"class\\\":\\\"uk.gov.gchq.palisade.resource.impl.DirectoryResource\\\",\\\"id\\\":\\\"home/\\\",\\\"parent\\\":{\\\"class\\\":\\\"uk.gov.gchq.palisade.resource.impl.SystemResource\\\",\\\"id\\\":\\\"nix/\\\"}},\\\"serialisedFormat\\\":\\\"json\\\",\\\"\n" +
                "type\\\":\\\"string\\\"}\":{\"service\":{\"class\":\"uk.gov.gchq.palisade.integrationtests.PalisadeRegisterRequestTest$StubService\",\"@id\":1,\"class\":\"uk.gov.gchq.palisade.integrationtests.PalisadeRegisterRequestTest$StubService\"}}}\n"))));
    }

}
