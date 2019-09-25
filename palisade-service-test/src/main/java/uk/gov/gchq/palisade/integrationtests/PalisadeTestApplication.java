package uk.gov.gchq.palisade.integrationtests;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PalisadeTestApplication {

    /**
     * Application entry point
     * @param args from the command line
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(PalisadeTestApplication.class).web(WebApplicationType.SERVLET)
                .run(args);
    }

}
