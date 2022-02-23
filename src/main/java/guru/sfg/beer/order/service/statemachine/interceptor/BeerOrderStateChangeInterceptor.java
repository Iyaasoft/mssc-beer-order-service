package guru.sfg.beer.order.service.statemachine.interceptor;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.manager.BeerOrderManager;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStateEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;

    @Override
    public void postStateChange(State<BeerOrderStateEnum, BeerOrderEventEnum> state, Message<BeerOrderEventEnum> message, Transition<BeerOrderStateEnum, BeerOrderEventEnum> transition, StateMachine<BeerOrderStateEnum, BeerOrderEventEnum> stateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(String.class.cast(msg.getHeaders()
                    .getOrDefault(BeerOrderManager.BEER_ORDER_ID, ""))
            ).ifPresent(id -> {
                log.debug("Saving state of beer order : " + id);
                BeerOrder bo = beerOrderRepository.findOneById(UUID.fromString(id));
                bo.setOrderStatus(state.getId());
                beerOrderRepository.save(bo);
            });
        });
    }
}