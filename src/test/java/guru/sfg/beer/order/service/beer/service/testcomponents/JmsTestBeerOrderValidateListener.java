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
        ValidateOrderEvent event = (ValidateOrderEvent) message.getPayload();
        log.debug("test send listener  Validate result msg to q "+event);

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESULT,
                BeerOrderValidationResult.builder()
                        .beerOrderId(event
                                .getBeerOrderDto().getId())
                                .isValid(true)
                        .build());

    }
}
