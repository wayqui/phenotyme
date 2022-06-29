package es.upm.nlp.corenlpapi.controller;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations;
import es.upm.nlp.corenlpapi.bean.NERBeanResponse;
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
            //sentence().entityMentions.get(0).entityMentionCoreMap.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class)
            System.out.println("temporal expression: "+em.text());
            System.out.println("temporal value: " +
                    em.coreMap().get(TimeAnnotations.TimexAnnotation.class));


            NERBeanResponse nerBean = new NERBeanResponse();
            nerBean.setText(em.text());
            nerBean.setEntityType(em.entityType());
            nerBean.setNormalizedDate(em.coreMap().get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
            results.add(nerBean);
        }

        return results;
    }
}
