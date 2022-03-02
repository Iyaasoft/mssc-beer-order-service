package guru.sfg.beer.order.service.statemachine.action;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.manager.BeerOrderManager;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.statemachine.action.event.AllocateOrderEvent;
import guru.springframework.web.model.BeerOrderDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Data
@Component
@RequiredArgsConstructor
public class AllocateBeerOrderAction implements Action<BeerOrderStateEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStateEnum, BeerOrderEventEnum> stateContext) {

            UUID beerId = (UUID) stateContext.getMessageHeader(BeerOrderManager.BEER_ORDER_ID);
            Optional<BeerOrder> bo = beerOrderRepository.findById(beerId);
            bo.ifPresentOrElse (order -> {
                jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER, AllocateOrderEvent
                        .builder()
                        .beerOrderDto(
                                beerOrderMapper.beerOrderToDto(order)
                        )
                        .build());
                log.debug("Send to Allocate order message queue, id : " + beerId);
            }, () -> log.debug("Error ALLOCATE_ORDER, beer order not found id : " + beerId));
    }
}
