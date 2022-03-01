package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.client.BeerServiceClientImpl;
import guru.springframework.web.model.BeerDto;
import guru.springframework.web.model.BeerOrderLineDto;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public abstract class BeerOrderLineDecorator implements  BeerOrderLineMapper {

    private  BeerOrderLineMapper beerOrderLineMapper;

    private  BeerServiceClientImpl beerServiceClient;

    @Autowired
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    @Autowired
    public void setBeerServiceClient(BeerServiceClientImpl beerServiceClient) {
        this.beerServiceClient = beerServiceClient;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto lineDto = beerOrderLineMapper.beerOrderLineToDto(line);
        // faking call the service with a known id
       // lineDto.setBeerId(UUID.fromString("5456319d-7b6f-41e0-a6e3-2365f3a6d196"));
        ResponseEntity<BeerDto> beerDto = beerServiceClient.beerServiceInvoker(lineDto.getBeerId());
        lineDto.setBeerId(line.getBeerId());
        lineDto.setBeerName(beerDto.getBody().getBeerName());
        lineDto.setBeerName(beerDto.getBody().getBeerStyle().toString());
        lineDto.setUpc(beerDto.getBody().getUpc());
        lineDto.setPrice(beerDto.getBody().getPrice());
        return lineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return beerOrderLineMapper.dtoToBeerOrderLine(dto);
    }
}
