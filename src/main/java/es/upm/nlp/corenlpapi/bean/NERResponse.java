package es.upm.nlp.corenlpapi.bean;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NERResponse {

    private Integer entitiesCount;
    private Integer phenotypesCount;
    private Integer timesCount;
    private Integer datesCount;
    private Integer setsCount;
    private Integer durationsCount;

    private List<NEREntityResponse> entities;

}
