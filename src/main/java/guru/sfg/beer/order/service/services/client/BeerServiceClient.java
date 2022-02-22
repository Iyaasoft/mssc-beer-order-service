package guru.sfg.beer.order.service.services.client;

import guru.springframework.web.model.BeerDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface BeerServiceClient {

    ResponseEntity<BeerDto> beerServiceInvoker(UUID beerId);
}
