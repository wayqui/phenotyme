package es.upm.nlp.corenlpapi.hpo.lingpipe.statistic;

import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import oracle.jrockit.jfr.parser.ChunkParser;

import java.io.File;
import java.io.IOException;

public class LingPiperPipeline {

    static final int MAX_N_GRAM = 50;
    static final int NUM_CHARS = 300;
    static final double LM_INTERPOLATION = MAX_N_GRAM; // default behavior

    public static void main(String[] args) throws IOException {
        File corpusFile = new File("src/main/resources/hpo/inputfile.txt");// my annotated file
        File modelFile = new File("src/main/resources/hpo/outputmodelfile.model");

        System.out.println("Setting up Chunker Estimator");
        TokenizerFactory factory
                = IndoEuropeanTokenizerFactory.INSTANCE;
        HmmCharLmEstimator hmmEstimator
                = new HmmCharLmEstimator(MAX_N_GRAM,NUM_CHARS,LM_INTERPOLATION);
        CharLmHmmChunker chunkerEstimator
                = new CharLmHmmChunker(factory,hmmEstimator);

        System.out.println("Setting up Data Parser");
        Muc6ChunkParser parser = new Muc6ChunkParser();
        parser.setHandler(chunkerEstimator);

        System.out.println("Training with Data from File=" + corpusFile);
        parser.parse(corpusFile);

        System.out.println("Compiling and Writing Model to File=" + modelFile);
        AbstractExternalizable.compileTo(chunkerEstimator,modelFile);
    }

}
