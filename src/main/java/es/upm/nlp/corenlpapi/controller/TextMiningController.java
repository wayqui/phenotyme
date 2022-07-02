package es.upm.nlp.corenlpapi.controller;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.util.CoreMap;
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
        pipeline = new StanfordCoreNLP("spanish");
    }

    @GetMapping("/ner")
    public List<NERBeanResponse> getNamedEntities(@RequestParam String text) {
        CoreDocument doc = pipeline.processToCoreDocument(text);
        List<NERBeanResponse> results = new ArrayList<>();
        for (CoreEntityMention em : doc.entityMentions()) {
            Timex timex3 = em.coreMap().get(TimeAnnotations.TimexAnnotation.class);
            String normalizedDate = em.coreMap().get(edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

            NERBeanResponse nerBean = NERBeanResponse.builder()
                    .text(em.text())
                    .entityType(em.entityType())
                    .normalizedValue(normalizedDate)
                    .timex3(timex3 != null ? NERBeanTimex3.builder()
                            .tid(timex3.tid())
                            .value(timex3.value())
                            .type(timex3.timexType())
                            .xml(timex3.toString()).build() : null).build();
            results.add(nerBean);
        }
        return results;
    }

    @GetMapping("/sentences")
    public List<String> getSentencesWithSBD(@RequestParam String text) {
        List<String> sentences = new ArrayList<>();
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> coreMapSentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        coreMapSentences.forEach(coreMapSentence -> {
            sentences.add(coreMapSentence.get(CoreAnnotations.TextAnnotation.class));
        });

        return sentences;
    }
}
