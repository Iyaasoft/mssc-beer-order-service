package guru.sfg.beer.order.service.statemachine.action;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.manager.BeerOrderManager;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class CancelBeerOrderAction implements Action<BeerOrderStateEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;

    @Override
    public void execute(StateContext<BeerOrderStateEnum, BeerOrderEventEnum> stateContext) {
        UUID beerId = (UUID) stateContext.getMessageHeader(BeerOrderManager.BEER_ORDER_ID);
        Optional<BeerOrder> beerOrder = beerOrderRepository.findById(beerId);
        beerOrder.ifPresentOrElse(order -> {


        }, () -> log.debug("Error could not find order to cancel, id :" + beerId));
        log.debug("Compensating transaction ... canceling order id : "+beerId);
    }
}
