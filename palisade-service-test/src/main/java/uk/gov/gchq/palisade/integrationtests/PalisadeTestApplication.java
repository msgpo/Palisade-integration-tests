package uk.gov.gchq.palisade.integrationtests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PalisadeTestApplication implements CommandLineRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(PalisadeTestApplication.class);

    /**
     * Application entry point
     * @param args from the command line
     */
    public static void main(final String[] args) {
         new SpringApplicationBuilder(PalisadeTestApplication.class).web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Starting {}...", PalisadeTestApplication.class.getSimpleName());
    }
}
