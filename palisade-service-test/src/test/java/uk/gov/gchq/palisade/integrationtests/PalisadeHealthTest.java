package uk.gov.gchq.palisade.integrationtests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PalisadeHealthTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void registerRequestTest() {
        final String health = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

}
