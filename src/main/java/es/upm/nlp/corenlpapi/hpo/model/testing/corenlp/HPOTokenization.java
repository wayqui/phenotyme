package es.upm.nlp.corenlpapi.hpo.model.testing.corenlp;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CollectionUtils;
import edu.stanford.nlp.util.StringUtils;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class HPOTokenization {
    public static void main(String[] args) {
        Locale currentLocale = new Locale("es", "ES");

        BreakIterator iterator = BreakIterator.getSentenceInstance(currentLocale);
        iterator.setText("Para deter");
    }

    /**
     * n grams for already split string. the ngrams are joined with a single space
     */
    public static Collection<String> getNgramsFromTokens(List<CoreLabel> words, int minSize, int maxSize){
        List<String> wordsStr = new ArrayList<>();
        for(CoreLabel l : words)
            wordsStr.add(l.word());
        List<List<String>> ng = CollectionUtils.getNGrams(wordsStr, minSize, maxSize);
        Collection<String> ngrams = new ArrayList<>();
        for(List<String> n: ng)
            ngrams.add(StringUtils.join(n," "));
        return ngrams;
    }
}
