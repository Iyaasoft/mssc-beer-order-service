package guru.sfg.beer.order.service.services.client;

import guru.sfg.beer.order.service.BeerOrderServiceApplication;
import guru.sfg.beer.order.service.base.BreweryWiremockParameterResolver;
import guru.sfg.beer.order.service.base.WiremockInitialiser;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(BreweryWiremockParameterResolver.class)
@SpringBootTest(classes = {BeerOrderServiceApplication.class})
@ContextConfiguration(initializers = {WiremockInitialiser.class})
class BeerServiceClientTest {



}