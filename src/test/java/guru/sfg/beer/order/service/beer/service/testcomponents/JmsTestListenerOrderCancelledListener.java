package guru.sfg.beer.order.service.beer.service.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.statemachine.action.event.CancelOrderEvent;
import guru.springframework.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class JmsTestListenerOrderCancelledListener {
    private final BeerOrderRepository beerOrderRepository;

    @JmsListener(destination = JmsConfig.CANCELLED_ORDER_Q)
    public void cancelOrderListener(@Payload CancelOrderEvent event) {

        Optional<BeerOrder> beerOrder = beerOrderRepository.findById(event.getBeerOrderDto().getId());
        beerOrder.ifPresentOrElse(order -> {
                order.getBeerOrderLines().forEach(item -> {
                    item.setOrderQuantity(item.getOrderQuantity() + item.getQuantityAllocated());
                });
                order.setOrderStatus(BeerOrderStateEnum.CANCELLED);
                beerOrderRepository.saveAndFlush(order);
        },()->log.debug("Error inventory could not find beer order id : "+event.getBeerOrderDto().getId()));
    }
}
