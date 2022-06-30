package es.upm.nlp.corenlpapi.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NERBeanTimex3 {
    private String tid;
    private String type;
    private String value;
    private String xml;
}
