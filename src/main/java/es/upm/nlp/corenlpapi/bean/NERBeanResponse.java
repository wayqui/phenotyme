package es.upm.nlp.corenlpapi.bean;

import lombok.Data;

@Data
public class NERBeanResponse {
    private String text;
    private String entityType;
    private String normalizedDate;
}
