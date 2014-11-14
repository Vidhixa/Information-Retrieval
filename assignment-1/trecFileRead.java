package lucenedemo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.benchmark.byTask.feeds.TrecDocParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class trecFileRead {
	public String DOCNO;
	public String HEAD;
	public String BYLINE;
	public String DATELINE;
	public String TEXT;
	
	//Contructor for trecFileRead type object
	public trecFileRead(){	
		 DOCNO = this.DOCNO;
		 HEAD = this.HEAD;
		 BYLINE = this.BYLINE;
		 DATELINE = this.DATELINE;
		 TEXT = this.TEXT;
	}

	//I have used XML DOM parser to parse t
	public ArrayList<trecFileRead>  docReader(String s){
		ArrayList<trecFileRead> list= new ArrayList<trecFileRead>();
		String str;
		NodeList nodeList = null;
		try{
			 //Adding root to make it XML compatiable form
			 s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <root>\n" + s + "\n</root>"; 
			 //Replacing the & in string by spaces since & character throws parsing exception by DOM parser
			 str = s.replaceAll("&", "and");
		     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		     DocumentBuilder db = dbf.newDocumentBuilder();
		     Document dom = db.parse(new InputSource(new ByteArrayInputStream(str.getBytes("utf-8"))));
			 Element elt = dom.getDocumentElement();
			 nodeList = elt.getElementsByTagName("DOC");
		     list= parseDoc(nodeList);     
		} 
		catch(ParserConfigurationException pce) {
				pce.printStackTrace();
		}
		catch(SAXException se) {
				se.printStackTrace();
		}
		catch(IOException ioe) {
				ioe.printStackTrace();
		}	
		return list;		
	}
	     
	public static ArrayList<trecFileRead> parseDoc(NodeList n){ 
		
		   ArrayList<trecFileRead> list= new ArrayList<trecFileRead>();
		   if(n != null && n.getLength() > 0) {
				for(int i = 0 ; i < n.getLength();i++) {
					Element element = (Element)n.item(i);
					trecFileRead t= getValue(element);
					list.add(t);					
				}	
		   } 
		  return list;
	}
	
	 private static trecFileRead getValue(Element element) {
		 
		 String HEAD= " ";
		 String BYLINE= " ";
		 String TEXT= " ";
		 trecFileRead t = new trecFileRead();
		 NodeList heads = null;
		 NodeList bylines = null;
		 NodeList text = null;
		 heads = element.getElementsByTagName("HEAD");
		 bylines = element.getElementsByTagName("BYLINE");
		 text = element.getElementsByTagName("TEXT");
		 
		 //We have HEAD, BYLINE fields as multiple tags in <DOC> we parse it differently
		 if(heads != null && heads.getLength() > 0) {
				for(int i = 0 ; i < heads.getLength();i++) {
					Element e = (Element)heads.item(i);
					HEAD = HEAD+" "+e.getFirstChild().getNodeValue();
				}
			}
		 if(bylines != null && bylines.getLength() > 0) {
				for(int i = 0 ; i < bylines.getLength();i++) {
					Element e = (Element)bylines.item(i);
					BYLINE = BYLINE+" "+e.getFirstChild().getNodeValue();
				}
			}
		 
		 if(text != null && text.getLength() > 0) {
				for(int i = 0 ; i < text.getLength();i++) {
					Element e = (Element)text.item(i);
					TEXT = TEXT+" "+e.getFirstChild().getNodeValue();
				}
			}
		 		//Creating object for each <DOC> tag
		 		t.DOCNO = tagValue(element,"DOCNO");
		 		t.HEAD= HEAD;
		 		t.BYLINE= BYLINE;	
		 		t.TEXT = TEXT;
				t.DATELINE = tagValue(element, "DATELINE");
				
				return t;		  
			}

	 public static String tagValue(Element e, String tag){
		 	//Extracting tag values as required
		 	String val = " ";
			NodeList nl = e.getElementsByTagName(tag);
			
			if(nl != null && nl.getLength() > 0) {
				Element first = (Element)nl.item(0);			
				val= first.getFirstChild().getNodeValue();
			}
			return val;		
		}		 
	}
