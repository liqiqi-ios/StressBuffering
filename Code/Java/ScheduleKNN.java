//518 SI vs. 259 USI
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleKNN {

	//Tool: getCharset
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
	
	//Tool: string to date
	static java.sql.Date string2Date(String dateString) throws ParseException{
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.ENGLISH);
		dateFormat.setLenient(false);
		java.util.Date timeDate = dateFormat.parse(dateString);
		java.sql.Date dateTime = new java.sql.Date(timeDate.getTime());
		return dateTime;
	}
				
	//Tool: specific list to arrays
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
		
		u_br.close();
		s_br.close();
		
		res.add(small_upAry);//开始点
		res.add(big_upAry);
		res.add(small_strAry);
		res.add(big_strAry);//结束点
		return res;
	}

	//Function: find scheduled uplift/stress measures
	static ArrayList<Integer> scheduleUplift(String yearNum, int K, String name) throws Exception{
		
		ArrayList<Integer> teenAry = new ArrayList();
		DecimalFormat df = new DecimalFormat("#####0.0000");

		//out1
		String allTeenU = "C:\\TEST\\POSITIVE\\Schedule\\Result\\knnValue\\upFeaKNN_"+name+"_"+yearNum+".txt";
		File allTeenUFile = new File(allTeenU);
		if(!allTeenUFile.exists()){
			allTeenUFile.createNewFile();
		}	
		FileWriter all_up_fw = new FileWriter(allTeenUFile.getAbsoluteFile());
		BufferedWriter bw_teen_uplift = new BufferedWriter(all_up_fw);
		//out2
		String allTeenS = "C:\\TEST\\POSITIVE\\Schedule\\Result\\knnValue\\stressFeaKNN_" + name+ "_"+ yearNum+".txt";
		File allTeenSFile = new File(allTeenS);
		if(!allTeenSFile.exists()){
			allTeenSFile.createNewFile();
		}	
		FileWriter all_str_fw = new FileWriter(allTeenSFile.getAbsoluteFile());
		BufferedWriter bw_teen_stress = new BufferedWriter(all_str_fw);		

		ArrayList<Double> SKnnAry = new ArrayList();
		ArrayList<Double> UKnnAry = new ArrayList();
		int SI_NUM = 0;//所有人
		int USI_NUM = 0;//所有人
		
		//function1: read in each user
		String listPath = "C:\\TEST\\POSITIVE\\Schedule\\userList_"+yearNum+".txt";
		InputStreamReader user_reader = new InputStreamReader(new FileInputStream(listPath),getCharset(listPath));
		BufferedReader userSet = new BufferedReader(user_reader);
		String cur_user;
		while((cur_user=userSet.readLine())!=null){							
			String s_cate_userPath = "C:\\TEST\\POSITIVE\\CateSta\\"+cur_user;
			String s_userPath = "C:\\TEST\\POSITIVE\\DaySta\\"+cur_user;
			String u_cate_userPath = "C:\\TEST\\POSITIVE\\upCateSta\\"+cur_user;
			String u_userPath = "C:\\TEST\\POSITIVE\\upDaySta\\"+cur_user;
			
			//[1] Stress: DaySta, CateSta 
			//stress - day statistic (accumulated, stress tweet, total tweet)
			InputStreamReader s_cate_reader = new InputStreamReader(new FileInputStream(s_cate_userPath),getCharset(s_cate_userPath));
			BufferedReader sCateSta = new BufferedReader(s_cate_reader);			
			//stress category
			InputStreamReader s_reader = new InputStreamReader(new FileInputStream(s_userPath),getCharset(s_userPath));
			BufferedReader sDaySta = new BufferedReader(s_reader);
			
			//[2] Up: upDaySta, upCateSta
			InputStreamReader u_cate_reader = new InputStreamReader(new FileInputStream(u_cate_userPath),getCharset(u_cate_userPath));
			BufferedReader uCateSta = new BufferedReader(u_cate_reader);			
			//up category
			InputStreamReader u_reader = new InputStreamReader(new FileInputStream(u_userPath),getCharset(u_userPath));
			BufferedReader uDaySta = new BufferedReader(u_reader);

			ArrayList<Integer> upDayAry = new ArrayList();
			ArrayList<Integer> stressDayAry = new ArrayList();
			ArrayList<ArrayList<Integer>> upCateAry = new ArrayList();
			ArrayList<ArrayList<Integer>> strCateAry = new ArrayList();
			int L = daysBetween("1990/1/1", "2018/1/1"); //+2
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

				String [] s_elem = sLevel.split(" ");
				int s_level = Integer.parseInt(s_elem[0]);//accumulated stress
				String [] u_elem = uLevel.split(" ");
				int u_level = Integer.parseInt(u_elem[0]);//accumulated positive
				

				String [] s_cat_elem = scate.split(" "); //5+3+3+year+month+day
				String [] u_cat_elem = ucate.split(" ");

				if(s_cat_elem.length == 15){
					String y = s_cat_elem[11];
					String m = s_cat_elem[12];
					String d = s_cat_elem[13];
					y = y.replace(",", "");
					String newDay = y+"/"+m+"/"+d;
					int cur_point = daysBetween("1990/1/1", newDay);
					if(cur_point < L){
						upDayAry.set(cur_point, u_level);
						stressDayAry.set(cur_point, s_level);						
						for(int k=0; k<6; k++){
							int typeNum = Integer.parseInt(u_cat_elem[k]);//only store the categories..
							upCateAry.get(cur_point).set(k,typeNum);
						}
						for(int k=0; k<5; k++){
							int typeNum = Integer.parseInt(s_cat_elem[k]);//only store the categories..
							strCateAry.get(cur_point).set(k,typeNum);							
						}						
					}else{
						throw new Exception("error length..");
					}
				}				
			}
			sDaySta.close();
			sCateSta.close();

			ArrayList<ArrayList<Integer> > R= scheduleToAry(yearNum,name);
			//既返回了SI集合，又返回了USI集合
			if(R.size()!=4){
				throw new Exception("schedule list error...");
			}
			ArrayList<Integer> small_upAry = R.get(0);
			ArrayList<Integer> big_upAry = R.get(1);
			ArrayList<Integer> small_strAry = R.get(2);
			ArrayList<Integer> big_strAry = R.get(3);

			//1 Stress Slides
			ArrayList<ArrayList<Double> > sche1_s_ResAry = new ArrayList();
			ArrayList<ArrayList<Double> > sche1_u_ResAry = new ArrayList();						
			//1-1 stress slides of current teen
			int slideNum = 0;
			for(int k=0; k<small_strAry.size(); k++){//for each scheduled SI slide
				int sDate = small_strAry.get(k);
				int eDate = big_strAry.get(k);

				ArrayList<Double> r_s = calcuSlide(sDate, eDate, stressDayAry, strCateAry, L);//Stress Slide - Stress
				ArrayList<Double> r_u = calcuSlide(sDate, eDate, upDayAry, upCateAry, L);//Stress Slide - Up
				
				if(r_s.get(0)>0)
				{
					 slideNum++;
					sche1_s_ResAry.add(r_s);
					sche1_u_ResAry.add(r_u);
				}				
			}
			SI_NUM += slideNum;
			

			int up_slideNum = 0;
			ArrayList<ArrayList<Double> > sche2_s_ResAry = new ArrayList();
			ArrayList<ArrayList<Double> > sche2_u_ResAry = new ArrayList();
			for(int k=0; k<small_upAry.size(); k++){
				
				int sDate = small_upAry.get(k);
				int eDate = big_upAry.get(k);
				ArrayList<Double> r_s = calcuSlide(sDate, eDate, stressDayAry, strCateAry, L);//Up Slide - Stress
				ArrayList<Double> r_u = calcuSlide(sDate, eDate, upDayAry, upCateAry, L);//Up Slide - Up
				
				if(r_s.get(0)>0) //posi值>0; posi值 = positive_emotion * degree
				{
					up_slideNum++;
					sche2_s_ResAry.add(r_s);
					sche2_u_ResAry.add(r_u);
				}								
			}
			USI_NUM += up_slideNum;

			Double r1 = knnMethod(sche1_s_ResAry, sche2_s_ResAry,K);//K is set to 2
			Double r2 = knnMethod(sche1_u_ResAry, sche2_u_ResAry,K);

			SKnnAry.add(r1);
			UKnnAry.add(r2);
			
			bw_teen_stress.write(r1+"\r\n");
			bw_teen_uplift.write(r2+"\r\n");
		}

		int AllTeen = SKnnAry.size();
		int SKnnTeen = 0;
		int UKnnTeen = 0;
		int SUKnnTeen = 0;
		int SUKnnTeenBoth = 0;
		
		for(int i=0; i<SKnnAry.size(); i++){
			if(SKnnAry.get(i)>1.96){
				SKnnTeen++;
			}
			if(UKnnAry.get(i)>1.96){
				UKnnTeen++;
			}
			if((SKnnAry.get(i)+UKnnAry.get(i))*1.0/2>1.96){
				SUKnnTeen++;
			}
			if(SKnnAry.get(i)>1.96 || UKnnAry.get(i)>1.96){
				SUKnnTeenBoth++;
			}
		}
		
		teenAry.add(AllTeen);
		teenAry.add(SKnnTeen);
		teenAry.add(UKnnTeen);
		teenAry.add(SUKnnTeen);
		teenAry.add(SUKnnTeenBoth);
		teenAry.add(SI_NUM);
		teenAry.add(USI_NUM);
				
		userSet.close();		
		bw_teen_uplift.close();
		bw_teen_stress.close();
				
		return teenAry;
	}

	//Tool: KNN method
	static Double knnMethod(ArrayList<ArrayList<Double> >strBigAry, ArrayList<ArrayList<Double> >strSmallAry, int K){
		Double r = 0.0;
		
		int N1 = strBigAry.size(), N2 = strSmallAry.size();
		Map<Integer, Double> res = new HashMap<Integer, Double>();		 
		
		ArrayList<ArrayList<Double> > allAry = new ArrayList<ArrayList<Double> >();
		for(int i=0; i<N1; i++){
			allAry.add(strBigAry.get(i));
		}
		for(int i=0; i<N2; i++){
			allAry.add(strSmallAry.get(i));
		}
		
		int N0= allAry.size();
		int X = 0, Y = 0;
		
		for(int i=0; i<N0; i++){//
			
			ArrayList<Double> curAry = allAry.get(i);
			int L1 = curAry.size();

			for(int j=0; j<N0; j++){

				ArrayList<Double> tmpAry = allAry.get(j);
				int L2 = tmpAry.size();

				int sub = L1 - L2;
				if(sub>=0){						
					ArrayList<Double> newAry = new ArrayList<Double>();
					for(int p=0; p<sub*1.0/2; p++){
						newAry.add(0.0);
					}
					for(int q=0; q<L2; q++){
						newAry.add(tmpAry.get(q));//new test
					}
					for(int p=0; p<sub*1.0/2; p++)
						newAry.add(0.0);

					double dis = 0;
					for(int p=0; p<L1 && p<curAry.size() && p<newAry.size(); p++){
						dis = dis + Math.pow((curAry.get(p)-newAry.get(p)), 2);//curAry is larger
					}
					res.put(j, dis);
				}else
					if(sub<0){
						ArrayList<Double> newAry = new ArrayList<Double>();
						for(int p=0; p<sub*1.0/2; p++){
							newAry.add(0.0);
						}
						for(int q=0; q<L1; q++){
							newAry.add(curAry.get(q));//new test
						}
						for(int p=0; p<sub*1.0/2; p++)
							newAry.add(0.0);

						double dis = 0;
						for(int p=0; p<L1 && p<tmpAry.size() && p<newAry.size(); p++){
							dis = dis + Math.pow((tmpAry.get(p)-newAry.get(p)), 2);//tmpAry is larger
						}
						res.put(j, dis);
					}
			}
	        
	        List<Map.Entry<Integer,Double>> list = new ArrayList<Map.Entry<Integer,Double>>(res.entrySet());
	        Collections.sort(list,new Comparator<Map.Entry<Integer,Double>>() {
				@Override
				public int compare(java.util.Map.Entry<Integer,Double> o1,
						java.util.Map.Entry<Integer,Double> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}	            
	        });
	        
	        int pos = 0;	        
	        for(Map.Entry<Integer,Double> mapping:list){ 	              
	        	if(pos<K){
	        		if(i>=0 && i<N1){ //Set A
	        			if(mapping.getKey()>=0 && mapping.getKey()<N1)
	        				X++;
	        			else
	        				Y++;
		        	}else
		        		if(i>=N1 && i<(N1+N2)){
		        			if(mapping.getKey()>=N1 && mapping.getKey()<(N1+N2)){//Set B
			        			X++;
			        		}else
			        			Y++;
		        		}else{
		        			System.out.println(i+" error...");
		        		}
	        		pos++;
	        	}else
	        		break;
	        } 		
		}

		if(N1>0 && N2>0){
			double b = X*1.0/(N1*N2);
			double lam1 = N1*1.0/N0;
			double lam2 = N2*1.0/N0;
			double u = Math.pow(lam1,2)+Math.pow(lam2,2);
			double div = lam1*lam2 + 4*lam1*lam1*lam2*lam2;
			if(div > 0){
				r = Math.sqrt(N1*N2)*(b-u)/div;
			}else
				r = 0.0;
		}else
		{
			System.out.println(N1+"|"+N2);
		}		
		return r;
	}
	

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
	 
	public static void main(String[] args) throws Exception {
		int K = 2;
		String name1 = "ALL";
		//String name1 = "Holiday";
		//String name1 = "Party";
		//String name1 = "Sport";
		//String name1 = "Activity";
		String year[] = {"2012","2013","2014","2015"};

		String allTeenKNN = "C:\\TEST\\POSITIVE\\Schedule\\Result\\scheduleKNN_"+name1+".txt";
		File allTeenKNNFile = new File(allTeenKNN);
		if(!allTeenKNNFile.exists()){
			allTeenKNNFile.createNewFile();
		}	
		FileWriter all_knn_fw = new FileWriter(allTeenKNNFile.getAbsoluteFile());
		BufferedWriter bw_all_knn = new BufferedWriter(all_knn_fw);

		ArrayList<Integer> resTeen = new ArrayList();
		for(int i=0; i<5; i++)
			resTeen.add(0);
		//	teenAry.add(AllTeen);
//			teenAry.add(SKnnTeen);
//			teenAry.add(UKnnTeen);
//			teenAry.add(SUKnnTeen);
//			teenAry.add(SUKnnTeenBoth);
		int SI = 0;
		int USI = 0;
		//	SI and U-SI num
//			teenAry.add(SI_NUM);
//			teenAry.add(USI_NUM);

		for(int i=0; i<year.length;i++)
		{
			ArrayList<Integer> r = scheduleUplift(year[i],K,name1); 
			for(int j=0; j<resTeen.size(); j++){
				int pre = resTeen.get(j);
				resTeen.set(j, pre+r.get(j));
			}
			SI += r.get(5);
			USI += r.get(6);
			
			System.out.println("this year done..");			
		}
		
		bw_all_knn.write(resTeen.get(0)+"\r\n"
				+resTeen.get(1)*1.0/resTeen.get(0)+"\r\n"
				+resTeen.get(2)*1.0/resTeen.get(0)+"\r\n"
				+resTeen.get(3)*1.0/resTeen.get(0)+"\r\n"
				+resTeen.get(4)*1.0/resTeen.get(0)+"\r\n"
				+SI+"\r\n"
				+USI+"\r\n");
		bw_all_knn.close();
	}	
}
