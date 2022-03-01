package guru.sfg.beer.order.service.statemachine.listener;


import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.event.BeerOrderStatusChangeEvent;
import guru.sfg.beer.order.service.web.mappers.DateMapper;
import guru.springframework.web.model.OrderStatusUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class BeerOrderStatusChangeListener {
    private RestTemplate restTemplate;
    private final DateMapper dateMapper;

    public BeerOrderStatusChangeListener(DateMapper dateMapper, RestTemplateBuilder builder) {
        this.dateMapper = dateMapper;
        this.restTemplate = builder.build();
    }

    @Async
    @EventListener
    public void orderStatusChangeListener(BeerOrderStatusChangeEvent event) {
        log.debug("order State change called");

        BeerOrder  order = event.getBeerOrder();
        OrderStatusUpdate update  = OrderStatusUpdate.builder()
                .orderId(order.getId())
                .version(order.getVersion() != null ? order.getVersion().intValue() : null)
                .createdDate(dateMapper.asOffsetDateTime(order.getCreatedDate()))
                .lastModifiedDate(dateMapper.asOffsetDateTime(order.getLastModifiedDate()))
                .customerRef(order.getCustomerRef())
                .orderStatus(order.getOrderStatus().toString())
                .build();

        try {
            log.debug("order State change " + event.getPreviousStatus() +" "+ update.getOrderStatus());
            restTemplate.postForObject(order.getOrderStatusCallbackUrl(), update, String.class);
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex.getCause());
        }
    }


}
