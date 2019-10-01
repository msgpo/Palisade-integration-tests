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
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.service.palisade.request.GetUserRequest;
import uk.gov.gchq.palisade.service.palisade.service.UserService;

import java.io.IOException;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"test"})
public class PalisadeUserTest {

    @Autowired
    public UserService userService;

    @ClassRule
    public static WireMockRule userServiceMock = new WireMockRule(options().port(8083).notifier(new ConsoleNotifier(true)));

    private static GetUserRequest GET_USER_REQUEST = new GetUserRequest().userId(new UserId().id("user-id"));
    static {
        GET_USER_REQUEST.setOriginalRequestId(new RequestId().id(UUID.randomUUID().toString()));
    }
    private static User USER = new User().userId("user-id");

    @Test
    public void userServiceTest() throws IOException, ExecutionException, InterruptedException {
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
