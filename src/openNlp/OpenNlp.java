package openNlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.twitter.Extractor;

import Connessione.Connessione;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;


public class OpenNlp extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static String[] tagsToDelete = {
			"CC","PRP","PRP$","RBR","RBS","TO","WDT","WP","WP$","WRB","UH",".","DT",",","IN",":"," ","-RRB-","SYM"
		};

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Extractor extractor = new Extractor();

		Connessione connessione = new Connessione();
	    Connection con = connessione.connetti();
	    HttpSession session=request.getSession();
	    
	    String eventDay=(String) session.getAttribute("day");
		String timeMin=request.getParameter("timeMin");
		String timeMax=request.getParameter("timeMax");
		
		String startTileHorizS=(String) session.getAttribute("sth");
		String endTileHorizS=(String) session.getAttribute("eth");
		String startTileVertS=(String) session.getAttribute("stv");
		String endTileVertS=(String) session.getAttribute("etv");

		double startTileHoriz = Double.parseDouble(startTileHorizS);
	    double startTileVert = Double.parseDouble(startTileVertS);
	    double endTileHoriz=Double.parseDouble(endTileHorizS);
	    double endTileVert=Double.parseDouble(endTileVertS);
	    
	    InputStream modelTokenIn = new FileInputStream("/Users/Diegomarra/Documents/Universita/3ANNO/PPM/PROGETTO/nlpModel/en-token.bin"); 
		InputStream modelTagIn = new FileInputStream("/Users/Diegomarra/Documents/Universita/3ANNO/PPM/PROGETTO/nlpModel/en-pos-maxent.bin");

//		File f = new File("en-token.bin");
//		System.out.println(f.getAbsolutePath());
//		System.out.println(System.getProperty("user.dir"));

		
		POSModel model = new POSModel(modelTagIn);
		POSTaggerME tagger = new POSTaggerME(model);

		TokenizerModel modelToken = new TokenizerModel(modelTokenIn);
		Tokenizer tokenizer = new TokenizerME(modelToken);
		
	    Statement selectTweets;
		try {
			selectTweets = con.createStatement();
			ResultSet tweets=selectTweets.executeQuery("SELECT * FROM `tweets` WHERE time>="+timeMin+" AND time<="+timeMax+" AND latitude>="+startTileHoriz+" and longitude>="+startTileVert+" and latitude<"+endTileHoriz+" and longitude<"+endTileVert+" and date='"+eventDay+"'");
		
		    
		    while(tweets.next()){
		    	 List<String> hashtag=new ArrayList<String>();
		    	 List<String> username=new ArrayList<String>();
		    	 List<String> URLs=new ArrayList<String>();
		    	 
		    	ArrayList<String> wordsList = new ArrayList<String>();
				String message=tweets.getString("message");
				
				 hashtag=extractor.extractHashtags(message);
				 username=extractor.extractMentionedScreennames(message);
				 URLs=extractor.extractURLs(message);
				
				message=message.replaceAll("…", "");
				System.out.println();
				System.out.println("--------------------------------------");
				System.out.println(message);
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
					  if(!delete){
					  wordsList.add(tokens[i]);
					  }
				  }
				System.out.println("----------Parole senza stopWords-----------");
				for (String str : wordsList) {
					System.out.print(str+" ");
				}
				System.out.println();
				
				
				 System.out.println("---Hashtag---");
				 for (String tag : hashtag) {
					 System.out.println(tag);
					 }
				 
				 System.out.println("---Username---");
				 for (String user : username) {
					 System.out.println(user);
					 }
				 
				 System.out.println("---URL---");
				 for (String url : URLs) {
					 System.out.println(url);
					 }
				}
		    
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	
	}


}
