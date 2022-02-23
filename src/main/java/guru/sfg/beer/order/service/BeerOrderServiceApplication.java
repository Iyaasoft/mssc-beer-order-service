package guru.sfg.beer.order.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.json.Json;

@SpringBootApplication
public class BeerOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeerOrderServiceApplication.class, args);

    }

    @Bean
    @JsonFormat(without = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_VALUES)
    public ObjectMapper mapper(){
       return new ObjectMapper();
    }
}
