package guru.sfg.beer.order.service.statemachine;

import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class BeerOrderStateMachine extends StateMachineConfigurerAdapter<BeerOrderStateEnum, BeerOrderEventEnum> {

    @Override
    public void configure(StateMachineStateConfigurer<BeerOrderStateEnum, BeerOrderEventEnum> states) throws Exception {

        states.withStates()
                .initial(BeerOrderStateEnum.NEW)
                .states(EnumSet.allOf(BeerOrderStateEnum.class))
                .end(BeerOrderStateEnum.PICKED_UP)
                .end(BeerOrderStateEnum.DELIVERED)
                .end(BeerOrderStateEnum.DELIVERY_EXCEPTION)
                .end(BeerOrderStateEnum.VALIDATION_EXCEPTION)
                .end(BeerOrderStateEnum.ALLOCATION_EXCEPTION);
    }


    @Override
    public void configure(StateMachineTransitionConfigurer<BeerOrderStateEnum, BeerOrderEventEnum> transitions) throws Exception {

        transitions.withExternal().source(BeerOrderStateEnum.NEW).target(BeerOrderStateEnum.NEW).event(BeerOrderEventEnum.VALIDATE_ORDER)
            .and()
                .withExternal().source(BeerOrderStateEnum.NEW).target(BeerOrderStateEnum.VALIDATED)
                .event(BeerOrderEventEnum.VALIDATION_PASSED)
            .and()
                .withExternal().source(BeerOrderStateEnum.NEW).target(BeerOrderStateEnum.VALIDATION_EXCEPTION)
                .event(BeerOrderEventEnum.VALIDATION_FAILED)
            .and()
                .withExternal().source(BeerOrderStateEnum.VALIDATED).target(BeerOrderStateEnum.ALLOCATION)
                .event(BeerOrderEventEnum.ALLOCATION_SUCCESS)

                .and().withExternal().source(BeerOrderStateEnum.ALLOCATION).target(BeerOrderStateEnum.ALLOCATION_PENDING).event(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)
                .and().withExternal().source(BeerOrderStateEnum.ALLOCATION).target(BeerOrderStateEnum.ALLOCATION_EXCEPTION).event(BeerOrderEventEnum.ALLOCATION_FAILED)
                .and().withExternal().source(BeerOrderStateEnum.ALLOCATION).target(BeerOrderStateEnum.PICKED_UP).event(BeerOrderEventEnum.ORDER_COLLECTED)
                .and().withExternal().source(BeerOrderStateEnum.PICKED_UP).target(BeerOrderStateEnum.DELIVERED).event(BeerOrderEventEnum.ORDER_DELIVERED)
                .and().withExternal().source(BeerOrderStateEnum.PICKED_UP).target(BeerOrderStateEnum.DELIVERY_EXCEPTION).event(BeerOrderEventEnum.ORDER_DELIVERY_FAILED);

    }
}
