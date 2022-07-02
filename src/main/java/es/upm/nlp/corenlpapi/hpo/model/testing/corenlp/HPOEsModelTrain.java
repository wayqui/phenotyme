package es.upm.nlp.corenlpapi.hpo.model.testing.corenlp;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;

import java.util.Properties;

public class HPOEsModelTrain {

    private static String modelPath = "src/main/resources/hpo/corenlp/hpo-ner-model";
    private static String propFile = "src/main/resources/hpo/corenlp/custom_model.properties";

    public static void main(String[] args) {
        HPOEsModelTrain train = new HPOEsModelTrain();
        // Training the model
        train.trainAndWrite(modelPath, propFile, null);
    }

    public void trainAndWrite(String modelOutPath, String prop, String trainingFilepath) {
        Properties props = StringUtils.propFileToProperties(prop);
        props.setProperty("serializeTo", modelOutPath);
        //if input use that, else use from properties file.
        if (trainingFilepath != null) {
            props.setProperty("trainFile", trainingFilepath);
        }

        SeqClassifierFlags flags = new SeqClassifierFlags(props);
        CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
        crf.train();
        crf.serializeClassifier(modelOutPath);
    }

}
