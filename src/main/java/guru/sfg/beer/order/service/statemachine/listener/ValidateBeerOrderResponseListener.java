package guru.sfg.beer.order.service.statemachine.listener;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.statemachine.manager.BeerOrderManager;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.services.validate.BeerOrderValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

import javax.jms.Message;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateBeerOrderResponseListener {

    private  final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESULT)
    public void handleResponse(@Payload BeerOrderValidationResult beerOrderValidationResult) {
        if(beerOrderValidationResult.isValid()){
            beerOrderManager.sendBeerOrderValidationResult(beerOrderValidationResult.getBeerOrderId(), beerOrderValidationResult.isValid());
            log.debug("Send validate passed to state machine id : " + beerOrderValidationResult.getBeerOrderId());
        }
    }
}
