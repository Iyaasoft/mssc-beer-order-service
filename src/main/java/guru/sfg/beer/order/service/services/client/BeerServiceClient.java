package guru.sfg.beer.order.service.services.client;

import guru.sfg.beer.order.service.web.model.beer.service.dto.BeerDto;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface BeerServiceClient {

    ResponseEntity<BeerDto> beerServiceInvoker(UUID beerId);
}
