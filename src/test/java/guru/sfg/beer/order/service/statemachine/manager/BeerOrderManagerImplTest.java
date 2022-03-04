package guru.sfg.beer.order.service.statemachine.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.junit.jupiter.api.Disabled;
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

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
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
        BeerOrder beerOrder = getBeerOrder();

        prepareWiremockStub(wireMockServer, "Allocated Beer");

        beerOrderManager.newBeerOrder(beerOrder);

        waitAndAssertBeerQuantities(beerOrder, BeerOrderStateEnum.ALLOCATED);

        Optional<BeerOrder> orderSaved = beerOrderRepository.findById(beerOrder.getId());
        assertEquals(BeerOrderStateEnum.ALLOCATED, orderSaved.get().getOrderStatus());
    }

    @Test
    void pickUpBeerOrder(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        BeerOrder beerOrder = getBeerOrder();

        prepareWiremockStub(wireMockServer, "Pickup Beer");

        final BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        waitAndAssertBeerQuantities(savedBeerOrder, BeerOrderStateEnum.ALLOCATED);

        beerOrderManager.pickUpBeerOrder(savedBeerOrder.getId());

        BeerOrder orderSaved = beerOrderRepository.findById(beerOrder.getId()).get();

        assertEquals(BeerOrderStateEnum.PICKED_UP, orderSaved.getOrderStatus());
    }


    @Test
    void ValidationFailedTest() throws JsonProcessingException {
        BeerOrder beerOrder = getBeerOrder();

        prepareWiremockStub(wireMockServer, "Validation Failed");

        beerOrder.setCustomerRef("fail-validation");

        beerOrderManager.newBeerOrder(beerOrder);

        waitAndAssertBeerOrderState(beerOrder, BeerOrderStateEnum.VALIDATION_EXCEPTION);
    }

    @Test
    void AllocationFailedTest() throws JsonProcessingException {
        BeerOrder beerOrder = getBeerOrder();

        prepareWiremockStub(wireMockServer, "Validation Failed");

        beerOrder.setCustomerRef("fail-allocation");

        beerOrderManager.newBeerOrder(beerOrder);

        waitAndAssertBeerOrderState(beerOrder, BeerOrderStateEnum.ALLOCATION_EXCEPTION);


    }

    @Test
    void AllocationCancelledTest() throws JsonProcessingException {
        BeerOrder beerOrder = getBeerOrder();

        prepareWiremockStub(wireMockServer, "Cancel Order");

        beerOrder.setCustomerRef("allocation-cancelled");

        beerOrderManager.newBeerOrder(beerOrder);

        waitAndAssertBeerOrderState(beerOrder, BeerOrderStateEnum.CANCELLED);


    }

    @Disabled
    @Test
    void deliverBeerOrder(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        BeerOrder beerOrder = getBeerOrder();

        prepareWiremockStub(wireMockServer, "Order Delivered");

        final BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        waitAndAssertBeerQuantities(savedBeerOrder, BeerOrderStateEnum.ALLOCATED );

        beerOrderManager.pickUpBeerOrder(savedBeerOrder.getId());

        waitAndAssertBeerOrderState(beerOrder, BeerOrderStateEnum.PICKED_UP);

        beerOrderManager.deliverBeerOrder(savedBeerOrder.getId());

        waitAndAssertBeerOrderState(beerOrder, BeerOrderStateEnum.DELIVERED);
    }

    private void waitAndAssertBeerOrderState(BeerOrder beerOrder, BeerOrderStateEnum state) {
        await().atMost(30, SECONDS).until(
                () -> {
        BeerOrder orderSaved  = beerOrderRepository.findById(beerOrder.getId()).get();

        assertEquals(state, orderSaved.getOrderStatus());
                    return true;
         });
    }

    private BeerOrder getBeerOrder() {
        Set<BeerOrderLine> orderLines = new HashSet<>();
        orderLines.add(getOrderDetail());
        BeerOrder beerOrder = BeerOrder.builder().beerOrderLines(orderLines).build();
        return beerOrder;
    }

    private void prepareWiremockStub(WireMockServer wireMockServer, String Pickup_Beer) throws JsonProcessingException {
        BeerDto dto = BeerDto.builder().id(UUID.fromString("5456319d-7b6f-41e0-a6e3-2365f3a6d196")).beerName("Pilsner").beerStyle(BeerStyleEnum.IPA).build();
        String responseJson = mapper.writeValueAsString(dto);
        wireMockServer.stubFor(get(WireMock.anyUrl())
                .inScenario(Pickup_Beer)
                .whenScenarioStateIs(STARTED)
                //.withQueryParam("showAllInventoryOnHand", equalTo(("true")))
                .willReturn(aResponse().withBody(responseJson)
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("content-type", "application/json")));
    }

    private void waitAndAssertBeerQuantities(BeerOrder savedBeerOrder, BeerOrderStateEnum state) {
        await().atMost(30, SECONDS).until(
                () -> {
                    BeerOrder found  = beerOrderRepository.findById(savedBeerOrder.getId()).get();
                    assertEquals(state, found.getOrderStatus());
                    found.getBeerOrderLines().forEach(line ->
                            assertEquals(line.getOrderQuantity(),line.getQuantityAllocated())
                    );
                    return true;
                });
    }


}