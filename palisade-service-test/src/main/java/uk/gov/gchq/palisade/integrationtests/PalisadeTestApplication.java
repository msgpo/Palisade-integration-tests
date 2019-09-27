package uk.gov.gchq.palisade.integrationtests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;

@SpringBootApplication
public class PalisadeTestApplication {

    private final static Logger LOGGER = LoggerFactory.getLogger(PalisadeTestApplication.class);

    /**
     * Application entry point
     * @param args from the command line
     */
    public static void main(final String[] args) {
         new SpringApplicationBuilder(PalisadeApplication.class).web(args.length == 0 ? WebApplicationType.SERVLET : WebApplicationType.NONE)
                .run(args);
    }

    @Bean
    CommandLineRunner runner() {
        return args -> {
            LOGGER.info("Starting {}...", PalisadeTestApplication.class.getSimpleName());
        };
    }

}
