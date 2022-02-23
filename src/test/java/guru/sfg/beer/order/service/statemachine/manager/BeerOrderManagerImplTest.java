package guru.sfg.beer.order.service.statemachine.manager;

import guru.sfg.beer.order.service.base.BaseInventoryTest;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.web.model.BeerOrderLineDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BeerOrderManagerImplTest extends BaseInventoryTest {

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Test
    void newBeerOrder() {
        Set<BeerOrderLine> orderLines = new HashSet<>();
        orderLines.add(getOrderDetail());
        BeerOrder beerOrder = BeerOrder.builder().beerOrderLines(orderLines).build();
        BeerOrder beerOrderSaved = beerOrderManager.newBeerOrder(beerOrder);
        assertEquals(beerOrderSaved.getOrderStatus(),BeerOrderStateEnum.NEW);



    }
}