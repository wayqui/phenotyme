package es.upm.nlp.corenlpapi.hpo.model.testing.lingpipe.statistic;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.xml.DelegatingHandler;

import java.util.ArrayList;
import java.util.List;


import org.xml.sax.Attributes;

import org.xml.sax.helpers.DefaultHandler;

public class Muc6ChunkParser
        extends XMLParser<ObjectHandler<Chunking>> {

    String mSentenceTag = "s";  // default for MUC6

    /**
     * Construct a MUC6 chunk parser with no handler specified.
     */
    public Muc6ChunkParser() {
        super();
    }

    /**
     * Construct a MUC6 chunk parser with the specified chunk handler.
     *
     * @param handler Chunk handler for the parser.
     */
    public Muc6ChunkParser(ObjectHandler<Chunking> handler) {
        super(handler);
    }

    @Override
    protected DefaultHandler getXMLHandler() {
        return new MucHandler(getHandler());
    }

    /**
     * Sets the value of the sentence tag to be the specified value.
     * Only elements within sentences will be picked up by the parser.
     *
     * @param tag Tag marking sentence elements.
     */
    public void setSentenceTag(String tag) {
        mSentenceTag = tag;
    }

    class MucHandler extends DelegatingHandler {
        ObjectHandler<Chunking> mChunkHandler;
        SentenceHandler mSentHandler;
        MucHandler(ObjectHandler<Chunking> chunkHandler) {
            mChunkHandler = chunkHandler;
            mSentHandler = new SentenceHandler();
            setDelegate(mSentenceTag,mSentHandler);
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler handler) {
            Chunking chunking = mSentHandler.getChunking();
            mChunkHandler.handle(chunking);
        }
    }

    static class SentenceHandler extends DefaultHandler {
        StringBuilder mBuf;
        String mType;
        int mStart;
        int mEnd;
        final List<Chunk> mChunkList = new ArrayList<Chunk>();
        SentenceHandler() {
            /* do nothing */
        }
        @Override
        public void startDocument() {
            mBuf = new StringBuilder();
            mChunkList.clear();
        }
        @Override
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes) {
            if (!"ENAMEX".equals(qName)) return;
            mType = attributes.getValue("TYPE");
            mStart = mBuf.length();
        }
        @Override
        public void endElement(String uri, String localName, String qName) {
            if (!"ENAMEX".equals(qName)) return;
            mEnd = mBuf.length();
            Chunk chunk = ChunkFactory.createChunk(mStart,mEnd,mType,0);
            mChunkList.add(chunk);
        }
        @Override
        public void characters(char[] cs, int start, int length) {
            mBuf.append(cs,start,length);
        }
        public Chunking getChunking() {
            ChunkingImpl chunking = new ChunkingImpl(mBuf);
            for (Chunk chunk : mChunkList)
                chunking.add(chunk);
            return chunking;
        }
    }

}
