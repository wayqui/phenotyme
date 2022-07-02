package es.upm.nlp.corenlpapi.hpo.lingpipe.dictionary;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DictionaryChunker {
    static final double CHUNK_SCORE = 1.0;

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("src/main/resources/hpo/corenlp/HPO-spanish3.tsv");

        List<String[]> dictionary = Files.lines(path)
                .map(line -> line.split("\t"))
                .collect(Collectors.toList());

        MapDictionary<String> mapDictionary = new MapDictionary<>();
        dictionary.forEach(line -> {
            mapDictionary.addEntry(new DictionaryEntry<>(line[0], line[1], CHUNK_SCORE));
        });

        ExactDictionaryChunker dictionaryChunkerTT
                = new ExactDictionaryChunker(mapDictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                true,true);

        ExactDictionaryChunker dictionaryChunkerTF
                = new ExactDictionaryChunker(mapDictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                true,false);

        ExactDictionaryChunker dictionaryChunkerFT
                = new ExactDictionaryChunker(mapDictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                false,true);

        ExactDictionaryChunker dictionaryChunkerFF
                = new ExactDictionaryChunker(mapDictionary,
                IndoEuropeanTokenizerFactory.INSTANCE,
                false,false);

        System.out.println("\nDICTIONARY\n" + mapDictionary);
        args = new String[]{"Servicio de Obstetricia y Ginecología\", \"Requiere hospitalización y cesarea ya que no será un parto anormal"};
        for (int i = 0; i < args.length; ++i) {
            String text = args[i];
            System.out.println("\n\nTEXT=" + text);

            chunk(dictionaryChunkerTT,text);
            chunk(dictionaryChunkerTF,text);
            chunk(dictionaryChunkerFT,text);
            chunk(dictionaryChunkerFF,text);
        }

    }

    static void chunk(ExactDictionaryChunker chunker, String text) {
        System.out.println("\nChunker."
                + " All matches=" + chunker.returnAllMatches()
                + " Case sensitive=" + chunker.caseSensitive());
        Chunking chunking = chunker.chunk(text);
        for (Chunk chunk : chunking.chunkSet()) {
            int start = chunk.start();
            int end = chunk.end();
            String type = chunk.type();
            double score = chunk.score();
            String phrase = text.substring(start,end);
            System.out.println("     phrase=|" + phrase + "|"
                    + " start=" + start
                    + " end=" + end
                    + " type=" + type
                    + " score=" + score);
        }
    }

}
