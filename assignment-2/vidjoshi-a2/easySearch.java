package rankingIndex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.xml.stream.events.StartDocument;

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
import org.apache.lucene.util.BytesRef;

//Comparator for ranking my arraylist of DocumentNo and relevance
class relComparator implements Comparator<easySearch> {	
	public int compare(easySearch a, easySearch b) {
    	if (b.RELEVANCE > a.RELEVANCE){
    		return 1;
    	}
    	else if (b.RELEVANCE == a.RELEVANCE){
    		return 0;
    	}
    	else{
    		return -1;
    	}
	}
}

	public class easySearch {	
		String DOC_NO;
		double RELEVANCE;
		
	public easySearch(String doc_no, double relevance){
		this.DOC_NO = doc_no;
		this.RELEVANCE = relevance;		
	}

	public static void main(String args[])throws IOException, ParseException {
		
		String pathToIndex = "C:\\Users\\Vidhixa\\Desktop\\assignment-2\\Default";
		String queryString = "new";
	
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();	
		query.extractTerms(queryTerms);
		
		//Reading index already provided
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));
		//Use DefaultSimilarity.decodeNormValue(…) to decode normalized document length
		DefaultSimilarity dSimi=new DefaultSimilarity(); 
		//Get the segments of the index
		List<AtomicReaderContext> leafContexts = reader.getContext().reader().leaves();
	
		double relevance= 0;
		int countAllDocs = 0;
		double relTrack[] = new double[85555];
		HashMap<Integer,String> getDocID= new HashMap<Integer, String>();
		List<easySearch> rankingList = new ArrayList<easySearch>();
	
		//Relevance is calculated term-wise
		for (Term t : queryTerms) {
			HashMap<Integer, Double> map = new HashMap<Integer, Double>();
			double tf= 0;
			double idf= 0;
			int countRelDocs = 0;
			System.out.println("Relevance of each doc for ::: \n"+t);
			
			for (int i = 0; i < leafContexts.size(); i++) {
				AtomicReaderContext leafContext=leafContexts.get(i);
				int startDocNo=leafContext.docBase;				
				int numberOfDoc=leafContext.reader().maxDoc();		
				countAllDocs=(startDocNo+numberOfDoc);
			
				//Mapping the numeric document number in leaf context to its actual <DOCNO>
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
				System.out.println(getDocID.get(entry.getKey())+"\t"+relevance);
				relTrack[(int) entry.getKey()] =+ relevance;
			}
		}
		for (int j=0; j<relTrack.length; j++){
			if(relTrack[j]>0.0){
				easySearch e = new easySearch(getDocID.get(j), relTrack[j]);
				rankingList.add(e);
				}		
			}
			System.out.println("********************************************");
			System.out.println();
			printRanks(rankingList);
		}

	//Ranking the docs
	public static void printRanks(List<easySearch> rankingList){
		int rank = 0;
		System.out.println("Relevance of queryString rankwise is::");
		System.out.println();
		System.out.println("Rank\tDocument\tRelevance Score ");
		Collections.sort(rankingList, new relComparator());
		for(easySearch e: rankingList){
			++rank;
			System.out.println(rank+"\t"+e.DOC_NO+"\t"+e.RELEVANCE);			
		}
	}
	}
		
 


