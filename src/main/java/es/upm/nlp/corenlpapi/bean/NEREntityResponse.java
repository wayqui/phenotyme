package es.upm.nlp.corenlpapi.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NEREntityResponse {
    private String text;
    private String entityType;
    private String normalizedValue;
    private NERBeanTimex3 timex3;
}
