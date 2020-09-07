package uplifts;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;


public class  UPParser {
	public ArrayList<Hashtable<String, String>> Topic_List = new ArrayList<Hashtable<String, String>>();//store the four kinds of stress topic words, each stress topic corresponds to a hashtable
	public Hashtable<String, String> Positive_Words = new Hashtable<String, String>();//store stressful words
	public Hashtable<String, Integer> Adv_Words = new Hashtable<String, Integer>();//store stressful words
	public Hashtable<String, String> Retweet = new Hashtable<String, String>();
	public Hashtable<String, String> Reply = new Hashtable<String, String>();
	public Hashtable<String, String> collectLexHash = new Hashtable<String, String>(); 
	
	//LIWC
	public ArrayList<Hashtable<String, String>> Emotion_List  = new ArrayList<Hashtable<String, String>>();//8种emotion	
		
	//addition function
	private String getCharset(String fileName) throws IOException{
         
         BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));  
         int p = (bin.read() << 8) + bin.read();  
         
         String code = null;  
         
         switch (p) {  
             case 0xefbb:  
                 code = "UTF-8";  
                 break;  
             case 0xfffe:  
                 code = "Unicode";  
                 break;  
             case 0xfeff:  
                 code = "UTF-16BE";  
                 break;  
             default:  
                 code = "GBK";  
         }  
         return code;
 }

	//addition function
	static java.sql.Date string2Date(String dateString) throws ParseException{
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
		dateFormat.setLenient(false);
		java.util.Date timeDate = dateFormat.parse(dateString);
		java.sql.Date dateTime = new java.sql.Date(timeDate.getTime());
		return dateTime;
	}
	
	//IMP: load lexicon
	public void lexiconLoad()
	{		
		//***********************Load topic lexicon*******************************************************
		String[] Files = {
				  "lex_uplifts/lexp_study.txt", //0
				  "lex_uplifts/lexp_romantic.txt",//1
		          "lex_uplifts/lexp_friends.txt",//2
				  "lex_uplifts/lexp_self.txt",//3	         
		          "lex_uplifts/lexp_family.txt", //4
		          "lex_uplifts/lexp_enter.txt",//entertainment-new lexicon 5 !!!!
		          "lex_uplifts/dic_positive.txt",//positive emotion 6
		          "lex_uplifts/lex_degree_lq.txt",//degree adverb 7
		          "lex_uplifts/retweet.txt",//social 8
		          "lex_uplifts/reply.txt"//social 9
		         };   
		
		BufferedReader reader = null;
		Hashtable<String, String> temp = null;
		String line = null;
		
		InputStreamReader isr;		
		
		//MyStaticValue.userLibrary = "library/userLibrary.dic";
		MyStaticValue.isNameRecognition = false;
		MyStaticValue.ENV.put(DicLibrary.DEFAULT, "library/userLibrary.dic");

		for (int i = 0; i < Files.length; i++)
		{				
			try
			{
				isr = new InputStreamReader(new FileInputStream(Files[i]),getCharset(Files[i]));
				reader = new BufferedReader(isr);
				String tline = reader.readLine();

				byte[] b = tline.getBytes("UTF-8");
				line = new String(b, 3, b.length-3, "UTF-8");//
				int num=0;//total number of words in all lexicons
				
				if (i < 6)                                 //add positive type words
				{
					temp = new Hashtable<String, String>();
					temp.clear();
					while (line != null)
					{
						String[] elem = line.split(" ");	
						if(elem.length>1)
						{
							temp.put(elem[0], elem[1]);	//teacher rol
						}
						else
							temp.put(elem[0], elem[0]);
						
				    	num++;
						line = reader.readLine();			
					}
					Topic_List.add(temp);	
				//	System.out.println(num);
				}
				else if (i < 7)                         //positive emotion words
				{
					while (line != null)
					{
						String[] elem = line.split(" ");				    	
				    	Positive_Words.put(elem[0], elem[0]);
				    	num++;
						line = reader.readLine();
					}
				//	System.out.println(num);
				}
				else if (i == 7)						//adverb
				{
					while(line!=null){
						String[] elem = line.split(" ");
						String adv = elem[0];
						String level = elem[1];
						Adv_Words.put(adv, Integer.parseInt(level));
						num++;
						line = reader.readLine();
					}
				//	System.out.println(num);
				}else if(i==8){
					while(line!=null){
						Retweet.put(line,line);
						line = reader.readLine();
						num++;
					}			
				//	System.out.println(num);
				}else if(i==9){
					while(line!=null){
						Reply.put(line, line);
						line = reader.readLine();
						num++;
					}			
				//	System.out.println(num);
				}
												
				System.out.println(Files[i]+"	"+num+"words");
				System.out.println("Hashtable Loading completed!");					
			}
			catch(Exception e)
			{
				System.out.println("Error: " + line);
				
			}
			finally 
			{
            	if (reader != null)
            	{
                	try 
                	{
                		
                    	reader.close();
                    	reader = null;
                	} 
                	catch (IOException e1) 
                	{
                	}
            	}
			}					
		}
	}

	//IMP: parser each tweet for positive words...
	public ArrayList<ArrayList> parserOneTweet(String tweet, int ifCollectLexicon){
		
		ArrayList<ArrayList> result = new ArrayList<ArrayList>();
		
		ArrayList<Integer> Category = new ArrayList<Integer>();// categories
				
		ArrayList<Integer> specialWords = new ArrayList<Integer>();// press + adv
		
		ArrayList<Integer> labelWords = new ArrayList<Integer>();//rol, act, des
		
		for(int i=0;i<6;i++){//0study,1romantic,2friends,3self,4family,5enter
			Category.add(0);
		}
		
		for(int i=0;i<3;i++){//rol, act, des
			labelWords.add(0);
		}
				
		int Num_positive=0;//
		int Num_adv_level=0;

		ArrayList<String> sentences = new ArrayList<String>();
	    
		Pattern pattern = Pattern.compile("[^\\. ! 。！？?~…\\s]+[\\. ! 。！？?~…\\s]+");	    	    
	    Matcher matcher = pattern.matcher(tweet);	    
	    while (matcher.find())
	    {    		
	    	sentences.add(matcher.group(0));
	    }
	    
	   // int num_words = 0;
	    	   	
	    for (String sen: sentences)
	    {		
	    	List<Term> parse_temp = (List<Term>) ToAnalysis.parse(sen);
			//List<Term> parse_temp = DicAnalysis.parse(text);
	    	ArrayList<String> parse_result = new ArrayList<String>();
	    	
	    	for (Term term: parse_temp)
	    	{ 
	    		//去掉词性标注
	    		String item = term.getName().trim();  
	    		if (item.length() > 0)
	    		{  
	    			parse_result.add(item.trim());
	    			if(ifCollectLexicon == 1){//if need to collect words for lexicon, only the initial phase need
	    				collectLexHash.put(item.trim(), item.trim());//collect all words in such training set to build the lexicon
	    			}
	    			
	    		//	num_words++;
	    		}
	    	}
	    		
	    	for(int i=0; i<parse_result.size();i++){
	    		String word = parse_result.get(i);
	    		if(word.length()>0){
	    			
	    			//number of each category; number of rol/act/des
	    			for(int j=0;j<Topic_List.size();j++){
	    				if(Topic_List.get(j).containsKey(word)){
	    					int pre = Category.get(j)+1;
	    					Category.set(j, pre);//number of words in category j
	    					
	    					String str = Topic_List.get(j).get(word);//rol/des/act
	    					if(str.equals("rol")){
	    						int n = labelWords.get(0)+1;
		    					labelWords.set(0, n);
	    					}
	    					else
	    						if(str.equals("act")||str.equals("actn")){
	    							int n = labelWords.get(1)+1;
			    					labelWords.set(1, n);
	    						}
	    						else
	    							if(str.equals("des")||str.equals("desn")){
	    								int n = labelWords.get(2)+1;
	    		    					labelWords.set(2, n);
	    							}
	    				}
	    			}

	    			if(Positive_Words.contains(word)){
	    				Num_positive++;
	    			}
	    			
	    			if(Adv_Words.containsKey(word)){
	    				Num_adv_level+=Adv_Words.get(word);
	    			}
	    				    			
	    		}
	    	}
	     }		

	    specialWords.add(Num_positive);//0
	    specialWords.add(Num_adv_level);//1
	    
	    result.add(Category);//5
	    result.add(specialWords);// 2
	    result.add(labelWords);//3
	    
		return result;
	}

	//IMP: parser each file
	public ArrayList<String> parser(String filename, int collectLexicon) throws IOException, FileNotFoundException, IOException, ParseException{
		
		ArrayList<String> result = new ArrayList<String>();
				
		//output
		String textPath = filename.replace("textID", "upLevelOrigin");	
		//String textPath = filename.replace("textID", "upTextFeature");	
		File textFile = new File(textPath);
		if(!textFile.exists()){
			textFile.createNewFile();
		}	
		FileWriter fw = new FileWriter(textFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);			
				
		//input
		InputStreamReader isr = new InputStreamReader(new FileInputStream(filename),getCharset(filename));
		BufferedReader reader = new BufferedReader(isr);
		
		String filename_time = filename.replace("textID", "timeID");
		InputStreamReader isr_time = new InputStreamReader(new FileInputStream(filename_time),getCharset(filename_time));
		BufferedReader reader_time = new BufferedReader(isr_time);
							
		DecimalFormat df = new DecimalFormat("######0.0000");
		String line = "";
		String line_time = "";
		int tID = 0;
		long endDay = 0;//the lastest day of current teen
		
		//int N = 7;	
		//int Length  = 1000;
		
		while((line = reader.readLine()) != null && (line_time = reader_time.readLine()) != null){
			if(line.length()>0 && line_time.length()>0){
				
				//time
				String time_ID = line_time;
				String[] elem = time_ID.split(" ");
				String time = elem[1]+" "+elem[2];
				
				//set the end day for this teen
				Date sDate = string2Date(time);
				long curTime = sDate.getTime()/86400000;
				if(tID == 0){
					endDay = curTime;
				}
																
				//text
				ArrayList<ArrayList> res = 	parserOneTweet(line, collectLexicon);
				ArrayList<Integer> res_cate = res.get(0);//topics GOURP: 0,1,2,3,7
				ArrayList<Integer> res_words = res.get(1);//special words GROUP:0-positive,1-adv
				ArrayList<Integer> res_rols = res.get(2);//rol, act, des
											
				//bw<<positive<<adv<<categories<<rol/act/des<<endl;
				
				int p_num = res_words.get(0);
			//	for(int i=0; i<res_cate.size();i++){
			//		p_num += res_cate.get(i);
			//	}
				int degree = res_words.get(1);				
				if(degree>0){
					p_num = p_num*degree;
				}
				if(p_num>5)
					p_num=5;
				p_num = -1*p_num;
				bw.write(tID+" "+p_num+"\r\n");
				
				for(int i=0;i<res_words.size();i++){
					
				//		bw.write(res_words.get(i)+" ");				//2 posiNum, degree	
				}				
				
				for(int i=0;i<res_cate.size();i++){
				//		bw.write(res_cate.get(i)+" ");	//6 categories
				}			
//				"lex_uplifts/lexp_study.txt", //0
//				  "lex_uplifts/lexp_romantic.txt",//1
//		          "lex_uplifts/lexp_friends.txt",//2
//				  "lex_uplifts/lexp_self.txt",//3	         
//		          "lex_uplifts/lexp_family.txt", //4
//		          "lex_uplifts/lexp_enter.txt",//entertainment-new lexicon 5 !!!!

				
				for(int i=0; i<res_rols.size(); i++){//2 rol, act, des
				//	bw.write(res_rols.get(i)+" ");
				}
				//bw.write("\r\n");
				
			}else{
				System.out.println("null tweet error..");
			}
			
			tID++;
		}		
			
		isr.close();
		isr_time.close();
		bw.close();
		
		result.add("test for group");
		return result;
	}
	
	//Addition function:  write all tweet words to file lexAll.txt
	public void collectLexRes() throws IOException{
		String lexPath = "E:\\TEST\\POSITIVE\\lexAll.txt";
		File textFile = new File(lexPath);
		if(!textFile.exists()){
			textFile.createNewFile();
		}	
		FileWriter fw = new FileWriter(textFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		 Iterator<String> it1 = collectLexHash.keySet().iterator();
         while(it1.hasNext()) {
            bw.write(it1.next());
            bw.write("\r\n");
        }        
        bw.close();
	}
}

		
		
	

