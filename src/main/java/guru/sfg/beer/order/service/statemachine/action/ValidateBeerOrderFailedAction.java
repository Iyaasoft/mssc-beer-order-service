package guru.sfg.beer.order.service.statemachine.action;

import guru.sfg.beer.order.service.statemachine.manager.BeerOrderManager;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;
// handle the alternate flow can raise incident ticket, callback user etc.
// just for course log error
@Slf4j
@Component
public class ValidateBeerOrderFailedAction implements Action<BeerOrderStateEnum, BeerOrderEventEnum> {


    @Override
    public void execute(StateContext<BeerOrderStateEnum, BeerOrderEventEnum> stateContext) {
        UUID beerId = (UUID) stateContext.getMessageHeader(BeerOrderManager.BEER_ORDER_ID);
        log.error("Compensating transaction.... validation step failed: "+ beerId);
    }
}
