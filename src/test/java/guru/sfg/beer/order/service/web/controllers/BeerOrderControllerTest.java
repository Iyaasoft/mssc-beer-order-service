package guru.sfg.beer.order.service.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.beer.order.service.base.BaseInventoryTest;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.BeerOrderService;
import guru.sfg.beer.order.service.services.client.BeerServiceClient;
import guru.sfg.beer.order.service.web.mappers.BeerOrderLineMapper;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.springframework.web.model.BeerOrderDto;
import guru.springframework.web.model.BeerOrderPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import wiremock.org.checkerframework.checker.units.qual.C;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BeerOrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {BeerOrderRepository.class, CustomerRepository.class}))
class BeerOrderControllerTest extends BaseInventoryTest {


    @Autowired
    MockMvc mvc;

    @Autowired
    BeerOrderController beerOrderController;

    @MockBean
    BeerServiceClient beerServiceClient;

    @MockBean
    BeerOrderRepository beerOrderRepository;

    @MockBean
    BeerOrderService beerOrderService;

    @BeforeEach
    public void setup () {
        when(beerServiceClient.beerServiceInvoker(any(UUID.class))).thenReturn(getBeerDto());
        when(beerOrderService.listOrders(any(UUID.class), any(PageRequest.class))).thenReturn(getBearPageList());



    }

    @Test
    void listOrdersTest() throws Exception {
        BeerOrderPagedList pageList = getBearPageList();
        String expected = mapper.writeValueAsString(pageList);
        when(beerOrderService.placeOrder(any(UUID.class), any(BeerOrderDto.class))).thenReturn(pageList.getContent().get(0));
        mvc.perform(get(BASE_URI + "/orders/", CUSTOMER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNumber", "2")
                        .param("pageSize", "25"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }



    @Test
    void placeOrderTest() throws Exception {
        BeerOrderPagedList pageList = getBearPageList();
        String expected = mapper.writeValueAsString(pageList);
        when(beerOrderService.placeOrder(any(UUID.class), any(BeerOrderDto.class))).thenReturn(pageList.getContent().get(0));

        mvc.perform(post(BASE_URI + "/orders/", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getBeerOrderDtoJson()))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void getOrderTest() throws Exception {

        mvc.perform(get(BASE_URI + "/orders/", CUSTOMER_ID, ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].customerId").value(CUSTOMER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].beerOrderLines[0].beerId").value(BEER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].beerOrderLines[0].beerStyle").value(BEER_STYLE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].beerOrderLines[0].id").value(ORDER_ID));
    }

    @Test
    void pickupOrder() throws Exception {

        mvc.perform(put(BASE_URI + " /orders/{orderId}/pickup", CUSTOMER_ID, ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

    }
}