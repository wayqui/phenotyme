package es.upm.nlp.corenlpapi.hpo.model.testing.lingpipe.statistic;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.util.Set;

public class Recognition {
    public static void main(String[] args) throws Exception {
        File modelFile = new File("outputmodelfile.model");
        Chunker chunker = (Chunker) AbstractExternalizable
                .readObject(modelFile);
        String testString="my test string to be trained and anotated annotated";
        Chunking chunking = chunker.chunk(testString);
        Set<Chunk> test = chunking.chunkSet();
        for (Chunk c : test) {
            System.out.println(testString + " : "
                    + testString.substring(c.start(), c.end()) + " >> "
                    + c.type());
        }
    }
}