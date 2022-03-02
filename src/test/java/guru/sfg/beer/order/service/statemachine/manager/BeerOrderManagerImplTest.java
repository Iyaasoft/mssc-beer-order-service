package guru.sfg.beer.order.service.statemachine.manager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import guru.sfg.beer.order.service.base.BaseInventoryTest;
import guru.sfg.beer.order.service.base.BreweryWiremockParameterResolver;
import guru.sfg.beer.order.service.base.WiremockInitialiser;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.web.model.BeerDto;
import guru.springframework.web.model.BeerStyleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(BreweryWiremockParameterResolver.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = {WiremockInitialiser.class})
@TestPropertySource(properties = {"server.port=9090"})
class BeerOrderManagerImplTest extends BaseInventoryTest {

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    WireMockServer wireMockServer;

   // @Transactional
    @Test
    void allocatedBeerOrder(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        Set<BeerOrderLine> orderLines = new HashSet<>();
        orderLines.add(getOrderDetail());
        BeerOrder beerOrder = BeerOrder.builder().beerOrderLines(orderLines).build();

        BeerDto dto = BeerDto.builder().id(UUID.fromString("5456319d-7b6f-41e0-a6e3-2365f3a6d196")).beerName("Pilsner").beerStyle(BeerStyleEnum.IPA).build();
        String responseJson = mapper.writeValueAsString(dto);
//""/api/v1/beer/([a-f0-9])?showAllInventoryOnHand=true")  ([/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}/])
        wireMockServer.stubFor(get(WireMock.anyUrl())
                .inScenario("Allocated Beer")
                .whenScenarioStateIs(STARTED)
                //.withQueryParam("showAllInventoryOnHand", equalTo(("true")))
                .willReturn(aResponse().withBody(responseJson)
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("content-type", "application/json")));

        beerOrderManager.newBeerOrder(beerOrder);
        await().atMost(5, SECONDS).until(
                () -> {
                    BeerOrder found  = beerOrderRepository.findById(beerOrder.getId()).get();
                    assertEquals(BeerOrderStateEnum.ALLOCATED, found.getOrderStatus());
                    return true;
                });
        Optional<BeerOrder> orderSaved = beerOrderRepository.findById(beerOrder.getId());
        assertEquals(BeerOrderStateEnum.ALLOCATED, orderSaved.get().getOrderStatus());
    }

    private Callable<Boolean> orderHasUpdated(UUID id) {

        return () -> true;
    }
}