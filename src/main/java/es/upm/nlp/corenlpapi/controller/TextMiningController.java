package es.upm.nlp.corenlpapi.controller;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import es.upm.nlp.corenlpapi.bean.NERBeanResponse;
import es.upm.nlp.corenlpapi.bean.NERBeanTimex3;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TextMiningController {

    private StanfordCoreNLP pipeline;

    public TextMiningController() {
        //pipeline = new StanfordCoreNLP("StanfordCoreNLP-spanish.properties");
        pipeline = new StanfordCoreNLP("spanish");
    }

    @GetMapping("/ner")
    public List<NERBeanResponse> getNamedEntities(@RequestParam String text) {
        CoreDocument doc = pipeline.processToCoreDocument(text);
        List<NERBeanResponse> results = new ArrayList<>();
        for (CoreEntityMention em : doc.entityMentions()) {

            Timex timex3 = em.coreMap().get(TimeAnnotations.TimexAnnotation.class);
            String normalizedDate = em.coreMap().get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

            NERBeanResponse nerBean = NERBeanResponse.builder()
                    .text(em.text())
                    .entityType(em.entityType())
                    .normalizedDate(normalizedDate)
                    .timex3(NERBeanTimex3.builder()
                            .tid(timex3 != null ? timex3.tid() : null)
                            .value(timex3 != null ? timex3.value() : null)
                            .type(timex3 != null ? timex3.timexType() : null)
                            .xml(timex3 != null ? timex3.toString() : null).build()).build();
            results.add(nerBean);
        }
        return results;
    }
}
