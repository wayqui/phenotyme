package es.upm.nlp.corenlpapi.hpo.model.testing.corenlp;

import edu.stanford.nlp.ie.crf.CRFClassifier;

import java.util.Arrays;

public class HPOEsModelTest {

    private static String modelPath = "src/main/resources/hpo/corenlp/hpo-ner-model";

    public static void main(String[] args) {
        HPOEsModelTest train = new HPOEsModelTest();

        // Testing the model
        Arrays.asList(new String[]{"Servicio de Obstetricia y Ginecología", "Requiere hospitalización y Cesárea ya que no será un Parto anormal"})
                .forEach(item -> train.doTagging(train.getModel(modelPath), item));
    }

    public CRFClassifier getModel(String modelPath) {
        return CRFClassifier.getClassifierNoExceptions(modelPath);
    }

    public void doTagging(CRFClassifier model, String input) {
        input = input.trim();
        System.out.println(input);
        System.out.println(model.classifyToString(input));
    }

}
