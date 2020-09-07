package uplifts;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class ScheduleExam{

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
	
	//Tool: schedule list to arrays
	static ArrayList<ArrayList<Integer> > scheduleToAry(String yearNum, String name) throws UnsupportedEncodingException, FileNotFoundException, IOException, ParseException{
		ArrayList<ArrayList<Integer> > res = new ArrayList();
		
		String upList = "C:\\TEST\\POSITIVE\\Schedule\\SpecificEvent\\"+name+yearNum+"USI.txt";
		String stressorList = "C:\\TEST\\POSITIVE\\Schedule\\SpecificEvent\\"+yearNum+"SI.txt";

		InputStreamReader u_fr = new InputStreamReader(new FileInputStream(upList),getCharset(upList));
		BufferedReader u_br = new BufferedReader(u_fr);
		
		InputStreamReader s_fr = new InputStreamReader(new FileInputStream(stressorList),getCharset(stressorList));
		BufferedReader s_br = new BufferedReader(s_fr);
		
		ArrayList<Integer> small_upAry = new ArrayList();
		ArrayList<Integer> big_upAry = new ArrayList();
		ArrayList<Integer> small_strAry = new ArrayList();
		ArrayList<Integer> big_strAry = new ArrayList();

		String u_line;
		try{
		while((u_line = u_br.readLine())!=null && u_line.length()>1){
			String [] elem = u_line.split(" ");		
			if(elem.length>3){
				String s_date = elem[1];//small
				String b_date = elem[2];//big
				int s_point = daysBetween("1990/1/1", s_date);
				int b_point = daysBetween("1990/1/1", b_date);
				small_upAry.add(s_point);
				big_upAry.add(b_point);
			}
			else{
				System.out.println("why_up_"+u_line);
			}
		}}catch(Exception e){
			System.out.println(upList);
		}

		String s_line;
		while((s_line = s_br.readLine())!= null && s_line.length()>1){
			String [] elem = s_line.split(" ");				
			if(elem.length>3){
				String s_date = elem[1];//small
				String b_date = elem[2];//big
				int s_point = daysBetween("1990/1/1", s_date);
				int b_point = daysBetween("1990/1/1", b_date);
				small_strAry.add(s_point);
				big_strAry.add(b_point);
			}
			else{
				System.out.println("why_stress_"+s_line);
			}
		}				
		System.out.println(name+"|"+yearNum+":successful read in ...");		
	
		u_br.close();
		s_br.close();
		
		res.add(small_upAry);
		res.add(big_upAry);
		res.add(small_strAry);
		res.add(big_strAry);
		return res;
	}
	
	//Function: find scheduled uplift/stress measures
	static void scheduleUplift(String yearNum, String name) throws Exception{
		
		DecimalFormat df = new DecimalFormat("#####0.0000");

		String allTeenU = "C:\\TEST\\POSITIVE\\Schedule\\Result\\upSlideMeasures_"+name+"_"+yearNum+".txt";
		File allTeenUFile = new File(allTeenU);
		if(!allTeenUFile.exists()){
			allTeenUFile.createNewFile();
		}	
		FileWriter all_up_fw = new FileWriter(allTeenUFile.getAbsoluteFile());
		BufferedWriter bw_teen_uplift = new BufferedWriter(all_up_fw);
		
		String allTeenS = "C:\\TEST\\POSITIVE\\Schedule\\Result\\stressSlideMeasures_"+yearNum+".txt";
		File allTeenSFile = new File(allTeenS);
		if(!allTeenSFile.exists()){
			allTeenSFile.createNewFile();
		}	
		FileWriter all_str_fw = new FileWriter(allTeenSFile.getAbsoluteFile());
		BufferedWriter bw_teen_stress = new BufferedWriter(all_str_fw);		

		String listPath = "C:\\TEST\\POSITIVE\\Schedule\\userList_"+yearNum+".txt";
		InputStreamReader user_reader = new InputStreamReader(new FileInputStream(listPath),getCharset(listPath));
		BufferedReader userSet = new BufferedReader(user_reader);
		String cur_user;
		while((cur_user=userSet.readLine())!=null){							
			String s_cate_userPath = "C:\\TEST\\POSITIVE\\CateSta\\"+cur_user;
			String s_userPath = "C:\\TEST\\POSITIVE\\DaySta\\"+cur_user;
			String u_cate_userPath = "C:\\TEST\\POSITIVE\\upCateSta\\"+cur_user;
			String u_userPath = "C:\\TEST\\POSITIVE\\upDaySta\\"+cur_user;

			InputStreamReader s_cate_reader = new InputStreamReader(new FileInputStream(s_cate_userPath),getCharset(s_cate_userPath));
			BufferedReader sCateSta = new BufferedReader(s_cate_reader);			
			//stress category
			InputStreamReader s_reader = new InputStreamReader(new FileInputStream(s_userPath),getCharset(s_userPath));
			BufferedReader sDaySta = new BufferedReader(s_reader);

			InputStreamReader u_cate_reader = new InputStreamReader(new FileInputStream(u_cate_userPath),getCharset(u_cate_userPath));
			BufferedReader uCateSta = new BufferedReader(u_cate_reader);			
			//up category
			InputStreamReader u_reader = new InputStreamReader(new FileInputStream(u_userPath),getCharset(u_userPath));
			BufferedReader uDaySta = new BufferedReader(u_reader);

			ArrayList<Integer> upDayAry = new ArrayList();
			ArrayList<Integer> stressDayAry = new ArrayList();	
			ArrayList<ArrayList<Integer> > upCateAry = new ArrayList();
			ArrayList<ArrayList<Integer> >strCateAry = new ArrayList();
			int L = daysBetween("1990/1/1", "2018/1/1");
			for(int i=0; i<L; i++){
				upDayAry.add(0);
				stressDayAry.add(0);
				ArrayList<Integer> ary1 = new ArrayList();
				for(int j=0; j<6; j++)
					ary1.add(0);
				ArrayList<Integer> ary2 = new ArrayList();
				for(int j=0; j<5; j++)
					ary2.add(0);
				upCateAry.add(ary1);
				strCateAry.add(ary2);
			}

			String sLevel, scate, uLevel, ucate;
			while((sLevel = sDaySta.readLine())!=null && (scate=sCateSta.readLine())!=null 
					&&(uLevel = uDaySta.readLine())!=null && (ucate=uCateSta.readLine())!=null){
				//1-levels
				String [] s_elem = sLevel.split(" ");
				int s_level = Integer.parseInt(s_elem[0]);//accumulated stress
				
				String [] u_elem = uLevel.split(" ");
				int u_level = Integer.parseInt(u_elem[0]);//accumulated positive
				
				//2-categories
				String [] s_cat_elem = scate.split(" "); //5+3+3+year+month+day
				String [] u_cat_elem = ucate.split(" ");
				
				//3-find the date
				if(s_cat_elem.length == 15){//namely, has post cur day
					String y = s_cat_elem[11];
					String m = s_cat_elem[12];
					String d = s_cat_elem[13];
					y = y.replace(",", "");
					String newDay = y+"/"+m+"/"+d;
					//System.out.println("|"+newDay+"|");
					if(newDay.length()==0)
						System.out.println("此处长度为零");
					int cur_point = daysBetween("1990/1/1", newDay);					
					if(cur_point < L){
						//upDayAry.add(0);
						//stressDayAry.add(0);
						//upCateAry.add("");
						//strCateAry.add("");
						upDayAry.set(cur_point, u_level);
						stressDayAry.set(cur_point, s_level);						
						for(int k=0; k<6; k++){
							int typeNum = Integer.parseInt(u_cat_elem[k]);
							upCateAry.get(cur_point).set(k,typeNum);
						}
						for(int k=0; k<5; k++){
							int typeNum = Integer.parseInt(s_cat_elem[k]);
							strCateAry.get(cur_point).set(k,typeNum);							
						}						
					}else{
						throw new Exception("error length..");
					}
				}				
			}			
			sDaySta.close();
			sCateSta.close();

			ArrayList<ArrayList<Integer> > R= scheduleToAry(yearNum, name);
			if(R.size()!=4){
				throw new Exception("schedule list error...");
			}
			ArrayList<Integer> small_upAry = R.get(0);
			ArrayList<Integer> big_upAry = R.get(1);
			ArrayList<Integer> small_strAry = R.get(2);
			ArrayList<Integer> big_strAry = R.get(3);
			System.out.println(cur_user+" success");
			
			ArrayList<Double> sche1_s_ResAry = new ArrayList();
			ArrayList<Double> sche1_u_ResAry = new ArrayList();
			for(int k=0; k<15; k++){
				sche1_s_ResAry.add(0.0);
				sche1_u_ResAry.add(0.0);
			}
			sche1_u_ResAry.add(0.0);
			sche1_u_ResAry.add(0.0);

			int slideNum = 0;
			for(int k=0; k<small_strAry.size(); k++){
				int sDate = small_strAry.get(k);
				int eDate = big_strAry.get(k);				

				ArrayList<Double> r_s = calcuSlide(sDate, eDate, stressDayAry, strCateAry, L);
				ArrayList<Double> r_u = calcuSlide(sDate, eDate, upDayAry, upCateAry, L);
				
				if(r_s.get(0)>0)
				{
					 slideNum++;
					 if(r_s.size()!=sche1_s_ResAry.size() || r_u.size()!=sche1_u_ResAry.size())
						{
							System.out.println(r_u.size()+" "+sche1_u_ResAry.size());
							throw new Exception("ary error..");
						}

						for(int p=0;p<r_s.size();p++){
							double pre = sche1_s_ResAry.get(p);
							sche1_s_ResAry.set(p, pre + r_s.get(p));
						}
						for(int p=0; p<r_u.size();p++){
							double pre = sche1_u_ResAry.get(p);
							sche1_u_ResAry.set(p, pre+ r_u.get(p));
						}
				}				
			}

			if(slideNum>0){
				for(int p=0; p<sche1_s_ResAry.size();p++){
					double pre = sche1_s_ResAry.get(p);//calculate the average value for current teen..
					sche1_s_ResAry.set(p, pre*1.0/slideNum);
					bw_teen_stress.write(df.format(pre*1.0/slideNum)+" ");
				}
				
				//up
				for(int p=0; p<sche1_u_ResAry.size();p++){
					double pre = sche1_u_ResAry.get(p);
					sche1_u_ResAry.set(p, pre*1.0/slideNum);
					bw_teen_stress.write(df.format(pre*1.0/slideNum)+" ");
				}
				bw_teen_stress.write("\r\n");
			}
			else{
				String s = "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
				//bw_teen_stress.write(cur_user+" "+s+"\r\n");
				bw_teen_stress.write(s+"\r\n");
			}

			int up_slideNum = 0;
			ArrayList<Double> sche2_s_ResAry = new ArrayList();
			ArrayList<Double> sche2_u_ResAry = new ArrayList();
			for(int k=0; k<15; k++){
				sche2_s_ResAry.add(0.0);
				sche2_u_ResAry.add(0.0);
			}
			sche2_u_ResAry.add(0.0);
			sche2_u_ResAry.add(0.0);
			
			for(int k=0; k<small_upAry.size(); k++){
				int sDate = small_upAry.get(k);
				int eDate = big_upAry.get(k);
				ArrayList<Double> r_s = calcuSlide(sDate, eDate, stressDayAry, strCateAry, L);
				ArrayList<Double> r_u = calcuSlide(sDate, eDate, upDayAry, upCateAry, L);
				
				if(r_s.get(0)>0) 
				{
					up_slideNum++;
					if(r_s.size()!=sche2_s_ResAry.size() || r_u.size()!=sche2_u_ResAry.size())
						throw new Exception("ary error..");

					for(int p=0;p<r_s.size();p++){
						double pre = sche2_s_ResAry.get(p);
						sche2_s_ResAry.set(p, pre + r_s.get(p));					
					}

					for(int p=0; p<r_u.size();p++){
						double pre = sche2_u_ResAry.get(p);
						sche2_u_ResAry.set(p, pre+ r_u.get(p));
					}
				}								
			}
			

			if(up_slideNum>0){
				for(int p=0; p<sche2_s_ResAry.size();p++){
					double pre = sche2_s_ResAry.get(p);
					sche2_s_ResAry.set(p, pre*1.0/up_slideNum);
					bw_teen_uplift.write(df.format(pre*1.0/up_slideNum)+" ");//!!!!!!!!!!!!!!!!!!!!
					if(p==4 && pre*1.0/up_slideNum>10){
						System.out.println(pre*1.0/up_slideNum);
					}
				}

				for(int p=0; p<sche2_u_ResAry.size();p++){
					double pre = sche2_u_ResAry.get(p);
					sche2_u_ResAry.set(p, pre*1.0/up_slideNum);
					bw_teen_uplift.write(df.format(pre*1.0/up_slideNum)+" ");
				}
				bw_teen_uplift.write("\r\n");
			}else{
				String s = "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
				//bw_teen_uplift.write(cur_user+" "+s+"\r\n");
				bw_teen_uplift.write(s+"\r\n");
			}
		}
		userSet.close();
		
		bw_teen_uplift.close();
		bw_teen_stress.close();
	}
	
	//Tool: calculate measures in each slide
	static ArrayList<Double> calcuSlide(int sDate, int eDate, ArrayList<Integer> valueAry, 
			ArrayList<ArrayList<Integer> >cateAry, int L) throws Exception{						  
			
			ArrayList<Double> res = new ArrayList();			
			if(sDate>=L || eDate>=L || sDate>= valueAry.size() || sDate >= cateAry.size()){
				throw new Exception("schedule list error..");
			}
			
			double KIND  = cateAry.get(sDate).size();
			while(sDate<=eDate && valueAry.get(sDate)==0){
				sDate++;
			}
			while(eDate>=sDate && valueAry.get(eDate)==0 ){
				eDate--;
			}
			if(eDate<sDate){
				int tmp = (int) (5 + KIND*2);
				for(int i=0; i<tmp; i++){
					res.add(0.0);
				}
				return res;
			}
			
			double accStr = 0, avgStr = 0, RMS =0, max=0;
			double length = eDate-sDate+1;

			ArrayList <Double> accCateAry = new ArrayList();
			ArrayList <Double> ratioCateAry = new ArrayList();			
			double Cate_NUM = 0;						
			
			for(int j=sDate; j<=eDate && j<valueAry.size() && j<cateAry.size(); j++){
				accStr += valueAry.get(j);
				if(valueAry.get(j)>max)
					max = valueAry.get(j);

				for(int k=0; k<KIND; k++){
					int curCate = cateAry.get(j).get(k);
					if(j == sDate)
					{
						accCateAry.add(curCate*1.0);			
					}else{
						double pre = accCateAry.get(k);
						accCateAry.set(k, pre+curCate);						
					}
				}								
			}
			
			if(length>0){
				avgStr = accStr*1.0/length;
			}else{
				System.out.println(sDate+" "+eDate);
				throw new Exception("slide length error..");
			}
			for(int j=sDate; j<=eDate && j<valueAry.size(); j++){
				RMS += Math.pow((valueAry.get(j)-avgStr),2);
			}
			RMS = RMS*1.0/length;

			for(int k=0; k<KIND; k++){
				Cate_NUM += accCateAry.get(k);				
			}
			
			for(int k=0; k<KIND; k++){
				double rate = 0;
				if(Cate_NUM>0){
					rate = accCateAry.get(k)*1.0/Cate_NUM;
				}
				ratioCateAry.add(rate);				
			}

			res.add(accStr);//0
			res.add(avgStr);//1
			res.add(RMS);//2
			res.add(length);//3
			res.add(max);//4
			for(int i=0; i<KIND; i++)
				res.add(accCateAry.get(i));
			for(int i=0; i<KIND; i++)
				res.add(ratioCateAry.get(i));
			
			return res;
		}		
	

	//Tool: string to date
	static java.sql.Date string2Date(String dateString) throws ParseException{
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.ENGLISH);
		dateFormat.setLenient(false);
		java.util.Date timeDate = dateFormat.parse(dateString);
		java.sql.Date dateTime = new java.sql.Date(timeDate.getTime());
		return dateTime;
	}

	public static int daysBetween(String s1, String s2) throws ParseException    
	 {    
	   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
	  // smdate = (Date)sdf.parse(sdf.format(smdate));  
	  // bdate=(Date)sdf.parse(sdf.format(bdate));  
       Date smdate = string2Date(s1);//small
       Date bdate = string2Date(s2);//big
	   
	   Calendar cal = Calendar.getInstance();    
	   
	   cal.setTime(smdate);//first    
	   long time1 = cal.getTimeInMillis();                 
	   cal.setTime(bdate);    
	   long time2 = cal.getTimeInMillis();         
	   long between_days=(time2-time1)/(1000*3600*24);  
	            
       return Integer.parseInt(String.valueOf(between_days));           
	} 

		
	public static void main(String[] args) throws Exception{
		//compareStress();
		String year[] = {"2012","2013","2014","2015"};
		String names[] = {"ALL", "Holiday", "Party", "Sport", "Activity"};
		for(int i=0; i<year.length;i++)
		{
			for(int j=0; j<names.length; j++)
			{
				scheduleUplift(year[i],names[j]);
				System.out.println(year[i]+"|"+names[j]);
			}
		}		
	}
}
