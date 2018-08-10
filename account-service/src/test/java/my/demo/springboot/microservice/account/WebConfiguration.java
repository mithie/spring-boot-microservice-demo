package my.demo.springboot.microservice.account;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
public class WebConfiguration extends RepositoryRestMvcConfiguration {

    public WebConfiguration(final ApplicationContext context,
            final ObjectFactory<ConversionService> conversionService) {
        super(context, conversionService);
    }
}
