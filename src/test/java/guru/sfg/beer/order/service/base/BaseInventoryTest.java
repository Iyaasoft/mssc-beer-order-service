package guru.sfg.beer.order.service.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.springframework.domain.BeerOrderStateEnum;
import guru.springframework.web.model.BeerDto;
import guru.springframework.web.model.BeerOrderDto;
import guru.springframework.web.model.BeerOrderLineDto;
import guru.springframework.web.model.BeerOrderPagedList;
import guru.springframework.web.model.BeerStyleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseInventoryTest {

    protected static final String BEER_ID = "b6104b4e-8e3b-4270-aa89-6690782891ac";
    protected static final String BASE_URI = "/api/v1/customers/{customerId}";
    protected static final String CUSTOMER_ID = "b8e94041-f401-4147-a41c-e043385f1c01";

    protected static final String ORDER_ID = "f404a711-c998-4b85-99b9-f1cdf52cda37";
    protected static final String BEER_NAME = "Heinikin";
    protected static final String BEER_STYLE = "LARGER";
    protected static final String UPC = "123456342";
    @Autowired
    protected ObjectMapper mapper;

    protected BeerOrderLineDto getOrderLine() {
        return BeerOrderLineDto.builder()
                .id(UUID.fromString(ORDER_ID))
                .beerId(UUID.fromString(BEER_ID))
                .beerName("Heinikin")
                .beerStyle("LARGER")
                .createdDate(null)
                .lastModifiedDate(null)
                .orderQuantity(6)
                .price(new BigDecimal(3.50))
                .upc("123456342").build();

    }

    protected BeerOrderLine getOrderDetail() {
        return BeerOrderLine.builder()
                .id(UUID.fromString(ORDER_ID))
                .beerId(UUID.fromString(BEER_ID))
                .createdDate(null)
                .lastModifiedDate(null)
                .orderQuantity(6).orderQuantity(6).quantityAllocated(0).build();
    }

    protected String getOrderLineJson() throws Exception {
        return mapper.writeValueAsString(BeerOrderLineDto.builder()
                .beerId(UUID.fromString(BEER_ID))
                .beerName(BEER_NAME)
                .beerStyle(BEER_STYLE)
                .createdDate(null)
                .lastModifiedDate(null)
                .orderQuantity(6)
                .price(new BigDecimal(3.50))
                .upc(UPC).build());

    }

    protected ResponseEntity<BeerDto> getBeerDto() {
        return new ResponseEntity<BeerDto>(BeerDto.builder()
                .id(UUID.fromString(BEER_ID))
                .beerName("Heinikin")
                .beerStyle(BeerStyleEnum.LARGER)
                .createdDate(null)
                .lastModifiedDate(null)
                .price(new BigDecimal(3.50))
                .upc("123456342").build(), HttpStatus.OK);
    }

    protected BeerOrderPagedList getBearPageList() {
        List<BeerOrderLineDto> orderLineDtos = new ArrayList<>();
        orderLineDtos.add(getOrderLine());
        BeerOrderDto dto = BeerOrderDto.builder()
                            .id(UUID.fromString(BEER_ID))
                            .beerOrderLines(orderLineDtos)
                            .orderStatus(BeerOrderStateEnum.NEW)
                            .customerId(UUID.fromString(CUSTOMER_ID))
                .createdDate(null)
                .lastModifiedDate(null)
                            .build();
        List<BeerOrderDto> dtos = new ArrayList<>();
        dtos.add(dto);
        return new BeerOrderPagedList(dtos);
    }

    protected String getBeerOrderDtoJson() throws Exception {
        List<BeerOrderLineDto> orderLineDtos = new ArrayList<>();
        orderLineDtos.add(getOrderLine());
        BeerOrderDto dto = BeerOrderDto.builder()
                    .id(UUID.fromString(ORDER_ID))
                    .beerOrderLines(orderLineDtos)
                    .orderStatus(BeerOrderStateEnum.NEW)
                    .customerId(UUID.fromString(CUSTOMER_ID))
                    .createdDate(null)
                    .lastModifiedDate(null)
                    .build();
       return  mapper.writeValueAsString(dto);
    }
}
