package guru.sfg.beer.order.service.statemachine.manager;

import guru.sfg.beer.order.service.domain.BeerOrder;

import java.util.UUID;

public interface BeerOrderManager {

    String BEER_ORDER_ID = "beerOrderId";
    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void sendBeerOrderValidationResult(UUID beerOrderId, boolean valid);
}
