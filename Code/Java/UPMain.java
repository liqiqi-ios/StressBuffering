package uplifts;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;   
import java.text.ParseException;

import org.ansj.util.MyStaticValue;

public class UPMain {
	
	//Addition function0:getCharset
	private static String getCharset(String fileName) throws IOException{
        
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
	
	//Addition function1:filter duplicate words in positive/negative lexicons...
	public static void cleanLexicon() throws IOException
	{
		String[] Files = {
				"cleanLexicon/dic_positive.txt",
				"cleanLexicon/lex_press.txt"
		};   
		
		Hashtable<String, String> temp = null;
		Hashtable<String, String> PressSta = new Hashtable<String, String>();
		Hashtable<String, String> PositiveSta = new Hashtable<String, String>();
		
		BufferedReader reader = null;
		String line = null;				
		InputStreamReader isr;		
		
		//MyStaticValue.userLibrary = "library/userLibrary.dic"; 在配置文件中添加

		for (int i = 0; i < Files.length; i++)
		{				
			try
			{
				isr = new InputStreamReader(new FileInputStream(Files[i]),getCharset(Files[i]));
				reader = new BufferedReader(isr);
				String tline = reader.readLine();

				byte[] b = tline.getBytes("UTF-8");
				line = new String(b, 3, b.length-3, "UTF-8");//去掉UTF-8文件编码的BOM
				int p_num = 0;
				int n_num = 0;
				int both_num = 0;
				
				if(i == 0){
					while(line != null){
						PositiveSta.put(line, line);
						p_num++;
						line = reader.readLine();
					}
					System.out.println("Positive: "+p_num+"words....");
				}else{
					while(line != null){
						//if line not in positive
						if(!PositiveSta.containsKey(line)){
							PressSta.put(line, line);
							n_num++;
						}else
							both_num++;
						line = reader.readLine();
					}
				}								
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
		
		//output pressAll..
			String neg_lexPath = "E:\\TEST\\POSITIVE\\pressAll.txt";
			File neg_textFile = new File(neg_lexPath);
			if(!neg_textFile.exists()){
				neg_textFile.createNewFile();
			}	
			FileWriter neg_fw = new FileWriter(neg_textFile.getAbsoluteFile());
			BufferedWriter neg_bw = new BufferedWriter(neg_fw);
			
			 Iterator<String> neg_it = PressSta.keySet().iterator();
	         while(neg_it.hasNext()) {
	            neg_bw.write(neg_it.next());
	            neg_bw.write("\r\n");
	        }        
	        neg_bw.close();
	    
	    //output positiveAll
			String posi_lexPath = "E:\\TEST\\POSITIVE\\posiAll.txt";
			File posi_textFile = new File(posi_lexPath);
			if(!posi_textFile.exists()){
				posi_textFile.createNewFile();
			}	
			FileWriter posi_fw = new FileWriter(posi_textFile.getAbsoluteFile());
			BufferedWriter posi_bw = new BufferedWriter(posi_fw);
			
			 Iterator<String> posi_it = PositiveSta.keySet().iterator();
	         while(posi_it.hasNext()) {
	            posi_bw.write(posi_it.next());
	            posi_bw.write("\r\n");
	        }        
	        posi_bw.close();
	}

	public static void parseAllTeen(int collectLexicon) throws IOException, ParseException{
				String dirParseIn = "E:\\TEST\\POSITIVE\\Depart\\textID\\";
								
				File files2parse = new File(dirParseIn);//所有人文件夹
				if(files2parse.isDirectory()){
					String [] fileList = files2parse.list();
					UPParser curFile = new UPParser();
					curFile.lexiconLoad();
					for(int pos_file=0; pos_file<fileList.length; pos_file++){
						
						String filePath = dirParseIn+fileList[pos_file];
						
						ArrayList<String> result = curFile.parser(filePath, collectLexicon);	//upLevelOrigin				
						System.out.println(fileList[pos_file]+"	parser done");
					}
					
					if(collectLexicon == 1){
						System.out.println("lex collection start..");
						curFile.collectLexRes();
						System.out.println("lex collection end..");
					}
					System.out.println("All parser done....");
				}
	}
	
	//step2: judge positive or stress for each tweet
	public static void choosePosiNeg() throws IOException{

		String dirParseIn = "E:\\TEST\\POSITIVE\\Depart\\upLevelOrigin\\";
		File files2parse = new File(dirParseIn);
		
		if(files2parse.isDirectory()){
			
			String [] fileList = files2parse.list();
			
			for(int pos_file=0; pos_file<fileList.length; pos_file++){
				
				String PPath = dirParseIn+fileList[pos_file];

				InputStreamReader isr = new InputStreamReader(new FileInputStream(PPath),getCharset(PPath));
				BufferedReader reader = new BufferedReader(isr);

				String SPath = PPath.replace("upLevelOrigin", "labelNewOrigin");
				InputStreamReader s_isr = new InputStreamReader(new FileInputStream(SPath),getCharset(SPath));
				BufferedReader s_reader = new BufferedReader(s_isr);

				String PCombinePath = PPath.replace("upLevelOrigin", "upLevel");	
				File PFile = new File(PCombinePath);
				if(!PFile.exists()){
					PFile.createNewFile();
				}	
				FileWriter fw = new FileWriter(PFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);//upLevel

				String SCombinePath = PPath.replace("upLevelOrigin", "labelNew");	
				File SFile = new File(SCombinePath);
				if(!SFile.exists()){
					SFile.createNewFile();
				}	
				FileWriter s_fw = new FileWriter(SFile.getAbsoluteFile());
				BufferedWriter s_bw = new BufferedWriter(s_fw);//labelNew
				
				//compare
				String line = "", s_line="";
				int tID = 0;

				while((line = reader.readLine()) != null && (s_line = s_reader.readLine()) != null){
					if(line.length()>0 && s_line.length()>0){
						String[] elem = line.split(" ");
						String pLevel = elem[1];
						String[] s_elem = s_line.split(" ");
						String sLevel = s_elem[1];
						
						int p = Integer.parseInt(pLevel);
						int s = Integer.parseInt(sLevel);
						
						int p2 = -1*p; //positive
						if(p2>s)
							s=0;
						else
							p2=0;

						bw.write(tID+" "+p2+"\r\n");
						s_bw.write(tID+" "+s+"\r\n");
						
						tID++;
					}
				}
				
				System.out.println(fileList[pos_file]);
				
				reader.close();
				s_reader.close();
				bw.close();
				s_bw.close();
			}
			System.out.println("All compare done....");
		}		
	}
	
	public static void main(String[] args) throws IOException, ParseException{
	
	// function: parse linguistic words for each tweet
	//	int flagLex = 0;
	//	parseAllTeen(flagLex);
	//addition function 1: modify lexicons
	//	cleanLexicon();
	//addition function 2: choose positive or negative for each tweet
		choosePosiNeg();
	}	
}
