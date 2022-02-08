package guru.sfg.beer.order.service.services.client;

import guru.sfg.beer.order.service.config.OrderConfig;
import guru.sfg.beer.order.service.web.model.beer.service.dto.BeerDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class BeerServiceClientImpl extends OrderConfig implements BeerServiceClient {

    private final RestTemplate restTemplate;


    public BeerServiceClientImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ResponseEntity<BeerDto> beerServiceInvoker(UUID beerId) {

        BeerDto dto = restTemplate.exchange(
                this.beerServiceHost + "/api/v1/beer/{beerId}?showAllInventoryOnHand=true",
                HttpMethod.GET,
                null,
                BeerDto.class,
                beerId)
                .getBody();

        return new ResponseEntity(dto, HttpStatus.OK);
    }
}
