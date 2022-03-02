package guru.sfg.beer.order.service.statemachine.manager;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.interceptor.BeerOrderStateChangeInterceptor;
import guru.springframework.domain.BeerOrderEventEnum;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStateEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

   // @Transactional
    @Override
    public BeerOrder newBeerOrder(final BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStateEnum.NEW);
        BeerOrder bo = beerOrderRepository.save(beerOrder);
        sendOrderEvent(bo, BeerOrderEventEnum.VALIDATE_ORDER);
        log.debug("send beer new order  msg order id : "+ bo.getId());
        Optional<BeerOrder> order = beerOrderRepository.findById(bo.getId());
        return order.get();

    }

  //  @Transactional
    @Override
    public void sendBeerOrderValidationResult(final UUID beerOrderId, final boolean valid) {

        Optional<BeerOrder> beerOrder = beerOrderRepository.findById(beerOrderId);

        beerOrder.ifPresentOrElse (order -> {
                    if (valid) {

                        sendOrderEvent(order, BeerOrderEventEnum.VALIDATION_PASSED);

                        log.debug("send beer validation passed -> order id: " + beerOrderId);

                        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderId);

                        sendOrderEvent(beerOrderOptional.get(), BeerOrderEventEnum.ALLOCATE_ORDER);

                        log.debug("send beer validation result -> allocate order id: " + beerOrderId);

                    } else {
                        sendOrderEvent(beerOrder.get(), BeerOrderEventEnum.VALIDATION_FAILED);

                        log.debug("send beer validation result failerd -> order id: " + beerOrderId);
                    }

                }, () -> log.error("Order not found id : "+beerOrderId));
                }

  //  @Transactional
    @Override
    public void sendBeerOrderAllocationResult(final BeerOrderDto beerOrderDto, final boolean allocated,final  boolean allocationError) {

        Optional<BeerOrder> beerOrder = beerOrderRepository.findById(beerOrderDto.getId());
        beerOrder.ifPresentOrElse(order -> {
            if (allocated) {
                sendAllocationSuccessMsg(order, beerOrderDto);
                log.debug("send beer allocation success -> order id: " + beerOrderDto.getId());
            } else if (allocationError) {
                sendAllocationExceptionMsg(order, beerOrderDto);
                log.debug("send beer allocation failed -> order id: " + beerOrderDto.getId());
            } else {
                sendAllocationNoInventoryMsg(order, beerOrderDto);
                log.debug("send beer allocation no inventory -> order id: " + beerOrderDto.getId());
            }
        }, () -> log.error("Error processing order allocation result"));

    }

    @Override
    public void pickUpBeerOrder(UUID orderId) {
        Optional<BeerOrder> beerOrderFound = beerOrderRepository.findById(orderId);
        beerOrderFound.ifPresentOrElse(order -> {
            sendOrderEvent(order,BeerOrderEventEnum.ORDER_FOR_COLLECTION);
        },()->log.debug("Error ORDER_COLLECTED, beer order not fuond "+ orderId));
        log.debug("send beer order picked up  msg order id : "+ orderId);

    }

    @Override
    public void deliverBeerOrder(UUID orderId) {
        Optional<BeerOrder> beerOrderFound = beerOrderRepository.findById(orderId);
        beerOrderFound.ifPresentOrElse(order -> {
            sendOrderEvent(order,BeerOrderEventEnum.ORDER_DELIVERED);
        },()->log.debug("Error ORDER_DELIVERED, beer order not found "+ orderId));
        log.debug("send beer order delivered  msg order id : "+ orderId);

    }


    private void sendAllocationNoInventoryMsg(BeerOrder beerOrder, BeerOrderDto beerOrderDto) {

        beerOrder.setOrderStatus(BeerOrderStateEnum.PENDING_INVENTORY);
        Optional<BeerOrder> beerOrderFound = beerOrderRepository.findById(beerOrder.getId());
        sendOrderEvent( beerOrderFound.get() , BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
        updateOrderQuantities(beerOrderDto,beerOrder);
        beerOrderRepository.saveAndFlush(beerOrder);
        log.debug("send beer allocation no inventory  msg order id : "+ beerOrderDto.getId());
    }

    private void sendAllocationSuccessMsg(BeerOrder beerOrder, BeerOrderDto beerOrderDto) {
        beerOrder.setOrderStatus(BeerOrderStateEnum.ALLOCATED);
        Optional<BeerOrder> beerOrderFound = beerOrderRepository.findById(beerOrder.getId());
        sendOrderEvent( beerOrderFound.get() , BeerOrderEventEnum.ALLOCATION_SUCCESS);
        updateOrderQuantities(beerOrderDto,beerOrder);
        beerOrderRepository.saveAndFlush(beerOrder);
        log.debug("send beer allocation  success msg order id : "+ beerOrderDto.getId());

    }

    private void sendAllocationExceptionMsg(BeerOrder beerOrder, BeerOrderDto beerOrderDto) {
        beerOrder.setOrderStatus(BeerOrderStateEnum.ALLOCATION_EXCEPTION);
        beerOrderRepository.saveAndFlush(beerOrder);
        Optional<BeerOrder> beerOrderFound = beerOrderRepository.findById(beerOrder.getId());
        sendOrderEvent( beerOrderFound.get() , BeerOrderEventEnum.ALLOCATION_FAILED);
        log.debug("send beer allocation  failedc msg order id : "+ beerOrderDto.getId());
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
