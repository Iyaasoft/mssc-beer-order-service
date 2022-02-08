package guru.sfg.beer.order.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "sgf.order", ignoreInvalidFields = true)
public abstract class OrderConfig {

    protected String beerServiceHost;

    public void setBeerServiceHost(String beerServiceHost) {

        this.beerServiceHost = beerServiceHost;
    }
}

