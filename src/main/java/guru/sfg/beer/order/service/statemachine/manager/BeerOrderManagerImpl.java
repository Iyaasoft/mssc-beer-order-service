package guru.sfg.beer.order.service.statemachine.manager;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.interceptor.BeerOrderStateChangeInterceptor;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStateEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStateEnum.NEW);
        BeerOrder bo = beerOrderRepository.save(beerOrder);
        sendValidationEvent(bo, BeerOrderEventEnum.VALIDATE_ORDER);
        return beerOrderRepository.save(beerOrder);

    }

    @Override
    public void sendBeerOrderValidationResult(UUID beerOrderId, boolean valid) {
        BeerOrder beerOrder =  beerOrderRepository.findOneById(beerOrderId);
        StateMachine<BeerOrderStateEnum, BeerOrderEventEnum> sm = getStateMachine(beerOrder);
        if(valid) {

            sm.sendEvent(BeerOrderEventEnum.VALIDATION_PASSED);
        } else {
            sm.sendEvent(BeerOrderEventEnum.VALIDATION_FAILED);
        }
    }

    private void sendValidationEvent(BeerOrder bo,  BeerOrderEventEnum beerOrderEventEnum) {
        StateMachine<BeerOrderStateEnum, BeerOrderEventEnum> sm = getStateMachine(bo);
        Message msg = MessageBuilder.withPayload(beerOrderEventEnum).setHeader(BEER_ORDER_ID, bo.getId()).build();
        sm.sendEvent(msg);

    }

    private StateMachine<BeerOrderStateEnum, BeerOrderEventEnum> getStateMachine(BeerOrder beerOrder) {
        StateMachine<BeerOrderStateEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());
        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(
                sma -> {
                    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(),null,null,null));
                }
        );
        sm.start();

        return sm;
    }
}
