package guru.sfg.beer.order.service.statemachine.action;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.manager.BeerOrderManager;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.web.model.BeerOrderDto;
import lombok.AllArgsConstructor;
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
public class ValidateBeerOrderRequest implements Action<BeerOrderStateEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStateEnum, BeerOrderEventEnum> stateContext) {

        Optional.ofNullable(stateContext.getMessage().getPayload()).ifPresent(event -> {
            UUID beerId = (UUID) stateContext.getMessageHeader(BeerOrderManager.BEER_ORDER_ID);
            BeerOrder bo = beerOrderRepository.getOne(beerId);
            BeerOrderDto beerOrderDto = beerOrderMapper.beerOrderToDto(bo);
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER, beerOrderDto);
            log.debug("Send validate order to message queue id " + beerId);
        });

    }
}
