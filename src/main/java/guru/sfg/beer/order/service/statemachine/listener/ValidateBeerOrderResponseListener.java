package guru.sfg.beer.order.service.statemachine.listener;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.statemachine.manager.BeerOrderManager;
import guru.springframework.services.messages.BeerOrderValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateBeerOrderResponseListener {

    private  final BeerOrderManager beerOrderManager;

    @Transactional
    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESULT)
    public void handleResponse(@Payload BeerOrderValidationResult beerOrderValidationResult) {
        if(beerOrderValidationResult.isValid()){
            log.debug("Send validate passed to state machine id : " + beerOrderValidationResult.getBeerOrderId());
            beerOrderManager.sendBeerOrderValidationResult(beerOrderValidationResult.getBeerOrderId(), beerOrderValidationResult.isValid());

        }
    }
}
