package uk.gov.gchq.palisade.integrationtests.palisade;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;
import uk.gov.gchq.palisade.service.palisade.config.ApplicationConfiguration;

@SpringBootApplication
@EnableFeignClients(basePackages = "uk.gov.gchq.palisade.service.palisade.web")
@Import(ApplicationConfiguration.class)
public class IntegrationTestApplication {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(PalisadeApplication.class).web(args.length == 0 ? WebApplicationType.SERVLET : WebApplicationType.NONE)
                .run(args);
    }

}