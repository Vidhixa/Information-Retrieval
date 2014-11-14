package rankingIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {
	
	public static  void  main(String args[]) throws IOException, ParseException{
		String pathToIndex = "C:\\Users\\Vidhixa\\Desktop\\assignment-2\\Default";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));	
		
		File docRead = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\topics.51-100");
		BufferedReader r = new BufferedReader(new FileReader (docRead));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		
		while((line = r.readLine()) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		}
		
		String doc = stringBuilder.toString();
		
		ArrayList<String> queryShortList = readTrec(doc, "title");
		ArrayList<String> queryLongList = readTrec(doc, "desc");
		
		//BM25
		System.out.println("BM25 file created");
		File writeBMLong = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\BM25longQuery.txt");
		File writeBMShort = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\BM25shortQuery.txt");
		BufferedWriter bwBMLong = new BufferedWriter(new FileWriter(writeBMLong));
		BufferedWriter bwBMShort = new BufferedWriter(new FileWriter(writeBMShort));
		
		IndexSearcher bmsearcher = new IndexSearcher(reader);
		bmsearcher.setSimilarity(new BM25Similarity()); 	
		
		//Vector Space Model
		System.out.println("Vector Space Model file created");
		File writeVecLong = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\VectorSpacelongQuery.txt");
		File writeVecShort = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\VectorSpaceshortQuery.txt");
		BufferedWriter bwVecLong = new BufferedWriter(new FileWriter(writeVecLong));
		BufferedWriter bwVecShort= new BufferedWriter(new FileWriter(writeVecShort));
		IndexSearcher vecsearcher = new IndexSearcher(reader);
		vecsearcher.setSimilarity(new DefaultSimilarity()); 
		
		//Language Model with Dirichlet Smoothing
		System.out.println("Language Model with Dirichlet Smoothing file created");
		File writeDirLong = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\DirichletlongQuery.txt");
		File writeDirShort = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\DirichletshortQuery.txt");	
		BufferedWriter bwDirLong = new BufferedWriter(new FileWriter(writeDirLong));
		BufferedWriter bwDirShort = new BufferedWriter(new FileWriter(writeDirShort));
		IndexSearcher dirsearcher = new IndexSearcher(reader);
		dirsearcher.setSimilarity(new LMDirichletSimilarity()); 
				
		//Language Model with Jelinek Mercer Smoothing	
		System.out.println("Language Model with Jelinek Mercer Smoothing file created");
		File writeJMLong = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\JMSmoothinglongQuery.txt");
		File writeJMShort = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\JMSmoothingshortQuery.txt");
		BufferedWriter bwJMLong = new BufferedWriter(new FileWriter(writeJMLong));
		BufferedWriter bwJMShort = new BufferedWriter(new FileWriter(writeJMShort));
		IndexSearcher jmsearcher = new IndexSearcher(reader);
		jmsearcher.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7)); 
		
		//Ranking short queries according to different algorithms
		for(int x=0 ; x<queryShortList.size() ; x++){
			String shrt = queryShortList.get(x);
			diffRanks(bmsearcher, shrt, bwBMShort, x+51, "BM25_short");
			diffRanks(vecsearcher, shrt, bwVecShort, x+51, "VEC_short");
			diffRanks(dirsearcher, shrt, bwDirShort, x+51, "DIR_short");
			diffRanks(jmsearcher, shrt, bwJMShort, x+51, "JM_short");	
		}
		
		//Ranking long queries according to different algorithms
		for(int x=0 ; x<queryLongList.size() ; x++){
			String lng = queryLongList.get(x);
			diffRanks(bmsearcher, lng, bwBMLong, x+51, "BM25_long");
			diffRanks(vecsearcher, lng, bwVecLong, x+51, "VEC_long");
			diffRanks(dirsearcher, lng, bwDirLong, x+51, "DIR_long");
			diffRanks(jmsearcher, lng, bwJMLong, x+51, "JM_long");
		}
		
		bwBMLong.close();
		bwBMShort.close();
		bwDirLong.close();
		bwDirShort.close();
		bwJMShort.close(); 
		bwJMLong.close();
		bwVecLong.close();
		bwVecShort.close();
		reader.close();
	}
	
	public static void diffRanks(IndexSearcher searcher, String querystring, BufferedWriter bw, int x, String id) throws ParseException, IOException{
		
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(querystring);
		TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
		searcher.search(query, collector);
		ScoreDoc[] docs = collector.topDocs().scoreDocs;
		
		int rank = 0;		
		for (int i = 0; i < docs.length; i++) {
			rank = rank + 1;
			Document doc = searcher.doc(docs[i].doc);
			bw.write(x+" "+0+" "+doc.get("DOCNO")+" "+rank+" "+docs[i].score+" "+id);
			bw.newLine();
		}
		
	}
	
	public static ArrayList<String> readTrec(String s, String tag){		
		ArrayList<String> queryList = new ArrayList<String>();
			
		try{
			int start = -1;
			int end = -1;
			int count = 0;
			
			while(true){
				++count;
				int i = s.indexOf("<top>",start+1);
				int j = s.indexOf("</top>",end+1);
								
				if(i == -1 || j == -1){
					break;
				}
				//Pass the query-terms from title and desc
				String str =s.substring(i, j);
				
				//Title tag
				if (tag.equalsIgnoreCase("title")){	
					int titleStart = str.indexOf("<title>");
					int titleEnd = str.indexOf("<",titleStart+1);
					String title= str.substring(titleStart+7, titleEnd);
					title = title.replace("Topic:", " ");
					title = title.replace('?' , ' ');
					title = title.replace('/' , ' ');
					queryList.add(title);
				}				

				//Desc tag
				if (tag.equalsIgnoreCase("desc")){
					int descStart = str.indexOf("<desc>");
					int descEnd = str.indexOf("<",descStart+1);
					String desc= str.substring(descStart+6, descEnd);
					desc=desc.replace("Description:", " ");
					desc = desc.replace("Topic:", " ");
					desc = desc.replace('?' , ' ');
					desc = desc.replace('/' , ' '); 
					queryList.add(desc);
				}	
				
				start = j;
				end = j;				
			}
		}
		catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		}	
		return queryList;
	}
}
