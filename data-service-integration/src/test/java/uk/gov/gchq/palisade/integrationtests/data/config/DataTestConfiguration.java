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

package uk.gov.gchq.palisade.integrationtests.data.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.gchq.palisade.integrationtests.data.web.DataClient;
import uk.gov.gchq.palisade.integrationtests.data.web.DataClientWrapper;
import uk.gov.gchq.palisade.service.data.service.DataService;

@Configuration
@EnableAutoConfiguration
public class DataTestConfiguration {

    @Bean
    public DataClientWrapper dataClientWrapper(final DataClient client, final DataService service) {
        return new DataClientWrapper(client, service);
    }
}
