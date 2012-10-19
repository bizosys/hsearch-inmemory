package com.bizosys.hsearch.memory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class IdSearcher {

	public static void main(String[] args) throws Exception {
		IdSearcher.getInstance().load("D:\\work\\1lineserver-inmemorysearch\\searchids.txt");
		System.out.println( "Found Id Is :" +  IdSearcher.getInstance().find("Karana") );
	}
	
	private static final String Empty = "";
	private static IdSearcher dict = null;
	public static IdSearcher getInstance() {
		if ( null != dict) return dict;
		synchronized (IdSearcher.class) {
			if ( null != dict) return dict;
			dict = new IdSearcher();
		}
		return dict;
	}
	
	Directory idx = null;
	IndexReader reader = null;
	IndexSearcher searcher = null;

	public IdSearcher() {
	}
	
	public void load(String file) throws Exception{
		if ( null != reader ) {
			try {
				reader.close();
				if ( null != searcher ) searcher.close();
			} catch (Exception ex) {
			}
			
		}
		
		this.idx = build(file);
		reader = IndexReader.open(idx);
		searcher = new IndexSearcher(reader);
	}
	
	public String find(String query) throws Exception {
    	
		query = query.toLowerCase();

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		String content = search(idx, query, analyzer);
		if ( content == Empty) {
			content = search(idx, query + "~", analyzer);
		}
		return content;
    }

	private String search(Directory idx, String query, Analyzer analyzer) throws ParseException, CorruptIndexException, IOException {
		Query q = new QueryParser(Version.LUCENE_35, "v", analyzer).parse(query);
    	 
    	 int hitsPerPage = 1;
    	 
    	 TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
    	 searcher.search(q, collector);
    	 ScoreDoc[] hits = collector.topDocs().scoreDocs;
    	 int hitsT = ( null != hits ) ? hits.length : 0;
    	 
    	 String id = Empty;
    	 if ( hitsT > 0 ) {
        	 for(int i=0;i<hits.length;i++) {
        		 int docId = hits[i].doc;
        	     Document d = searcher.doc(docId);
        	     id = d.get("k");
        	 }
    	 }
    	 return id;
	}
	
	private Directory build(String file) throws Exception {
    	RAMDirectory idx = new RAMDirectory();
    	
    	IndexWriter writer = new IndexWriter(idx,  
    		new IndexWriterConfig(Version.LUCENE_35,new StandardAnalyzer(Version.LUCENE_35)));
    	
    	toLines(file, writer);
    	writer.close();
    	return idx;
	}
    
    private Document createDocument(String k, String v) {
        Document doc = new Document();
        doc.add(new Field("k", true, k, Field.Store.YES, Index.ANALYZED,TermVector.NO) );
        doc.add(new Field("v", true, v, Field.Store.NO, Index.ANALYZED,TermVector.NO) );
        return doc;
    }
    
	public final void toLines(String fileName, IndexWriter writer) throws  Exception {
		
		File aFile = new File(fileName);
		BufferedReader reader = null;
		InputStream stream = null;
		try {
			stream = new FileInputStream(aFile); 
			reader = new BufferedReader ( new InputStreamReader (stream) );
			String line = null;
			while((line=reader.readLine())!=null) {
				if (line.length() == 0) continue;
				char first=line.charAt(0);
				switch (first) {
					case ' ' : case '\n' : case '#' :  // skip blank & comment lines
					continue;
				}
				int divideAt = line.indexOf('\t');
				if ( divideAt == -1 ) continue;

				String key = line.substring(0, divideAt).toLowerCase();
				String val = line.substring(divideAt + 1);
				writer.addDocument( createDocument( key, val) );	
			}
		} finally {
			if ( null != reader ) reader.close();
			if ( null != stream) stream.close();
		}
	}    
    
}