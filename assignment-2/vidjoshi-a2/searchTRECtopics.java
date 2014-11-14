package rankingIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class searchTRECtopics {
	
	public static void main(String args[]) throws IOException{
		try{
			File docRead = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\topics.51-100");
			BufferedReader reader = new BufferedReader(new FileReader (docRead));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");
			
			while((line = reader.readLine()) != null ) {
			        stringBuilder.append( line );
			        stringBuilder.append( ls );
			}
			
			String doc = stringBuilder.toString();
			readTrec(doc);
			
			reader.close();
			
			}catch(Exception e){
				System.err.println("Error: " + e.getMessage());
			}
	}

	public static void readTrec(String s) throws IOException, ParseException{		
		File writeLong = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\myRankingAlgolongQuery.txt");
		File writeShort = new File("C:\\Users\\Vidhixa\\Desktop\\assignment-2\\myRankingAlgoshortQuery.txt");
		BufferedWriter bwLong = new BufferedWriter(new FileWriter(writeLong));
		BufferedWriter bwShort= new BufferedWriter(new FileWriter(writeShort));
		
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
			
			//Title tag:short queries
			int titleStart = str.indexOf("<title>");
			int titleEnd = str.indexOf("<",titleStart+1);
			String title= str.substring(titleStart+7, titleEnd);
			title = title.replace("Topic:", " ");
			title = title.replace('?' , ' ');
			title = title.replace('/' , ' ');
			
			queryEvaluate(title, bwShort, count, "MY_RANKshort");
			
			//Desc tag:long queries
			int descStart = str.indexOf("<desc>");
			int descEnd = str.indexOf("<",descStart+1);
			String desc= str.substring(descStart+6, descEnd);
			desc=desc.replace("Description:", " ");
			desc = desc.replace('?' , ' ');
			desc = desc.replace('/' , ' '); 
			
			queryEvaluate(desc, bwLong, count, "MY_RANKlong");				
			
			start = j;
			end = j;				
			}
	
	}

	//Evaluate the rankings and output them into corresponding files
	public static void queryEvaluate(String queryString, BufferedWriter bw, int querynum, String id) throws ParseException, IOException{
		String pathToIndex = "C:\\Users\\Vidhixa\\Desktop\\assignment-2\\Default";

		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();	
		query.extractTerms(queryTerms);
		
		//Reading index already provided
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
		DefaultSimilarity dSimi=new DefaultSimilarity();
		List<AtomicReaderContext> leafContexts = reader.getContext().reader().leaves();
		
		double relevance= 0;
		int countAllDocs = 0;
		double relTrack[] = new double[85555];
		HashMap<Integer, String> getDocID = new HashMap<Integer, String>();
		List<easySearch> rankingList = new ArrayList<easySearch>();
		
		for (Term t : queryTerms) {
			HashMap<Integer, Double> map = new HashMap<Integer, Double>();
			double tf= 0;
			double idf= 0;
			int countRelDocs = 0;
			for (int i = 0; i < leafContexts.size(); i++) {
				AtomicReaderContext leafContext=leafContexts.get(i);
				int startDocNo=leafContext.docBase;				
				int numberOfDoc=leafContext.reader().maxDoc();		
				countAllDocs=(startDocNo+numberOfDoc);
				
				for(int j = 0; j<numberOfDoc ; j++){
					String docid = leafContext.reader().document(j).get("DOCNO");
					getDocID.put((j+startDocNo), docid);
				}
				     
				//Get the term frequency of "new" within each document containing it for <field>TEXT</field>
				DocsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),	
														MultiFields.getLiveDocs(leafContext.reader()),
														"TEXT", 
														t.bytes());
				if (de!=null){
				int doc;
				while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {	
					//System.out.println(" word occurs "+de.freq() + " times in doc(" +(de.docID()+startDocNo)+") for the field TEXT");
					float normal=dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(de.docID())); 
					tf = de.freq()/normal;
					++countRelDocs;
					map.put((de.docID()+startDocNo), tf);
					}
				}							
			}			
			//calculating IDF for the current term
			if (countRelDocs != 0){
			idf = Math.log(1+(countAllDocs/countRelDocs));
			}
			
			//Iterating the list to display individual term relevance
			Set set = map.entrySet();
			Iterator iter = set.iterator(); 
			while(iter.hasNext()) { 
				Map.Entry entry = (Map.Entry)iter.next(); 
				Double val= (Double) entry.getValue();
				relevance = idf * val;
				relTrack[(int) entry.getKey()] =+ relevance;
			}
		}
		
		for (int j=0; j<relTrack.length; j++){
			if(relTrack[j]>0.0){
				easySearch e = new easySearch(getDocID.get(j), relTrack[j]);
				rankingList.add(e);
				}		
			}
			
		int rank = 0;
		Collections.sort(rankingList, new relComparator());
		for(easySearch e: rankingList){
			++rank;
			if(rank<1000){
			bw.write((querynum+50)+" "+0+" "+e.DOC_NO+" "+rank+" "+e.RELEVANCE+" "+id);
			bw.newLine();
			}
	}
}

}
	