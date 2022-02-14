package guru.sfg.beer.order.service.services.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import guru.sfg.beer.order.service.BeerOrderServiceApplication;
import guru.sfg.beer.order.service.base.BreweryWiremockParameterResolver;
import guru.sfg.beer.order.service.base.WiremockInitialiser;
import guru.sfg.beer.order.service.web.model.beer.service.dto.BeerDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(BreweryWiremockParameterResolver.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@SpringBootTest(classes = {BeerOrderServiceApplication.class})
@ContextConfiguration(initializers = {WiremockInitialiser.class})
@TestPropertySource(properties = {"server.port=9090"})
class BeerServiceClientTest {

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    BeerServiceClient beerServiceClient;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void ShouldRetrieveBeerDtoFromBeerService(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception { // ( WireMockRuntimeInfo wmRuntimeInfo) {

        BeerDto dto = BeerDto.builder().id(UUID.fromString("0a818933-087d-47f2-ad83-2f986ed087eb")).beerName("Pilsner").beerStyle("IPA").build();
        String responseJson = mapper.writeValueAsString(dto);
        wireMockServer.stubFor(get(WireMock.urlPathEqualTo("/api/v1/beer/0a818933-087d-47f2-ad83-2f986ed087eb"))
                .withQueryParam("showAllInventoryOnHand", equalTo(("true")))
                .willReturn(aResponse().withBody(responseJson)
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("content-type", "application/json")));

        BeerDto beerDto = beerServiceClient.beerServiceInvoker(UUID.fromString("0a818933-087d-47f2-ad83-2f986ed087eb")).getBody();
        // not started any recording by the way
        System.out.println("Recording status : " + wireMockRuntimeInfo.getWireMock().getStubRecordingStatus().getStatus());
        assertTrue(beerDto.getId().equals(UUID.fromString("0a818933-087d-47f2-ad83-2f986ed087eb")));
        assertTrue(beerDto.getBeerName().equals("Pilsner"));
        assertTrue(beerDto.getBeerStyle().equals("IPA"));
    }

}