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

package uk.gov.gchq.palisade.integrationtests.data.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.integrationtests.data.util.TestUtil;
import uk.gov.gchq.palisade.policy.PassThroughRule;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.request.DataRequestConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class PalisadeServiceMock {

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8084).notifier(new ConsoleNotifier(true)));
    }

    public static DataRequestConfig getDataRequestConfig() throws JsonProcessingException {
        Map<LeafResource, Rules> leafResourceToRules = new HashMap<>();
        Path path = Paths.get("./resources/data/employee_file0.avro").toAbsolutePath().normalize();
        FileResource resource = TestUtil.createFileResource(path, "uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee");
        Rules rules = new Rules().addRule("Test Rule", new PassThroughRule<>());
        leafResourceToRules.put(resource, rules);

        DataRequestConfig response = new DataRequestConfig()
                .user(new User().userId("userId").auths("auths").roles("roles"))
                .context(new Context().purpose("purpose"))
                .rules(leafResourceToRules);
        response.setOriginalRequestId(new RequestId().id("original"));

        return response;
    }

    public static void stubRule(final WireMockRule serviceMock, final ObjectMapper serializer) throws JsonProcessingException {
        serviceMock.stubFor(post(urlEqualTo("/getDataRequestConfig"))
                .willReturn(
                        okJson(serializer.writeValueAsString(getDataRequestConfig()))
                ));
    }

}
