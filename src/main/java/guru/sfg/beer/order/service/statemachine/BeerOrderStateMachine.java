package guru.sfg.beer.order.service.statemachine;

import guru.sfg.beer.order.service.statemachine.action.AllocateBeerOrderAction;
import guru.sfg.beer.order.service.statemachine.action.ValidateBeerOrderAction;
import guru.sfg.beer.order.service.statemachine.interceptor.BeerOrderStateChangeInterceptor;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@RequiredArgsConstructor
@Configuration
@EnableStateMachineFactory
public class BeerOrderStateMachine extends StateMachineConfigurerAdapter<BeerOrderStateEnum, BeerOrderEventEnum> {

    private final Action<BeerOrderStateEnum,BeerOrderEventEnum> allocateBeerOrderAction;
    private final Action<BeerOrderStateEnum,BeerOrderEventEnum>  validateBeerOrderAction;
    private final Action<BeerOrderStateEnum,BeerOrderEventEnum> validateBeerOrderFailedAction;
    private final Action<BeerOrderStateEnum,BeerOrderEventEnum> allocationBeerOrderFailedAction;
    private final Action<BeerOrderStateEnum,BeerOrderEventEnum> cancelBeerOrderAction;

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStateEnum, BeerOrderEventEnum> states) throws Exception {

        states.withStates()
                .initial(BeerOrderStateEnum.NEW)
                .states(EnumSet.allOf(BeerOrderStateEnum.class))
                .end(BeerOrderStateEnum.PICKED_UP)
                .end(BeerOrderStateEnum.DELIVERED)
                .end(BeerOrderStateEnum.CANCELLED)
                .end(BeerOrderStateEnum.DELIVERY_EXCEPTION)
                .end(BeerOrderStateEnum.VALIDATION_EXCEPTION)
                .end(BeerOrderStateEnum.ALLOCATION_EXCEPTION);
    }



    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStateEnum, BeerOrderEventEnum> transitions) throws Exception {

        transitions.withExternal().source(BeerOrderStateEnum.NEW).target(BeerOrderStateEnum.VALIDATION_PENDING)
                .event(BeerOrderEventEnum.VALIDATE_ORDER)
                .action(validateBeerOrderAction)
            .and()
                .withExternal().source(BeerOrderStateEnum.VALIDATION_PENDING).target(BeerOrderStateEnum.VALIDATED)
                .event(BeerOrderEventEnum.VALIDATION_PASSED)
            .and()
                .withExternal().source(BeerOrderStateEnum.VALIDATION_PENDING).target(BeerOrderStateEnum.VALIDATION_EXCEPTION)
                .event(BeerOrderEventEnum.VALIDATION_FAILED)
                .action(validateBeerOrderFailedAction)
            .and()
                .withExternal().source(BeerOrderStateEnum.VALIDATION_PENDING).target(BeerOrderStateEnum.CANCELLED)
                .event(BeerOrderEventEnum.CANCEL_ORDER)
            .and()
                .withExternal().source(BeerOrderStateEnum.VALIDATED).target(BeerOrderStateEnum.ALLOCATION_PENDING)
                .event(BeerOrderEventEnum.ALLOCATE_ORDER)
                .action(allocateBeerOrderAction)
                .and()
                .withExternal().source(BeerOrderStateEnum.VALIDATED).target(BeerOrderStateEnum.CANCELLED)
                .event(BeerOrderEventEnum.CANCEL_ORDER)
            .and()
                .withExternal().source(BeerOrderStateEnum.ALLOCATION_PENDING).target(BeerOrderStateEnum.ALLOCATED)
                .event(BeerOrderEventEnum.ALLOCATION_SUCCESS)
            .and()
                .withExternal().source(BeerOrderStateEnum.ALLOCATION_PENDING).target(BeerOrderStateEnum.ALLOCATED)
                .event(BeerOrderEventEnum.CANCEL_ORDER)
            .and()
                .withExternal().source(BeerOrderStateEnum.ALLOCATION_PENDING).target(BeerOrderStateEnum.ALLOCATION_EXCEPTION)
                .event(BeerOrderEventEnum.ALLOCATION_FAILED)
            .and().withExternal().source(BeerOrderStateEnum.ALLOCATION_PENDING).target(BeerOrderStateEnum.PENDING_INVENTORY)
                .event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)
            .and().withExternal().source(BeerOrderStateEnum.ALLOCATED).target(BeerOrderStateEnum.ALLOCATED)
            .and()
                .withExternal().source(BeerOrderStateEnum.ALLOCATED).target(BeerOrderStateEnum.CANCELLED)
                .event(BeerOrderEventEnum.CANCEL_ORDER)
                .action(allocationBeerOrderFailedAction)
            .and().withExternal().source(BeerOrderStateEnum.ALLOCATED).target(BeerOrderStateEnum.PICKED_UP)
                .event(BeerOrderEventEnum.ORDER_FOR_COLLECTION)
           .and().withExternal().source(BeerOrderStateEnum.PICKED_UP).target(BeerOrderStateEnum.DELIVERED)
                .event(BeerOrderEventEnum.ORDER_DELIVERED)
           .and().withExternal().source(BeerOrderStateEnum.PICKED_UP).target(BeerOrderStateEnum.DELIVERY_EXCEPTION)
                .event(BeerOrderEventEnum.ORDER_DELIVERY_FAILED);

    }
}
