package guru.sfg.beer.order.service.beer.service.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.services.messages.AllocateOrderResult;
import guru.springframework.statemachine.action.event.AllocateOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static guru.springframework.domain.BeerOrderEventEnum.ALLOCATE_ORDER;

@Slf4j
@RequiredArgsConstructor
@Component
public class JmsTestBeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER)
    public void allocateOrderListener(Message message) {
        log.debug("send allocated result to  -> ALLOCATE_ORDER_RESULT");
        AllocateOrderEvent event = (AllocateOrderEvent) message.getPayload();

        event.getBeerOrderDto().getBeerOrderLines().forEach(line -> {
            line.setQuantityAllocated(line.getOrderQuantity());
        });
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESULT,  AllocateOrderResult.builder()
                .allocated(true)
                .allocationError(false)
                .beerOrder(event.getBeerOrderDto())
                .build());
    }
}
