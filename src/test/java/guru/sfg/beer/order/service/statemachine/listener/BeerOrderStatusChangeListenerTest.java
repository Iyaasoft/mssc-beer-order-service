package guru.sfg.beer.order.service.statemachine.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import guru.sfg.beer.order.service.base.BreweryWiremockParameterResolver;
import guru.sfg.beer.order.service.base.WiremockInitialiser;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.event.BeerOrderStatusChangeEvent;
import guru.sfg.beer.order.service.web.mappers.DateMapper;
import guru.springframework.web.model.BeerDto;
import guru.springframework.web.model.BeerStyleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static guru.springframework.domain.BeerOrderStateEnum.NEW;
import static guru.springframework.domain.BeerOrderStateEnum.READY;

@ActiveProfiles("test")
@ExtendWith(BreweryWiremockParameterResolver.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = {WiremockInitialiser.class})
@TestPropertySource(properties = {"server.port=9090"})
class BeerOrderStatusChangeListenerTest {

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    ObjectMapper objectMapper;


    BeerOrderStatusChangeListener listener;

    @BeforeEach
    void setup() {
        WireMock.configureFor("localhost",9090);
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        listener = new BeerOrderStatusChangeListener(new DateMapper(), restTemplateBuilder);
    }

    @Disabled
    @Test
    void orderStatusChangeListenerTest() throws Exception {


        BeerDto dto = BeerDto.builder().id(UUID.fromString("0a818933-087d-47f2-ad83-2f986ed087eb"))
                .beerName("Pilsner")
                .beerStyle(BeerStyleEnum.IPA)
                .build();

        String responseJson = objectMapper.writeValueAsString(dto);
        WireMock.stubFor(get(WireMock.urlPathEqualTo("/api/v1/beer/"))
                .inScenario("Beer Order")
                .whenScenarioStateIs(STARTED)
                .withQueryParam("showAllInventoryOnHand", equalTo(("true")))
                .willReturn(aResponse().withBody(responseJson)
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("content-type", "application/json")));

        WireMock.stubFor(post(WireMock.urlPathEqualTo("/update"))
                .inScenario("Beer Order")
                        .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withBody("{\"success\": \"yes\"}")
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("content-type", "application/json")));

        BeerOrder order = BeerOrder.builder().id(UUID.randomUUID())
                .orderStatus(NEW)
                .orderStatusCallbackUrl("http://localhost:9090/update")
                .createdDate(Timestamp.from(OffsetDateTime.now().toInstant()))
                .build();

        BeerOrderStatusChangeEvent beerOrderStatusChangeEvent = new BeerOrderStatusChangeEvent(order, READY);

       listener.orderStatusChangeListener(beerOrderStatusChangeEvent);

        verify(1, postRequestedFor(urlPathEqualTo("/update")));
       verify(1, getRequestedFor(urlEqualTo("/api/v1/beer/([a-f0-9])")));


    }

}