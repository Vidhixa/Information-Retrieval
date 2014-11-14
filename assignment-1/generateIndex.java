package lucenedemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lucenedemo.trecFileRead;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.lucene.benchmark.byTask.feeds.TrecDocParser;
import org.apache.lucene.benchmark.byTask.feeds.TrecGov2Parser;
import org.apache.lucene.benchmark.quality.trec.*;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class generateIndex{
	
	public static void main(String[] args) throws IOException, ParseException, CorruptIndexException, LockObtainFailedException {
		
		//In this class we only use standard analyzer
		//Static paths assigned for now
		trecFileRead t = new trecFileRead();
		File docRead = new File("C:\\Users\\Vidhixa\\Desktop\\Corpus");
		File indexWrite = new File("C:\\Users\\Vidhixa\\Desktop\\indexWrite");
		Directory dir = FSDirectory.open(indexWrite);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer);
		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);	
		IndexWriter writer = new IndexWriter(dir, iwc);
		
		//Calulating the time taken for all files to read and index
		long startTime = System.currentTimeMillis();
		for (File f : docRead.listFiles()){
			
			//Reading each file and converting it into string for parsing
			ArrayList<trecFileRead> list= new ArrayList<trecFileRead>();
		    BufferedReader reader = new BufferedReader(new FileReader (f));
		    String line = null;
		    StringBuilder stringBuilder = new StringBuilder();
		    String ls = System.getProperty("line.separator");

		    while((line = reader.readLine()) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		    }
		    
		    //We parse each document in class trecFileRead
		    //It retuns a list of objects which contains the field we need to index
		    list = t.docReader(stringBuilder.toString());
			Iterator<trecFileRead> i = list.iterator();
			while(i.hasNext()) {
			   trecFileRead node =i.next();
			   Document luceneDoc = new Document();
			 
			   luceneDoc.add(new StringField("DOCNO", node.DOCNO, Field.Store.YES));
			   luceneDoc.add(new StringField("HEAD", node.HEAD, Field.Store.YES));
			   luceneDoc.add(new StringField("DATELINE", node.DATELINE, Field.Store.YES));
			   luceneDoc.add(new StringField("BYLINE", node.BYLINE, Field.Store.YES));
			   luceneDoc.add(new TextField("TEXT", node.TEXT, Field.Store.YES));
			   writer.addDocument(luceneDoc);
			}
		}
		long endTime = System.currentTimeMillis();
		long duration= (endTime-startTime);
		System.out.println("run time is :: "+duration+" millisec");
		writer.close();
		
			//Displaying the results obtained
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("C:\\Users\\Vidhixa\\Desktop\\indexWrite")));
			//Print the total number of documents in the corpus
			System.out.println("Total number of documents in the corpus:"+reader.maxDoc());
			//Print the number of documents containing the term "new" in<field>TEXT</field>.
			System.out.println("Number of documents containing the term \"new\" for	field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
			//Print the total number of occurrences of the term "new" across all documents for <field>TEXT</field>.
			System.out.println("Number of occurences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));
			//Counting the terms in text field for vocabulary
			int count = 0;
			Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
			TermsEnum iterator = vocabulary.iterator(null);
			BytesRef byteRef = null;
			while((byteRef = iterator.next()) != null) {
				count = count+1;
			}			
			//Print the size of the vocabulary for <field>content</field>, only		available per-segment.
			System.out.println("Size of the vocabulary for this field:	"+count);
			//Print the total number of documents that have at least one term for
			//<field>TEXT</field>
			System.out.println("Number of documents that have at least one term for	this field: "+vocabulary.getDocCount());
			//Print the total number of tokens for <field>TEXT</field>
			System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
			//Print the total number of postings for <field>TEXT</field>
			System.out.println("Number of postings for this field:	"+vocabulary.getSumDocFreq());
			//I did not print the vocabulary as the process was resource exhaustive
			/*TermsEnum iterator = vocabulary.iterator(null);
			BytesRef byteRef = null;
			System.out.println("\n*******Vocabulary-Start**********");
			while((byteRef = iterator.next()) != null) {
			String term = byteRef.utf8ToString();
			System.out.print(term+"\t");*/
			}
	}





	
