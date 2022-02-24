package guru.sfg.beer.order.service.statemachine.manager;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.interceptor.BeerOrderStateChangeInterceptor;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.web.model.BeerOrderDto;
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
        sendOrderEvent(bo, BeerOrderEventEnum.VALIDATE_ORDER);
        return beerOrderRepository.save(beerOrder);

    }

    @Override
    public void sendBeerOrderValidationResult(UUID beerOrderId, boolean valid) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);
        StateMachine<BeerOrderStateEnum, BeerOrderEventEnum> sm = getStateMachine(beerOrder);
        if (valid) {

            sendOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            beerOrder = beerOrderRepository.findOneById(beerOrderId);

            sendOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);

        } else {
            sendOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }
    }

    @Override
    public void sendBeerOrderAllocationResult(BeerOrderDto beerOrderDto, boolean allocated, boolean allocationError) {

        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        if (allocated) {
            sendAllocationSuccessMsg(beerOrder, beerOrderDto);
        } else if (allocationError) {
            sendAllocationExceptionMsg(beerOrder, beerOrderDto);
        } else {
            sendAllocationNoInventoryMsg(beerOrder, beerOrderDto);
        }
    }


    private void sendAllocationNoInventoryMsg(BeerOrder beerOrder, BeerOrderDto beerOrderDto) {

        beerOrder.setOrderStatus(BeerOrderStateEnum.PENDING_INVENTORY);
        BeerOrder beerOrderFound = beerOrderRepository.getOne(beerOrder.getId());
        sendOrderEvent( beerOrderFound , BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
        updateOrderQuantities(beerOrderDto,beerOrder);
    }

    private void sendAllocationSuccessMsg(BeerOrder beerOrder, BeerOrderDto beerOrderDto) {
        beerOrder.setOrderStatus(BeerOrderStateEnum.ALLOCATED);
        BeerOrder beerOrderFound = beerOrderRepository.getOne(beerOrder.getId());
        sendOrderEvent( beerOrderFound , BeerOrderEventEnum.ALLOCATION_SUCCESS);
        updateOrderQuantities(beerOrderDto,beerOrder);

    }

    private void sendAllocationExceptionMsg(BeerOrder beerOrder, BeerOrderDto beerOrderDto) {
        beerOrder.setOrderStatus(BeerOrderStateEnum.ALLOCATION_EXCEPTION);
        beerOrderRepository.saveAndFlush(beerOrder);
        BeerOrder beerOrderFound = beerOrderRepository.getOne(beerOrder.getId());
        sendOrderEvent( beerOrderFound , BeerOrderEventEnum.ALLOCATION_FAILED);
        updateOrderQuantities(beerOrderDto,beerOrder);
    }


    private void updateOrderQuantities(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
        beerOrderDto.getBeerOrderLines().forEach(beerDto -> {
            beerOrder.getBeerOrderLines().forEach(beer -> {
                if (beerDto.getId().equals(beer.getId())) {
                    beer.setQuantityAllocated(beerDto.getQuantityAllocated());
                }
            });
        });
    }


    private void sendOrderEvent(BeerOrder bo, BeerOrderEventEnum beerOrderEventEnum) {
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
