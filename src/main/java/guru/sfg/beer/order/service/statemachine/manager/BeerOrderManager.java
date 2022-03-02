package guru.sfg.beer.order.service.statemachine.manager;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.springframework.web.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {

    String BEER_ORDER_ID = "beerOrderId";
    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void sendBeerOrderValidationResult(UUID beerOrderId, boolean valid);

    void sendBeerOrderAllocationResult(BeerOrderDto beerOrder, boolean allocated, boolean allocationError);

    void pickUpBeerOrder(UUID beerId);

    void deliverBeerOrder(UUID id);
}
