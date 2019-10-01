package uk.gov.gchq.palisade.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.service.palisade.request.GetUserRequest;
import uk.gov.gchq.palisade.service.palisade.service.UserService;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
public class PalisadeUserTest {

    Logger LOGGER = LoggerFactory.getLogger(PalisadeUserTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    public ApplicationContext context;

    @Autowired
    public UserService userService;

    @ClassRule
    public static WireMockRule userServiceMock = new WireMockRule(options().port(8083).notifier(new ConsoleNotifier(true)));

    private static GetUserRequest GET_USER_REQUEST = new GetUserRequest().userId(new UserId().id("user-id"));
    {
        GET_USER_REQUEST.setOriginalRequestId(new RequestId().id(UUID.randomUUID().toString()));
    }
    private static User USER = new User().userId("user-id");

    @Test
    public void userServiceTest() throws IOException, ExecutionException, InterruptedException {
        Arrays.stream(this.context.getBeanDefinitionNames()).forEach(bean -> LOGGER.info("name = {}", bean));
        userServiceMock.stubFor(post(urlPathMatching("/getUser"))
                .withRequestBody(containing("user-id"))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(new ObjectMapper().writeValueAsString(USER))
                ));

        final User subject = this.userService.getUser(GET_USER_REQUEST).toCompletableFuture().get();
        assertThat(subject.getUserId().getId(), is(equalTo("user-id")));
    }

}
