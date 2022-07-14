package es.upm.nlp.corenlpapi.controller;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.ling.tokensregex.types.Expressions;
import edu.stanford.nlp.ling.tokensregex.types.Value;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.util.CoreMap;
import es.upm.nlp.corenlpapi.Timex3Util;
import es.upm.nlp.corenlpapi.bean.NERBeanTimex3;
import es.upm.nlp.corenlpapi.bean.NEREntityResponse;
import es.upm.nlp.corenlpapi.bean.NERResponse;
import oeg.tagger.core.time.annotation.temporal;
import oeg.tagger.core.time.annotation.timex;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class TextMiningController {

    private static final Logger LOGGER = Logger.getLogger(TextMiningController.class.getName());

    private StanfordCoreNLP pipeline;

    public TextMiningController() {
        pipeline = new StanfordCoreNLP("StanfordCoreNLP-spanish.properties");
    }

    @GetMapping("/ner")
    public List<NEREntityResponse> getNamedEntities(@RequestParam String text, @RequestParam(required = false) String anchorDate) {
        Annotation annotation = new Annotation(text);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, anchorDate != null ? anchorDate : LocalDate.now().toString());
        pipeline.annotate(annotation);
        List<NEREntityResponse> results = new ArrayList<>();

        List<CoreMap> mentions = annotation.get(CoreAnnotations.MentionsAnnotation.class);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        // Process entity mentions
        if (mentions == null) return results;
        for (CoreMap em : mentions) {
            Timex timex3 = em.get(TimeAnnotations.TimexAnnotation.class);
            String normalizedValue = em.get(edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

            NEREntityResponse nerBean = NEREntityResponse.builder()
                    .text(em.get(CoreAnnotations.TextAnnotation.class))
                    .entityType(em.get(CoreAnnotations.EntityTypeAnnotation.class))
                    .normalizedValue(normalizedValue)
                    .timex3(timex3 != null ? NERBeanTimex3.builder()
                            .tid(timex3.tid())
                            .value(timex3.value())
                            .type(timex3.timexType())
                            .xml(timex3.toString()).build() : null).build();
            results.add(nerBean);
        }

        // Processed temporal expressions
        if (sentences == null) return results;
        sentences.forEach(coreMap -> {
            CoreMapExpressionExtractor<MatchedExpression> extractor = CoreMapExpressionExtractor
                    .createExtractorFromFiles(TokenSequencePattern.getNewEnv(), "./src/main/resources/hourglass/rulesES-standard.txt");
            List<MatchedExpression> matchedExpressions = extractor.extractExpressions(coreMap);
            for (int i = 0; i < matchedExpressions.size(); i++) {
                MatchedExpression matched = matchedExpressions.get(i);
                CoreMap cm = matched.getAnnotation();

                String timex3 = Timex3Util.obtainTimex3Expression(matched, i, anchorDate);

                NEREntityResponse nerBean = NEREntityResponse.builder()
                        .text(cm.get(CoreAnnotations.TextAnnotation.class))
                        .entityType(cm.get(timex.Type.class))
                        .normalizedValue(cm.get(timex.Value.class))
                        .timex3(NERBeanTimex3.builder()
                                .xml(timex3).build()).build();
                results.add(nerBean);
            }
        });

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

    @PostMapping("/ner/phenotypic")
    public NERResponse getNamedPhenotypesFromText(@RequestBody List<String> plainSentences, @RequestParam(required = false) String anchorDate) {
        NERResponse response = NERResponse.builder()
                .entities(new ArrayList<>())
                .durationsCount(0)
                .timesCount(0)
                .datesCount(0)
                .entitiesCount(0)
                .phenotypesCount(0)
                .setsCount(0).build();
        if (plainSentences != null && !plainSentences.isEmpty()) {
            for (String plainSentence : plainSentences) {
                Annotation annotation = new Annotation(plainSentence);
                pipeline.annotate(annotation);
                List<CoreMap> mentions = annotation.get(CoreAnnotations.MentionsAnnotation.class);
                if (mentions == null) return response;
                for (CoreMap em : mentions) {
                    Timex timex3 = em.get(TimeAnnotations.TimexAnnotation.class);
                    String normalizedValue = em.get(edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

                    if (em.get(CoreAnnotations.EntityTypeAnnotation.class).equals("PRENATAL_DEVELOPMENT_OR_BIRTH_ABNORMALITY")) {
                        NEREntityResponse nerBean = NEREntityResponse.builder()
                                .text(em.get(CoreAnnotations.TextAnnotation.class))
                                .entityType(em.get(CoreAnnotations.EntityTypeAnnotation.class))
                                .normalizedValue(normalizedValue)
                                .timex3(timex3 != null ? NERBeanTimex3.builder()
                                        .tid(timex3.tid())
                                        .value(timex3.value())
                                        .type(timex3.timexType())
                                        .xml(timex3.toString()).build() : null).build();
                        response.getEntities().add(nerBean);
                    }
                }
            }
        }
        response.setEntitiesCount(response.getEntities().size());
        return response;
    }

        @PostMapping("/ner/tempex")
    public NERResponse getNamedTempExFromText(@RequestBody List<String> plainSentences, @RequestParam(required = false) String anchorDate) {
        NERResponse response = NERResponse.builder()
                .entities(new ArrayList<>())
                .durationsCount(0)
                .timesCount(0)
                .datesCount(0)
                .entitiesCount(0)
                .phenotypesCount(0)
                .setsCount(0).build();

        if (plainSentences != null && !plainSentences.isEmpty()) {
            for (String plainSentence: plainSentences) {
                Annotation annotation = new Annotation(plainSentence);
                annotation.set(CoreAnnotations.DocDateAnnotation.class, anchorDate != null ? anchorDate : LocalDate.now().toString());
                pipeline.annotate(annotation);
                List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

                if (sentences == null) return response;
                sentences.forEach(coreMap -> {
                    CoreMapExpressionExtractor<MatchedExpression> extractor = CoreMapExpressionExtractor
                            .createExtractorFromFiles(TokenSequencePattern.getNewEnv(), "./src/main/resources/hourglass/rulesES-standard.txt");
                    List<MatchedExpression> matchedExpressions = extractor.extractExpressions(coreMap);
                    for (MatchedExpression matched : matchedExpressions) {
                        CoreMap cm = matched.getAnnotation();

                        Value v = matched.getValue();
                        ArrayList<Expressions.PrimitiveValue> a = (ArrayList<Expressions.PrimitiveValue>) v.get();
                        String type = (String) a.get(0).get();
                        String value = (String) a.get(1).get();
                        String frequency = (String) a.get(2).get();
                        String rule = (String) a.get(4).get();

                        NEREntityResponse nerBean = NEREntityResponse.builder()
                                .text(cm.get(CoreAnnotations.TextAnnotation.class))
                                .entityType(cm.get(timex.Type.class))
                                .normalizedValue(cm.get(timex.Value.class))
                                .timex3(NERBeanTimex3.builder()
                                        .xml(cm.get(temporal.MyRuleAnnotation.class)).build()).build();
                        response.getEntities().add(nerBean);

                        switch (type) {
                            case "DATE": response.setDatesCount(response.getDatesCount()+1);
                                break;
                            case "TIME": response.setTimesCount(response.getTimesCount()+1);
                                break;
                            case "DURATION": response.setDurationsCount(response.getDurationsCount()+1);
                                break;
                            case "SET": response.setSetsCount(response.getSetsCount()+1);
                                break;
                        }
                    }
                });
            }
        }
        response.setEntitiesCount(response.getEntities().size());
        return response;
    }
}
