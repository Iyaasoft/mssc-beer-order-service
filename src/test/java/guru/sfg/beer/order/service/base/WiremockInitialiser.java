package guru.sfg.beer.order.service.base;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

@Configuration
public class WiremockInitialiser implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static WireMockRuntimeInfo wireMockRuntimeInfo;

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

        WireMockServer wireMockServer = new WireMockServer(8082);
        wireMockRuntimeInfo =  new WireMockRuntimeInfo(wireMockServer);

        wireMockServer.start();
        configurableApplicationContext.getBeanFactory().registerSingleton("wireMockServer", wireMockServer);
        configurableApplicationContext.getBeanFactory().registerSingleton("wireMockRuntimeInfo", wireMockRuntimeInfo);

        configurableApplicationContext.addApplicationListener(applicationEvent -> {
            if (applicationEvent instanceof ContextClosedEvent) {
                wireMockServer.stop();
            }
        });

    }
  public static WireMockRuntimeInfo getWireMockRuntimeInfo() {
        return wireMockRuntimeInfo;
  }
}