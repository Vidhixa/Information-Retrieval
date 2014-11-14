package lucenedemo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;


public class indexComparison {
	

	public static void generateAndCompare(IndexWriter writer, File docRead, String path) throws IOException {		

		long startTime = System.currentTimeMillis();
		
		//Reading and converting the files into string for parsing
		trecFileRead t = new trecFileRead();
		for (File f : docRead.listFiles()){
			ArrayList<trecFileRead> list= new ArrayList<trecFileRead>();
		    BufferedReader reader = new BufferedReader(new FileReader (f));
		    String line = null;
		    StringBuilder stringBuilder = new StringBuilder();
		    String ls = System.getProperty("line.separator");

		    while((line = reader.readLine()) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		    }
		    
		    //Adding the written index generated
		    list = t.docReader(stringBuilder.toString());
			Iterator<trecFileRead> i = list.iterator();
			while(i.hasNext()) {
			   trecFileRead node =i.next();
			   Document luceneDoc = new Document();
			   luceneDoc.add(new TextField("TEXT", node.TEXT, Field.Store.YES));
			   writer.addDocument(luceneDoc);
			}
		}
			writer.close();
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime); 
			System.out.println("the process took :: "+duration+" millisec");
			
			printOutput(path);
		
	}	
	
		public static void printOutput(String path) throws IOException{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(path)));
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

			////I did not print the vocabulary as the process was resource exhaustive
			/*TermsEnum iterator = vocabulary.iterator(null);
			BytesRef byteRef = null;
			System.out.println("\n*******Vocabulary-Start**********");
			while((byteRef = iterator.next()) != null) {
			String term = byteRef.utf8ToString();
			System.out.print(term+"\t");
			}
			System.out.println("\n*******Vocabulary-End**********");*/
			System.out.println("*******************************************************");
			reader.close();
			
		}

public static void main(String[] args) throws IOException, 
ParseException, CorruptIndexException, LockObtainFailedException {
	
	File docRead = new File("C:\\Users\\Vidhixa\\Desktop\\Corpus");
	
	//I preferred to create different directories to keep different index
	File indexWrite1 = new File("C:\\Users\\Vidhixa\\Desktop\\indexWrite1");
	Directory dir1 = FSDirectory.open(indexWrite1);
	
	File indexWrite2 = new File("C:\\Users\\Vidhixa\\Desktop\\indexWrite2");
	Directory dir2 = FSDirectory.open(indexWrite2);
	
	File indexWrite3 = new File("C:\\Users\\Vidhixa\\Desktop\\indexWrite3");
	Directory dir3 = FSDirectory.open(indexWrite3);
	
	File indexWrite4 = new File("C:\\Users\\Vidhixa\\Desktop\\indexWrite4");
	Directory dir4 = FSDirectory.open(indexWrite4);
	
	//Keyword Analyzer
	System.out.println("keyword anayzer:");
	KeywordAnalyzer analyzer1 = new KeywordAnalyzer();
	IndexWriterConfig iwc1 = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer1);
	iwc1.setOpenMode(IndexWriterConfig.OpenMode.CREATE);	
	IndexWriter writer1 = new IndexWriter(dir1, iwc1);
	generateAndCompare(writer1, docRead, indexWrite1.getAbsolutePath());
	
	//Simple Analyzer
	System.out.println("simple anayzer:");
	SimpleAnalyzer analyzer2 = new SimpleAnalyzer();
	IndexWriterConfig iwc2 = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer2);
	iwc2.setOpenMode(IndexWriterConfig.OpenMode.CREATE);	
	IndexWriter writer2 = new IndexWriter(dir2, iwc2);
	generateAndCompare(writer2, docRead, indexWrite2.getAbsolutePath());
	
	//Stop analyzer
	System.out.println("stop anayzer:");
	StopAnalyzer analyzer3 = new StopAnalyzer();
	IndexWriterConfig iwc3 = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer3);
	iwc3.setOpenMode(IndexWriterConfig.OpenMode.CREATE);	
	IndexWriter writer3 = new IndexWriter(dir3, iwc3);
	generateAndCompare(writer3, docRead, indexWrite3.getAbsolutePath());

	//Standard analyzer
	System.out.println("standard anayzer:");
	StandardAnalyzer analyzer4 = new StandardAnalyzer();
	IndexWriterConfig iwc4 = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer4);
	iwc1.setOpenMode(IndexWriterConfig.OpenMode.CREATE);	
	IndexWriter writer4 = new IndexWriter(dir4, iwc4);
	generateAndCompare(writer4, docRead, indexWrite4.getAbsolutePath());
	
}
}	







