package guru.sfg.beer.order.service.statemachine.listener;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.statemachine.manager.BeerOrderManager;
import guru.springframework.services.messages.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateOrderResponseListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESULT)
    public void handle(@Payload AllocateOrderResult allocateOrderResult) {
        log.debug("Handle allocation response for order : " + allocateOrderResult.getBeerOrder().getId());
        beerOrderManager.sendBeerOrderAllocationResult(allocateOrderResult.getBeerOrder(),
                allocateOrderResult.isAllocated(),
                allocateOrderResult.isAllocationError());
    }
}
