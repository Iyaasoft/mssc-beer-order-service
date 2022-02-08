package guru.sfg.beer.order.service.web.model.beer.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeerDto implements Serializable {

    public static final long serialVersionUID = -2101579202745568893L;


    private UUID id;

    private String beerName;

    private String beerStyle;

    private String upc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "\"yyyy-MM-dd'T'HH:mm:ssZ\"")
    private OffsetDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "\"yyyy-MM-dd'T'HH:mm:ssZ\"")
    private OffsetDateTime lastModifiedDate;

    private Integer version;

    private BigDecimal price;

    private Integer quantityOnHand;

    private Integer minOnHand;

}
