package textMining;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.twitter.Extractor;

import Connessione.Connessione;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import Utility.Utility;


public class TextMining extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	public static String[] tagsToDelete = {
			"CC","PRP","PRP$","RBR","RBS","TO","WDT","WP","WP$","WRB","UH",".","DT",",","IN",":"," ","-RRB-","SYM","VBZ","CD"
		};

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session=request.getSession();
		Extractor extractor = new Extractor();
		response.setContentType("application/json");
        Map<String, Map<String,Object>> result = new HashMap<String, Map<String, Object>>();

        
		Connessione connessione = new Connessione();
	    Connection con = connessione.connetti();
	    
	    Utility ut=new Utility();
	    String startTileHorizS=request.getParameter("sth");
		String endTileHorizS=request.getParameter("eth");
		String startTileVertS=request.getParameter("stv");
		String endTileVertS=request.getParameter("etv");
		String eventDay=(String) session.getAttribute("eventDay");
	 
		String timePeakS=request.getParameter("timePeak");
		int timePeak=Integer.parseInt(timePeakS);
		String timeMin=ut.convertSecondsToHhMmSs((timePeak-2)*3600);
	    String timeMax=ut.convertSecondsToHhMmSs((timePeak+2)*3600);
	    if(timePeak>=22){
	    	 timeMax=ut.convertSecondsToHhMmSs((23)*3600+3599);
	    }
	    if(timePeak<=1){
	    	 timeMin=ut.convertSecondsToHhMmSs(0);
	    }
	    

	    
	    
		double startTileHoriz = Double.parseDouble(startTileHorizS);
	    double startTileVert = Double.parseDouble(startTileVertS);
	    double endTileHoriz=Double.parseDouble(endTileHorizS);
	    double endTileVert=Double.parseDouble(endTileVertS);
	    
	    InputStream modelTokenIn = new FileInputStream("/Users/Diegomarra/Documents/Universita/3ANNO/PPM/PROGETTO/nlpModel/en-token.bin"); 
		InputStream modelTagIn = new FileInputStream("/Users/Diegomarra/Documents/Universita/3ANNO/PPM/PROGETTO/nlpModel/en-pos-maxent.bin");
		InputStream modelNameIn = new FileInputStream("/Users/Diegomarra/Documents/Universita/3ANNO/PPM/PROGETTO/nlpModel/en-ner-person.bin");

		
		POSModel model = new POSModel(modelTagIn);
		POSTaggerME tagger = new POSTaggerME(model);
		
		TokenizerModel modelToken = new TokenizerModel(modelTokenIn);
		Tokenizer tokenizer = new TokenizerME(modelToken);
		
		PorterStemmer stemmer=new PorterStemmer();
		
		TokenNameFinderModel modelTokenNameFinder = new TokenNameFinderModel(modelNameIn);
		NameFinderME nameFinder = new NameFinderME(modelTokenNameFinder);
	    try {
	    	
			Statement selectTweetsInRange = con.createStatement();
			String tweetsQuery="SELECT * FROM `tweets` WHERE time>='"+timeMin+"' AND time<='"+timeMax+"' AND latitude>="+startTileHoriz+" and longitude>="+startTileVert+" and latitude<"+endTileHoriz+" and longitude<"+endTileVert+" and date='"+eventDay+"'";
			System.out.println(tweetsQuery);
			ResultSet tweetsInRange=selectTweetsInRange.executeQuery(tweetsQuery);
		
			List<List<String>> hashtag=new ArrayList<List<String>>();
			List<List<String>> wordsList= new ArrayList<List<String>>();
			List<List<String>> URLsList= new ArrayList<List<String>>();
			List<List<String>> namedEntitiesList= new ArrayList<List<String>>();

			int nTweets=0;
			Map<String, Object> tweets= new LinkedHashMap<String, Object>();
			Map<String,Object> pictures= new  LinkedHashMap<String,Object>();
		    Map<String,Object> hashTagList= new  LinkedHashMap<String,Object>();
		    Map<String, Object> frequencies=  new LinkedHashMap<String, Object>();
		    Map<String, Object> entitiesList=new LinkedHashMap<String, Object>();

			 if(tweetsInRange.next()){
				 System.out.println("Non ci sono tweets disponibili");
			 }
			 tweetsInRange.beforeFirst();
			
		    while(tweetsInRange.next()){
		    	 List<String> hashtagTemp=new ArrayList<String>();
		    	 List<String> username=new ArrayList<String>();
		    	 List<String> URLsTemp=new ArrayList<String>();
		    	 List<String> namedEntitiesTemp=new ArrayList<String>();
		    	 
		    	ArrayList<String> wordsListTemp = new ArrayList<String>();
				String message=tweetsInRange.getString("message");
				nTweets++;
				String profileImage=tweetsInRange.getString("profile_image");
				tweets.put(message, profileImage);
//				System.out.println();
//				System.out.println("--------------------------------------");
//				System.out.println(message);
				
				String mediaUrl=tweetsInRange.getString("media_url");
				if(!mediaUrl.isEmpty()){
					pictures.put(mediaUrl, 0);
				}
				
				URLsTemp=extractor.extractURLs(message);
				
				String tokensForName[] = tokenizer.tokenize(message);
				Span nameSpans[] = nameFinder.find(tokensForName);
				String namedEntities[]=Span.spansToStrings(nameSpans, tokensForName);
				
//				System.out.println("-----------------------");
//				System.out.println("Trovate "+namedEntities.length+" entità");
				for(int i=0;i<namedEntities.length;i++){
					namedEntitiesTemp.add(namedEntities[i]);
				}
				
				message=message.toLowerCase();
				hashtagTemp=extractor.extractHashtags(message);
				username=extractor.extractMentionedScreennames(message);
				
				if(!hashtagTemp.isEmpty()){
					hashtag.add(hashtagTemp);
				}
				
				message=message.replaceAll("…", "");
				message=message.replaceAll("#", "");
				message=message.replaceAll("@", "");
				message= message.replaceAll("https?://\\S+\\s?", "");
				message=message.replaceAll("\\W", " ");
				message=message.replaceAll("_", "");
				
				String tokens[] = tokenizer.tokenize(message);
				String[] tags = tagger.tag(tokens);
			
				for(int i=0; i<tokens.length;i++){
					  
					boolean delete=false;
					  //System.out.println(tokens[i]+" "+tags[i]);
					  for (int j = 0; j < tagsToDelete.length; j++) {
						  
						  if(tags[i].equals(tagsToDelete[j]) || tokens[i].equals("https")){
							delete=true;  
						  }
					  }
					  if(!hashtagTemp.isEmpty()){
						  for (String tag : hashtagTemp) {
							  if(tokens[i].equals(tag)){
								 delete=true; 
							  }
						  	}
					  }
					  if(!username.isEmpty()){
						  for (String user : username) {
							  if(tokens[i].equals(user)){
								 delete=true; 
							  }
						  }
					  }
					  if(!URLsTemp.isEmpty()){
						  for (String url : URLsTemp) {
							  if(tokens[i].equals(url)){
								 delete=true; 
							  }
						  }
					  }
					  if(!namedEntitiesTemp.isEmpty()){
						  for (String entity : namedEntitiesTemp) {
							  entity.toLowerCase();
							  if(tokens[i].equals(entity)){
								 delete=true; 
							  }
						  }
					  }
					  tokens[i]=stemmer.stem(tokens[i]);
					  if(!delete){
					  wordsListTemp.add(tokens[i]);
					  }
				  }
				
				if(!wordsListTemp.isEmpty()){
					wordsList.add(wordsListTemp);
				}
				if(!URLsTemp.isEmpty()){
					URLsList.add(URLsTemp);
				}
				if(!namedEntitiesTemp.isEmpty()){
					namedEntitiesList.add(namedEntitiesTemp);
				}
				
//				System.out.println("----------Parole senza stopWords-----------");
//				for (String str : wordsListTemp) {
//					System.out.print(str+" ");
//				}
//				System.out.println();
//				
//				
//				System.out.println("---Hashtag---");
//				for (String tag : hashtagTemp) {
//					 System.out.println(tag);
//					 }
//				 
//				 System.out.println("---Username---");
//				 for (String user : username) {
//					 System.out.println(user);
//					 }
//				 
//				 System.out.println("---URL---");
//				 for (String url : URLsTemp) {
//					 System.out.println(url);
//					 }
				}
			 
			result.put("tweets", tweets);
			result.put("pictures", pictures);
		    selectTweetsInRange.close();
		    
		    for (Map.Entry<String, Object> entry : pictures.entrySet()) {
			System.out.println("Pictures: "+entry.getKey());
			}
		    
		    
		    LinkedHashMap<String,Integer> hashTags= new  LinkedHashMap<String,Integer>();

		   
		    if(!hashtag.isEmpty()){
				List<List<String>> hashtagStemmed=new ArrayList<List<String>>();
	
				 for (List<String> tagList : hashtag) {
						List<String> hashtagStemmedTemp=new ArrayList<String>();
						for (String tag : tagList) {
				    		tag=stemmer.stem(tag);
				    		hashtagStemmedTemp.add(tag);
				    		}
						hashtagStemmed.add(hashtagStemmedTemp);
				    }
			    
			    
			    HashMap<String,Integer> unsortedHashTags= new HashMap<String, Integer>();
			    for (List<String> tagList : hashtagStemmed) {
			    	
			    	for (String tag : tagList) {
			    		int nTag=countWord(hashtagStemmed,tag);
			    		unsortedHashTags.put(tag, nTag);
			    		}
			    }
			    
			    hashTags=(LinkedHashMap<String, Integer>) sortByComparatorInt(unsortedHashTags);
			    
//			    System.out.println("Numero hashtags trovati: "+hashTags.size());
				for (Map.Entry<String, Integer> entry : hashTags.entrySet()) {
					hashTagList.put(entry.getKey(), entry.getValue());
					
				}
				result.put("hashtags", hashTagList);
				
//			    List<Entry<String,Integer>> entryListHashtag =new ArrayList<Map.Entry<String, Integer>>(hashTags.entrySet());
//			    Entry<String, Integer> bestHashtag1 =entryListHashtag.get(entryListHashtag.size()-1);
//			    Entry<String, Integer> bestHashtag2 =entryListHashtag.get(entryListHashtag.size()-2);
//			    Entry<String, Integer> bestHashtag3 =entryListHashtag.get(entryListHashtag.size()-3);
//	
//	
//				for (List<String> tweet : cleanTweetsTags) {
//			    		if(tweet.contains(bestHashtag1.getKey())
//								||tweet.contains(bestHashtag2.getKey())
//								||tweet.contains(bestHashtag3.getKey())){
//							System.out.println(tweet);
//							}
//			    	}
		    }else{
		    	result.put("hashtags", hashTagList);
		    	System.out.println("No hashtags trovati");
		    }
		
//		    if(!wordsList.isEmpty()){
//			    HashMap<String,Integer> unsortedWords= new HashMap<String, Integer>();
//			    LinkedHashMap<String,Integer> wordsFrequency= new  LinkedHashMap<String,Integer>();
//		    	 for (List<String> words : wordsList) {
//				    	for (String word : words) {
//				    		int nWords=countWord(wordsList,word);
//				    		unsortedWords.put(word, nWords);
//				    		}
//				 }
//		    	
//		    	 wordsFrequency=(LinkedHashMap<String, Integer>) sortByComparatorInt(unsortedWords);
//				    
////				    System.out.println("Numero parole trovate: "+wordsFrequency.size());
////					for (Map.Entry<String, Integer> entry : wordsFrequency.entrySet()) {
////						System.out.println(entry.getValue()+" "+entry.getKey());
////						
////					}
//		    	 
//			result.put("words", wordsFrequency);
//		    	
//		    }
		    
		    if(!wordsList.isEmpty()){
			    HashMap<String,Integer> unsortedWords= new HashMap<String, Integer>();
			    LinkedHashMap<String,Integer> wordsFrequency= new  LinkedHashMap<String,Integer>();
		    	 for (List<String> words : wordsList) {
				    	for (String word : words) {
				    		int nWords=countWord(wordsList,word);
				    		unsortedWords.put(word, nWords);
				    		}
				 }
		    	
		    	 wordsFrequency=(LinkedHashMap<String, Integer>) sortByComparatorInt(unsortedWords);
		    	 
		    	  
					for (Map.Entry<String, Integer> entry : wordsFrequency.entrySet()) {
						frequencies.put(entry.getKey(), entry.getValue());
						
					}
			
			result.put("words", frequencies);
		    	
		    }
//		    if(!URLsList.isEmpty()){
//			    HashMap<String,Integer> unsortedURLs= new HashMap<String, Integer>();
//			    LinkedHashMap<String,Integer> pictures= new  LinkedHashMap<String,Integer>();
//		    	 for (List<String> URLs : URLsList) {
//				    	for (String URL : URLs) {
//				    		int nURLs=countWord(URLsList,URL);
//				    		unsortedURLs.put(URL, nURLs);
//				    		}
//				 }
//		    	
//		    	 pictures=(LinkedHashMap<String, Integer>) sortByComparatorInt(unsortedURLs);
//				    
////					for (Map.Entry<String, Integer> entry : pictures.entrySet()) {
////						System.out.println(entry.getValue()+" "+entry.getKey());
////						
////					}
//		    	 
//			result.put("pictures", pictures);
//		    	
//		    }
		    
		    if(!namedEntitiesList.isEmpty()){
			    HashMap<String,Integer> unsortedNamedEntities= new HashMap<String, Integer>();
			    LinkedHashMap<String,Integer> namedEntities= new  LinkedHashMap<String,Integer>();
		    	 for (List<String> namedEntity : namedEntitiesList) {
				    	for (String name : namedEntity) {
				    		int nNames=countWord(namedEntitiesList,name);
				    		System.out.println(name+" "+nNames);
				    		unsortedNamedEntities.put(name, nNames);
				    		}
				 }
		    	
		    	 namedEntities=(LinkedHashMap<String, Integer>) sortByComparatorInt(unsortedNamedEntities);
				    
					for (Map.Entry<String, Integer> entry : namedEntities.entrySet()) {
						entitiesList.put(entry.getKey(), entry.getValue());
					}
		    	 
			result.put("names", entitiesList);
		    	
		    }
		    
		    
		    String json= new Gson().toJson(result);
		    response.getWriter().write(json);
		    
		    
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public double tfCalculator(List<String> totalterms, String termToCheck) {
        double count = 0;  
        for (String s : totalterms) {
            if (s.equalsIgnoreCase(termToCheck)) {
                count++;
            }
        }
        return count / totalterms.size();
    }
	
	 public double idfCalculator(List <String> allTerms, String termToCheck) {
	        double count = 0;
	       
	            for (String s : allTerms) {
	                if (s.equalsIgnoreCase(termToCheck)) {
	                    count++;
	                    break;
	                }
	            }
	        
	        return 1 + Math.log(allTerms.size() / count);
	    }

	 public int countWord(List<List<String>>  allTerms, String termToCheck) {
	        int count = 0;
	        
	        for (List<String> hashtag : allTerms) {
	        	for (String tag : hashtag) {
	                if (tag.equalsIgnoreCase(termToCheck)) {
	                    count++;
	                    break;
	                }
	        	}
	        }
	        
	        return  count;
	    }

	 private static Map<String, Double> sortByComparatorDouble(Map<String, Double> unsortMap) {

			// Convert Map to List
			List<Map.Entry<String, Double>> list = 
				new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

			// Sort list with comparator, to compare the Map values
			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
				public int compare(Map.Entry<String, Double> o1,
	                                           Map.Entry<String, Double> o2) {
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});

			Collections.reverse(list);
			// Convert sorted map back to a Map
			Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
			for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
				Map.Entry<String, Double> entry = it.next();
				sortedMap.put(entry.getKey(), entry.getValue());
			}
			return sortedMap;
		}
	 private static Map<String, Integer> sortByComparatorInt(Map<String, Integer> unsortMap) {

			// Convert Map to List
			List<Map.Entry<String, Integer>> list = 
				new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

			// Sort list with comparator, to compare the Map values
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1,
	                                           Map.Entry<String, Integer> o2) {
					return (o1.getValue()).compareTo(o2.getValue());
				}
			});
			Collections.reverse(list);

			// Convert sorted map back to a Map
			Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
			for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
				Map.Entry<String, Integer> entry = it.next();
				sortedMap.put(entry.getKey(), entry.getValue());
			}
			return sortedMap;
		}


}
