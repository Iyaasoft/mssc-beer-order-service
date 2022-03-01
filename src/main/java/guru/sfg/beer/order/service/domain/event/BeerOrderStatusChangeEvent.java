package guru.sfg.beer.order.service.domain.event;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.springframework.domain.BeerOrderStateEnum;
import org.springframework.context.ApplicationEvent;


public class BeerOrderStatusChangeEvent extends ApplicationEvent {

    private final BeerOrderStateEnum previousStatus;

    public BeerOrderStatusChangeEvent(BeerOrder source, BeerOrderStateEnum previousStatus) {
        super(source);
        this.previousStatus = previousStatus;
    }

    public BeerOrderStateEnum getPreviousStatus() {
        return previousStatus;
    }

    public BeerOrder getBeerOrder() {
        return (BeerOrder) super.getSource();
    }
}
