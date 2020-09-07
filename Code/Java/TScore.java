package uplifts;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class TScore {

	//Tool: distance between days
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
	
	//Tool: string to date
	static java.sql.Date string2Date(String dateString) throws ParseException{
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.ENGLISH);
		dateFormat.setLenient(false);
		java.util.Date timeDate = dateFormat.parse(dateString);
		java.sql.Date dateTime = new java.sql.Date(timeDate.getTime());
		return dateTime;
	}
	
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
	
	//summary: 为每一个teen，调用ttestSequence
	static void ttestAllTeen(int teenNum, int K) throws IOException{	
		//initial threshold
		ArrayList<ArrayList<Double> > threAry = new ArrayList<ArrayList<Double> >();
		for(int k=0; k<teenNum; k++){
			ArrayList<Double> tmpAry = new ArrayList<Double>();
			for(int i=0; i<8; i++)
				tmpAry.add(0.0);
			threAry.add(tmpAry);
		}
				
		String typeID = "0";
		ArrayList<ArrayList<Double> > newAry = ttestSequence(threAry,0,typeID, K);//true threshold
		
		for(int i=0; i<5; i++){
			typeID = i+"";
			ttestSequence(newAry,1,typeID, K);//K useless now ...
		}	
	}

	//FUNCTION: 1)find SI/USI 2) KNN
	static ArrayList<ArrayList<Double> > ttestSequence ( 
		   ArrayList<ArrayList<Double> > threshAry, int flag, String typeID, int K) throws IOException{		
		
		ArrayList<ArrayList<Double> > res = new ArrayList<ArrayList<Double>>();	
			
		//---------------------OUT2: all pair slides-----------------------------------------------------
		//5-2-0 all pairs
		String allPair = "E:\\TEST\\POSITIVE\\Pair\\ttest\\T"+typeID+"\\allTTest.txt";	
		File allPairFile = new File(allPair);
		if(!allPairFile.exists()){
			allPairFile.createNewFile();
		}	
		FileWriter all_pair_fw = new FileWriter(allPairFile.getAbsoluteFile());
		BufferedWriter bw_all_pair = new BufferedWriter(all_pair_fw);			
			
		//5-2-1 all pairs big  ---------------
		String allPairBig = "E:\\TEST\\POSITIVE\\Pair\\ttest\\T"+typeID+"\\all_SI_TTest.txt";	
		File allPairFileBig = new File(allPairBig);
		if(!allPairFileBig.exists()){
			allPairFileBig.createNewFile();
		}	
		FileWriter all_big_fw_pair = new FileWriter(allPairFileBig.getAbsoluteFile());
		BufferedWriter bw_big_pair = new BufferedWriter(all_big_fw_pair);
		
		//5-2-2 all pairs small ---------------
		String allPairSmall = "E:\\TEST\\POSITIVE\\Pair\\ttest\\T"+typeID+"\\all_USI_TTest.txt";	
		File allPairFileSmall = new File(allPairSmall);
		if(!allPairFileSmall.exists()){
			allPairFileSmall.createNewFile();
		}	
		FileWriter all_small_fw_pair = new FileWriter(allPairFileSmall.getAbsoluteFile());
		BufferedWriter bw_small_pair = new BufferedWriter(all_small_fw_pair);
		
		//Add: [1] record correlation (stress)
		String allcor = "E:\\TEST\\POSITIVE\\Pair\\ttest\\corStress"+typeID+".txt";	
		File allcorFile = new File(allcor);
		if(!allcorFile.exists()){
			allcorFile.createNewFile();
		}	
		FileWriter allcorW = new FileWriter(allcorFile.getAbsoluteFile());
		BufferedWriter bw_cor_stress = new BufferedWriter(allcorW);
		
		//Add: [2] record correlation (stressor)
		String allcor2 = "E:\\TEST\\POSITIVE\\Pair\\ttest\\corStressor"+typeID+".txt";	
		File allcorFile2 = new File(allcor2);
		if(!allcorFile2.exists()){
			allcorFile2.createNewFile();
		}	
		FileWriter allcorW2 = new FileWriter(allcorFile2.getAbsoluteFile());
		BufferedWriter bw_cor_stressor = new BufferedWriter(allcorW2);
		
		//Add:!!! [3] record correlation (post)
		String allcor3 = "E:\\TEST\\POSITIVE\\Pair\\ttest\\corPost"+typeID+".txt";	
		File allcorFile3 = new File(allcor3);
		if(!allcorFile3.exists()){
			allcorFile3.createNewFile();
		}	
		FileWriter allcorW3 = new FileWriter(allcorFile3.getAbsoluteFile());
		BufferedWriter bw_cor_post = new BufferedWriter(allcorW3);
		
					
		//Add: record (average length) and (average stress) of stress intervals
		String allavg = "E:\\TEST\\POSITIVE\\Pair\\ttest\\avgUSI"+typeID+".txt";	
		File allavgFile = new File(allavg);
		if(!allavgFile.exists()){
			allavgFile.createNewFile();
		}	
		FileWriter allavgW = new FileWriter(allavgFile.getAbsoluteFile());
		BufferedWriter bw_avg = new BufferedWriter(allavgW);
				
		DecimalFormat df = new DecimalFormat("#####0.0000");
		String dirParseIn = "E:\\TEST\\POSITIVE\\smoothUp\\";//accumulated level per Day after regression
		File files2parse = new File(dirParseIn);
		if(files2parse.isDirectory()){
			
			String [] fileList = files2parse.list();			
			for(int pos_file=0; pos_file<fileList.length; pos_file++){

				ArrayList<Double> up_avgAllAry = new ArrayList<Double>();//up
				ArrayList<Double> up_accAllAry = new ArrayList<Double>();//up			
				ArrayList<Double> str_avgAllAry = new ArrayList<Double>();//str
				ArrayList<Double> str_accAllAry = new ArrayList<Double>();//str
				
				ArrayList<ArrayList<Double> > wordSmallAry = new ArrayList<ArrayList<Double> >();
				ArrayList<ArrayList<Double> > wordBigAry = new ArrayList<ArrayList<Double> >();
				ArrayList<ArrayList<Double> > stressSmallAry = new ArrayList<ArrayList<Double> >();
				ArrayList<ArrayList<Double> > stressBigAry = new ArrayList<ArrayList<Double> >();
				//add-post number
				ArrayList<ArrayList<Double> > postSmallAry = new ArrayList<ArrayList<Double> >();
				ArrayList<ArrayList<Double> > postBigAry = new ArrayList<ArrayList<Double> >();
				
				
				ArrayList<Double> threAry = threshAry.get(pos_file);
				
				String PPath = dirParseIn+fileList[pos_file];
				
				//1-input positive	- day		
				InputStreamReader isr = new InputStreamReader(new FileInputStream(PPath),getCharset(PPath));//smoothUp
				BufferedReader reader = new BufferedReader(isr);
				
				//1-2 input positive category - day
				String posiCatPath = PPath.replace("smoothUp", "upCateSta");
				InputStreamReader isrCateUp = new InputStreamReader(new FileInputStream(posiCatPath),getCharset(posiCatPath));
				BufferedReader up_cat_reader = new BufferedReader(isrCateUp);
				
				//2-input stress - day
				String SPath = PPath.replace("smoothUp", "smoothStr");
				InputStreamReader s_isr = new InputStreamReader(new FileInputStream(SPath),getCharset(SPath));
				BufferedReader s_reader = new BufferedReader(s_isr);
				
				//2-2 input stress category - day
				String strCatPath = PPath.replace("smoothUp", "CateSta");
				InputStreamReader isrCateStr = new InputStreamReader(new FileInputStream(strCatPath),getCharset(strCatPath));
				BufferedReader str_cat_reader = new BufferedReader(isrCateStr);

				//3-1 add:UpDaySta
				//3-2 add:DaySta
				String addPath1 = PPath.replace("smoothUp", "upDaySta");
				InputStreamReader addStream1 = new InputStreamReader(new FileInputStream(addPath1),getCharset(addPath1));
				BufferedReader addReader1 = new BufferedReader(addStream1);
				
				String addPath2 = PPath.replace("smoothUp", "DaySta");
				InputStreamReader addStream2 = new InputStreamReader(new FileInputStream(addPath2),getCharset(addPath2));
				BufferedReader addReader2 = new BufferedReader(addStream2);

				//--------------------------------OUT 3: all pairs/values--------------------------------------
				//3-out put file for pair interval
				String PCombinePath = PPath.replace("smoothUp", "Pair\\ttest\\T"+typeID+"\\valueBig");	
				File PFile = new File(PCombinePath);
				if(!PFile.exists()){
					PFile.createNewFile();
				}	
				FileWriter fw = new FileWriter(PFile.getAbsoluteFile());
				BufferedWriter bw_big = new BufferedWriter(fw);
				
				//4-output file for values in pair interval
				String SCombinePath = PPath.replace("smoothUp", "Pair\\ttest\\T"+typeID+"\\valueSmall");	
				File SFile = new File(SCombinePath);
				if(!SFile.exists()){
					SFile.createNewFile();
				}	
				FileWriter s_fw = new FileWriter(SFile.getAbsoluteFile());
				BufferedWriter bw_small = new BufferedWriter(s_fw);
				
				//----Stressor
				String LCombinePath = PPath.replace("smoothUp", "Pair\\ttest\\T"+typeID+"\\stressorValueBig");	
				File LFile = new File(LCombinePath);
				if(!LFile.exists()){
					LFile.createNewFile();
				}	
				FileWriter l_fw = new FileWriter(LFile.getAbsoluteFile());
				BufferedWriter stressor_bw_big = new BufferedWriter(l_fw);
				
				//4-output file for values in pair interval
				String LSCombinePath = PPath.replace("smoothUp", "Pair\\ttest\\T"+typeID+"\\stressorValueSmall");	
				File LSFile = new File(LSCombinePath);
				if(!LSFile.exists()){
					LSFile.createNewFile();
				}	
				FileWriter l_s_fw = new FileWriter(LSFile.getAbsoluteFile());
				BufferedWriter stressor_bw_small = new BufferedWriter(l_s_fw);
				//---------------------------------------------------------------------------------------------------------------

				String line = "", s_line="", up_cat_line="", str_cat_line="";
				String addDaySta = "", addUpDaySta = "";
				
				int tID = 0, upID=0, strID=0;
				double maxUp = 0, maxStr = 0, avgUp = 0, avgStr = 0, totalUp=0, totalStr=0, minUp = 0, minStr = 0;
				ArrayList<Double> upAry = new ArrayList<Double>();
				ArrayList<Double> strAry = new ArrayList<Double>();
				ArrayList<ArrayList<Integer>> upCateAry = new ArrayList<ArrayList<Integer>>();
				ArrayList<ArrayList<Integer>> strCateAry = new ArrayList<ArrayList<Integer>>();
				ArrayList<ArrayList<Integer>> postNumAry = new ArrayList<ArrayList<Integer>>();
				
				while((line = reader.readLine()) != null && (s_line = s_reader.readLine()) != null
						&& (up_cat_line = up_cat_reader.readLine())!= null 
						&&(str_cat_line = str_cat_reader.readLine()) != null
						&&(addUpDaySta = addReader1.readLine()) != null 
						&&(addDaySta = addReader2.readLine()) != null ){
					
					if(line.length()>0 && s_line.length()>0 && up_cat_line.length()>9 && str_cat_line.length()>0
					   && addUpDaySta.length() > 0 && addDaySta.length() > 0 ){
																		
						double p = Double.valueOf(line.toString()); //smoothed acc stress level
						double s = Double.valueOf(s_line.toString()); // smoothed acc up level
						
						if(p>0) p=0;
						if(s<0) s=0;
						
						if(tID == 0){
							maxUp = p*(-1);
							minUp = p*(-1);
							maxStr = s;
							minStr = s;
						}						
						if(p*(-1)>maxUp) maxUp = p*(-1);
						if(p*(-1)<minUp) minUp = p*(-1);						
						if(s>maxStr) maxStr = s;
						if(s<minStr) minStr = s;
						
						
						String[] up_elem = up_cat_line.split(" "); //!!!
						String[] str_elem = str_cat_line.split(" "); //!!!
						ArrayList<Integer> upTmpAry = new ArrayList<Integer>();
						ArrayList<Integer> strTmpAry = new ArrayList<Integer>();
						for(int k=0; k<up_elem.length && k<6; k++){
							upTmpAry.add(Integer.parseInt(up_elem[k].toString()));
						}					
						
						for(int k=0; k<str_elem.length && k<5; k++){
							strTmpAry.add(Integer.parseInt(str_elem[k].toString()));
						}		

						String[] up_post_elem = addUpDaySta.split(" ");
						String[] str_post_elem = addDaySta.split(" ");
						if(up_post_elem.length!=3 || str_post_elem.length!=3){
							System.out.println("UpDaySta or DaySta format is not 3...please check");
							System.exit(0);
						}
						
						ArrayList<Integer> oriPostAry = new ArrayList<Integer>();
						oriPostAry.add(Integer.parseInt(up_post_elem[2].toString()));//all post num  \1
						oriPostAry.add(Integer.parseInt(str_post_elem[1].toString()));//str post num \2
						oriPostAry.add(Integer.parseInt(up_post_elem[1].toString()));//posi post num \3
						
						//add-origin
						//for(int k=9; k<up_elem.length && k<12; k++){
						int httpPost = Integer.parseInt(up_elem[9].toString());
						int replyPost = Integer.parseInt(up_elem[10].toString());
						int comPost = Integer.parseInt(up_elem[11].toString());
						int allPostNum = Integer.parseInt(up_post_elem[2].toString());
						if(allPostNum >= httpPost){
							allPostNum = allPostNum - httpPost;
						}
						oriPostAry.add(allPostNum);
						//} add origin end
						
						upCateAry.add(upTmpAry);   //day
						strCateAry.add(strTmpAry); //day
						postNumAry.add(oriPostAry); //day
						upAry.add(p); //day
						strAry.add(s); //day
						totalUp += p;
						totalStr += s;
						tID++;						
						if(p!=0) upID++;
						if(s!=0) strID++;
					}
				}

				int ANUM = 0;
				int SMALL_NUM = 0;
				int BIG_NUM = 0;
					
				if(tID>10 && maxUp>=0 && maxStr>0){

					avgUp = totalUp/tID; //<0
					avgStr = totalStr/tID;	

					for(int i =0; i<strAry.size(); i++){

						while(i<strAry.size() && strAry.get(i)==0){
							i++;
						}						
						if(i==strAry.size()) break;
							
						double pre = 0;
						int a_pos = i, b_pos = i, c_pos = i;//a-find
						while(i<strAry.size() && strAry.get(i)>=pre && strAry.get(i)>0){
							pre = strAry.get(i);
							i++;
						}						
						if(i==strAry.size()) break;
						
						i--;
						if(i<strAry.size() && strAry.get(i)>=avgStr){
							b_pos = i;
							i++;
							while(i<strAry.size() && strAry.get(i)<=pre){
								pre = strAry.get(i);									
								i++;
								if(pre == 0) break;
							}

							if(i<strAry.size()){
								c_pos = i-1;
								double preMax = strAry.get(b_pos), preMin = strAry.get(c_pos);	

								while(i<strAry.size()){
									if(strAry.get(i)==0) break;
									
									double tmpH = 0, tmpL = 0;
									while(i<strAry.size() && strAry.get(i)>=pre){
										pre = strAry.get(i);
										i++;
										if(pre == 0) break;//
									}
									if(i<strAry.size()){
										i--;
										tmpH = strAry.get(i);
										if(strAry.get(i)< preMax){
											i++;
											while(i<strAry.size() && strAry.get(i)<=pre){
												pre = strAry.get(i);													
												i++;
												if(pre == 0) break;
											}
											i--;
											tmpL = strAry.get(i);
											if(tmpL<=preMin && tmpH<preMax){
												preMin = tmpL;
												preMax = tmpH;
												c_pos = i;
												i++;
											}else
												{
													i = c_pos+1;
													break;
												}
										}else
											{
												i = c_pos+1;//
												break;
											}
									}else
										break;
								}
																										
								//2-3-find corresponding positive values, and calculate
								if(!(a_pos>=0 && a_pos<strAry.size() && b_pos>=a_pos && b_pos < strAry.size()))
								{
									System.out.println("a-b-c error");
									break;
								}								

								double ACC_s = 0, ACC_u = 0;
								ArrayList<Integer> cur_upAry = new ArrayList<Integer>();
								ArrayList<Integer> cur_strAry = new ArrayList<Integer>();
								//add
								ArrayList<Integer> cur_postAry = new ArrayList<Integer>();
								double num_up=0, num_str=0;
								
								for(int k=0;k<6;k++){
									cur_upAry.add(0);
								}
								for(int k=0; k<5; k++){
									cur_strAry.add(0);
								}
								for(int k=0; k<4; k++){
									cur_postAry.add(0);
								}
								
								int LEN = c_pos-a_pos+1;
								for(int k=a_pos; k<=c_pos; k++){
									ACC_s += strAry.get(k);
									ACC_u += upAry.get(k);

									for(int p=0; p<6; p++){
										if(upCateAry.get(k).get(p)>0){
											Integer tmp = cur_upAry.get(p);
											tmp += upCateAry.get(k).get(p);
											cur_upAry.set(p,tmp);												
											num_up+=upCateAry.get(k).get(p);
										}
									}

									for(int p=0; p<5; p++){
										if(strCateAry.get(k).get(p)>0){ //..
											Integer tmp = cur_strAry.get(p);
											tmp += strCateAry.get(k).get(p);
											cur_strAry.set(p,tmp);
											num_str+=strCateAry.get(k).get(p);
										}
									}

									for(int p=0; p<4; p++){
										if(postNumAry.get(k).get(p)>0){
											Integer tmp = cur_postAry.get(p);//previous
											tmp += postNumAry.get(k).get(p);//current
											cur_postAry.set(p, tmp);//refresh
										}
									}									
								}

								if(LEN>0){//for sort (add current slide stress/up values(accumulated/average) into big array)
									up_avgAllAry.add(ACC_u*(-1.0)/LEN);
									up_accAllAry.add(ACC_u*(-1.0));
									str_avgAllAry.add((ACC_s)*1.0/LEN);
									str_accAllAry.add(ACC_s);
								}								
								
								//Now compare a-b,b-c,a-c Linguistic
								//1) up:distribution; rate; main type; distance
								//2) stress: distribution; rate; main type; distance
								ArrayList<Double> rateAry = new ArrayList<Double>();
																																				

								for(int k=0; k<5; k++){//frequency of each stress type- LEN1
									if(num_str>0)//total frequency
										rateAry.add(cur_strAry.get(k)*1.0/num_str);//ratio len1											
									else
										rateAry.add(0.0);
								}									

								int givenType = Integer.parseInt(typeID);
								int cur_k = givenType;
								int count = 0;
								for(int k=0; k<=4 && k<rateAry.size();k++){
									if(rateAry.get(k)>rateAry.get(cur_k))
										count++;
								}

								for(int k=0; k<6; k++){										
									if(num_up>0)
										rateAry.add(cur_upAry.get(k)*1.0/num_up);
									else
										rateAry.add(0.0);
								}

								if(flag == 1 && count <=2){ // ADD type constraint (DONE!)
									double low_avg_u = threAry.get(0);//input threshold upSmallAvg
									double high_avg_u = threAry.get(1);//input threshold upBigAvg
									double low_acc_u = threAry.get(2);//input threshold
									double high_acc_u = threAry.get(3);//input threshold
									
									if(LEN>0){
										
										double curAvg = ACC_u*1.0/LEN;
										double curAcc = ACC_u;
										
										if(curAvg*(-1)<=low_avg_u || curAcc*(-1)<=low_acc_u){

											String s = "";											
											ArrayList<Double> s_tmp_ary = new ArrayList<Double>();
											ArrayList<Double> w_tmp_ary = new ArrayList<Double>();
											ArrayList<Double> p_tmp_ary = new ArrayList<Double>();
											
											for(int p=a_pos;p<=c_pos;p++){
												s = s+ " "+strAry.get(p);  												
												s_tmp_ary.add(strAry.get(p));
											}

											int slideL = c_pos-a_pos+1;
											int maxL = strAry.size();
											int pre_begin = a_pos-slideL;
											int pre_end = a_pos-1;
											int post_begin = c_pos + 1;
											int post_end = c_pos + slideL;
											if(pre_begin>=0 && post_end<strAry.size()){
												String s_pre = "";
												String s_post = "";
												
												for(int p=pre_begin;p<=pre_end;p++){
													s_pre = s_pre+strAry.get(p)+" ";
												}
												for(int p=post_begin;p<=post_end;p++){
													s_post = s_post + strAry.get(p) + " ";
												}
												
												bw_small_pair.write(s_pre.trim()+" "+s.trim() + " " + s_post.trim() + "\r\n");
												bw_all_pair.write(s_pre.trim()+" "+s.trim() + " " + s_post.trim() + "\r\n");
											}										
											
											stressSmallAry.add(s_tmp_ary);//for KNN calculate //for the teen !!!!!
											bw_small.write(SMALL_NUM+s+"\r\n");																						

											String word = "";
											for(int p=0;p<5;p++){
												word = word + " " + cur_strAry.get(p);
												w_tmp_ary.add(cur_strAry.get(p)*1.0); //num
											}
											for(int p=0;p<5;p++){
												word = word + " " + rateAry.get(p); //rate
												w_tmp_ary.add(rateAry.get(p));
											}
											wordSmallAry.add(w_tmp_ary); //
											stressor_bw_small.write(SMALL_NUM+word+"\r\n");

											for(int p=0; p<4; p++){
												p_tmp_ary.add(cur_postAry.get(p)*1.0);//4 kinds of post
											}
											postSmallAry.add(p_tmp_ary);
											SMALL_NUM++;
										}else
											if(curAvg*(-1)>=high_avg_u || curAcc*(-1)>=high_acc_u){
												String s = "";
												String s2 = "";
												ArrayList<Double> s_tmp_ary = new ArrayList<Double>();
												ArrayList<Double> w_tmp_ary = new ArrayList<Double>();
												//add
												ArrayList<Double> p_tmp_ary = new ArrayList<Double>();												
												
												for(int p=a_pos;p<=c_pos;p++){
													s = s+ " "+strAry.get(p);
													s2 = s2 + strAry.get(p) + "\r\n";
													s_tmp_ary.add(strAry.get(p));
												}
												stressBigAry.add(s_tmp_ary);
												bw_big.write(s2);

												int slideL = c_pos-a_pos+1;
												int maxL = strAry.size();
												int pre_begin = a_pos-slideL;
												int pre_end = a_pos-1;
												int post_begin = c_pos + 1;
												int post_end = c_pos + slideL;
												if(pre_begin>=0 && post_end<strAry.size()){
													String s_pre = "";
													String s_post = "";
													
													for(int p=pre_begin;p<=pre_end;p++){
														s_pre = s_pre+strAry.get(p)+" ";
													}
													for(int p=post_begin;p<=post_end;p++){
														s_post = s_post + strAry.get(p) + " ";
													}
													
													bw_big_pair.write(s_pre.trim()+" "+s.trim() + " " + s_post.trim() + "\r\n");
													bw_all_pair.write(s_pre.trim()+" "+s.trim() + " " + s_post.trim() + "\r\n");
												}											

												String word = "";
												for(int p=0;p<5;p++){
													word = word + " " + cur_strAry.get(p); //num
													w_tmp_ary.add(cur_strAry.get(p)*1.0);
												}
												for(int p=0;p<5;p++){
													word = word + " " + rateAry.get(p); //rate
													w_tmp_ary.add(rateAry.get(p));
												}
												wordBigAry.add(w_tmp_ary); //
												stressor_bw_big.write(BIG_NUM+word+"\r\n");

												for(int p=0; p<4; p++){
													p_tmp_ary.add(cur_postAry.get(p)*1.0);//4 kinds of post
												}
												postBigAry.add(p_tmp_ary);												
												
												BIG_NUM++;
											}
										ANUM++;
									}										
								}																																			
							}else
								break;
						}					
					}
					
				}else{
					System.out.println("invalid file too few tweets..");
					reader.close();
					s_reader.close();
					
					up_cat_reader.close();
					str_cat_reader.close();
					addReader1.close();
					addReader2.close();
					
					bw_big.close();
					bw_small.close();
					continue;
				}
				
				reader.close();
				s_reader.close();
				
				up_cat_reader.close();
				str_cat_reader.close();
				addReader1.close();
				addReader2.close();
				
				bw_big.close();
				bw_small.close();
				stressor_bw_big.close();
				stressor_bw_small.close();

				Collections.sort(up_avgAllAry);
				Collections.sort(up_accAllAry);
				Collections.sort(str_avgAllAry);
				Collections.sort(str_accAllAry);

				int len = up_avgAllAry.size();
				ArrayList <Double>  res_cur = new ArrayList<Double>();
				if(len>0){
					res_cur.add(up_avgAllAry.get((int) (len*0.2)));
					res_cur.add(up_avgAllAry.get((int) (len*0.8)));
					res_cur.add(up_accAllAry.get((int) (len*0.2)));
					res_cur.add(up_accAllAry.get((int) (len*0.8)));
					res_cur.add(str_avgAllAry.get((int) (len*0.2)));
					res_cur.add(str_avgAllAry.get((int) (len*0.8)));
					res_cur.add(str_accAllAry.get((int) (len*0.2)));
					res_cur.add(str_accAllAry.get((int) (len*0.8)));
				}else{
					for(int t=0; t<8; t++)
						res_cur.add(0.0);
				}
				
				res.add(res_cur);

				System.out.println(fileList[pos_file]);
				if(flag == 1)//not for thresh, for real ...
				{
					double res1 = knnMethod(stressBigAry, stressSmallAry, K);   //stress intensity  -- ALERT: better change to 7 measures...
					double res2 = knnMethod(wordBigAry, wordSmallAry, K); 		//linguistic -- topic distribution and topic ratio
					double res3 = knnMethod(postBigAry, postSmallAry, K); 		//post behavior 
					
					bw_cor_stress.write(df.format(res1)+"\r\n");
					bw_cor_stressor.write(df.format(res2)+"\r\n");
					bw_cor_post.write(df.format(res3)+"\r\n");

					double L = 0; //length
					double S = 0; //
					for(int k=0; k<stressBigAry.size(); k++){ //number of intervals
						int n = stressBigAry.get(k).size(); // length of each interval
						L += n;
						for(int p=0; p<n; p++){  //Intensity of each interval
							S+=stressBigAry.get(k).get(p);
						}
					}
					
					if(stressBigAry.size()>0 && L>0)
					{
						S = S/L; //average intensity (day) in big intervals
						L = L/stressBigAry.size(); //average length of big intervals
					}

					int LtoI = (int) (L)+1;
					bw_avg.write(LtoI+" "+df.format(S)+"\r\n");													
				}			
			}

			bw_all_pair.close();//all pairs
			bw_small_pair.close();//all small pairs
			bw_big_pair.close();//all big pairs
			
			bw_cor_stressor.close();
			bw_cor_stress.close();
			bw_cor_post.close();
			
			bw_avg.close();		
			
			System.out.println("All compare done....");
		}
		return res;
	}	

	//Tool***: KNN method
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
				
				//find the nearest, record position
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
							//add 0 to the same length
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

	public static void main(String[] args) throws Exception {
		ttestAllTeen(121,2);
	}	
}
