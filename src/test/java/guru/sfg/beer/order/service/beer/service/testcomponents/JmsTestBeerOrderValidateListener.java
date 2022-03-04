package guru.sfg.beer.order.service.beer.service.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.springframework.services.messages.BeerOrderValidationResult;
import guru.springframework.statemachine.action.event.ValidateOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
@Component
public class JmsTestBeerOrderValidateListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER)
    public void validateMsgListener(Message message) {

        System.out.println("+++++++  IM RUNNING ++++++++++ ");
        boolean isValid = true;
        ValidateOrderEvent event = (ValidateOrderEvent) message.getPayload();
        // return an invalid state
        if("fail-validation".equals(event.getBeerOrderDto().getCustomerRef())){
            isValid = false;
        }
        log.debug("test send listener  Validate result msg to q "+event);

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESULT,
                BeerOrderValidationResult.builder()
                        .beerOrderId(event
                                .getBeerOrderDto().getId())
                                .isValid(isValid)
                        .build());

    }
}
