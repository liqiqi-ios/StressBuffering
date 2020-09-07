package uplifts;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ScheduleExamSummary {
	
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

	private static void summaryUplift() throws IOException {

		String names[] = {"ALL", "Holiday", "Party", "Sport", "Activity"}; 
		String years[] = {"2012","2013","2014","2015"};
		
		for(int i=0; i<names.length;i++) {
			String allTeen = "C:\\TEST\\POSITIVE\\Schedule\\Result\\EachEvent\\"+names[i]+".txt";
			File allTeenFile = new File(allTeen);
			if(!allTeenFile.exists()){
				allTeenFile.createNewFile();
			}	
			FileWriter all_fw = new FileWriter(allTeenFile.getAbsoluteFile());
			BufferedWriter bw_measures = new BufferedWriter(all_fw);
			
			for(int j=0;j<years.length;j++)
			{
				String upList = "C:\\TEST\\POSITIVE\\Schedule\\Result\\upSlideMeasures_"+names[i]+"_"+years[j]+".txt";
				InputStreamReader u_fr = new InputStreamReader(new FileInputStream(upList),getCharset(upList));
				BufferedReader u_br = new BufferedReader(u_fr);
				
				String line;
				try{
				while((line = u_br.readLine())!=null && line.length()>1){
					bw_measures.write(line+"\r\n");
				}}catch(Exception e){
					System.out.println(upList);
				}
				System.out.println(names[i]+"|"+years[j]+" done...");
				u_br.close();
			}
			bw_measures.close();
		}
	}
	
	private static void summaryStress() throws IOException {
		
		String years[] = {"2012","2013","2014","2015"};
		
		String allTeen = "C:\\TEST\\POSITIVE\\Schedule\\Result\\EachEvent\\stressAllteen.txt";
		File allTeenFile = new File(allTeen);
		if(!allTeenFile.exists()){
			allTeenFile.createNewFile();
		}	
		FileWriter all_fw = new FileWriter(allTeenFile.getAbsoluteFile());
		BufferedWriter bw_measures = new BufferedWriter(all_fw);
		for(int j=0;j<years.length;j++)
		{
			String upList = "C:\\TEST\\POSITIVE\\Schedule\\Result\\stressSlideMeasures_"+years[j]+".txt";
			InputStreamReader u_fr = new InputStreamReader(new FileInputStream(upList),getCharset(upList));
			BufferedReader u_br = new BufferedReader(u_fr);
			
			String line;
			try{
			while((line = u_br.readLine())!=null && line.length()>1){
				bw_measures.write(line+"\r\n");
			}}catch(Exception e){
				System.out.println(upList);
			}
			System.out.println(years[j]+" done...");
			u_br.close();
		}
		bw_measures.close();
	}

	public static void main(String[] args) throws Exception{
		summaryUplift();
		summaryStress();
	}
}



