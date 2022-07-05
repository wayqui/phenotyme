package es.upm.nlp.corenlpapi.hpo.model.testing.lingpipe.statistic;

import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.HmmChunker;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;

public class Muc6ChunkParserTest {

    public static void main(String[] args) throws Exception {
        File chunkerFile = new File("src/main/resources/hpo/outputmodelfile.model");
        File testFile = new File("src/main/resources/hpo/inputfile.txt");

        HmmChunker chunker
                = (HmmChunker)
                AbstractExternalizable.readObject(chunkerFile);

        ChunkerEvaluator evaluator = new ChunkerEvaluator(chunker);
        evaluator.setVerbose(true);

        Muc6ChunkParser parser
                = new Muc6ChunkParser();
        parser.setHandler(evaluator);

        parser.parse(testFile);

        System.out.println(evaluator.toString());
    }
}
