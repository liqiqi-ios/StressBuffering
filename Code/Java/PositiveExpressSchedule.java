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

public class PositiveExpressSchedule {
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

    static ArrayList<ArrayList<Integer> > scheduleToAry(String yearNum, String name) throws UnsupportedEncodingException, FileNotFoundException, IOException, ParseException{
        //function-0: 读入事件列表
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
        res.add(big_upAry);//结束点
        res.add(small_strAry);//开始点
        res.add(big_strAry);//结束点
        return res;
    }

    static ArrayList<Integer> scheduleUplift(String yearNum, int K, String name) throws Exception{

        ArrayList<Integer> teenAry = new ArrayList();
        DecimalFormat df = new DecimalFormat("#####0.0000");

        String allTeenU = "C:\\TEST\\POSITIVE\\Schedule\\Result\\knnValue\\upFeaKNN_"+name+"_"+yearNum+".txt";
        File allTeenUFile = new File(allTeenU);
        if(!allTeenUFile.exists()){
            allTeenUFile.createNewFile();
        }
        FileWriter all_up_fw = new FileWriter(allTeenUFile.getAbsoluteFile());
        BufferedWriter bw_teen_uplift = new BufferedWriter(all_up_fw);

        String allTeenS = "C:\\TEST\\POSITIVE\\Schedule\\Result\\knnValue\\stressFeaKNN_" + name+ "_"+ yearNum+".txt";
        File allTeenSFile = new File(allTeenS);
        if(!allTeenSFile.exists()){
            allTeenSFile.createNewFile();
        }
        FileWriter all_str_fw = new FileWriter(allTeenSFile.getAbsoluteFile());
        BufferedWriter bw_teen_stress = new BufferedWriter(all_str_fw);

        ArrayList<Double> Ling_Knn1_Ary = new ArrayList();
        ArrayList<Double> Ling_Knn2_Ary = new ArrayList();
        ArrayList<Double> Ling_Knn3_Ary = new ArrayList();
        ArrayList<Double> Ling_Knn4_Ary = new ArrayList();
        ArrayList<Double> NLing_Knn_Ary = new ArrayList();

        //ArrayList<Double> UKnnAry = new ArrayList();
        int SI_NUM = 1;//所有人的
        int NL_USI_NUM = 1;//所有人的
        int L_USI1_NUM = 1;//所有人的
        int L_USI2_NUM = 1;//所有人的
        int L_USI3_NUM = 1;//所有人的
        int L_USI4_NUM = 1;//所有人的

        String allUSI1 = "C:\\TEST\\POSITIVE\\Schedule\\Result\\Express\\USI_Details\\USI1_121.txt";
        File allUSIUFile = new File(allUSI1);
        if(!allUSIUFile.exists()){
            allUSIUFile.createNewFile();
        }
        FileWriter all_usi1_fw = new FileWriter(allUSIUFile.getAbsoluteFile());
        BufferedWriter bw_USI1 = new BufferedWriter(all_usi1_fw);

        String allUSI2 = "C:\\TEST\\POSITIVE\\Schedule\\Result\\Express\\USI_Details\\USI2_121.txt";
        File allUSIUFile2 = new File(allUSI2);
        if(!allUSIUFile2.exists()){
            allUSIUFile2.createNewFile();
        }
        FileWriter all_usi2_fw = new FileWriter(allUSIUFile2.getAbsoluteFile());
        BufferedWriter bw_USI2 = new BufferedWriter(all_usi2_fw);

        String allUSI3 = "C:\\TEST\\POSITIVE\\Schedule\\Result\\Express\\USI_Details\\USI3_121.txt";
        File allUSIUFile3 = new File(allUSI3);
        if(!allUSIUFile3.exists()){
            allUSIUFile3.createNewFile();
        }
        FileWriter all_usi3_fw = new FileWriter(allUSIUFile3.getAbsoluteFile());
        BufferedWriter bw_USI3 = new BufferedWriter(all_usi3_fw);

        String allUSI4 = "C:\\TEST\\POSITIVE\\Schedule\\Result\\Express\\USI_Details\\USI4_121.txt";
        File allUSIUFile4 = new File(allUSI4);
        if(!allUSIUFile4.exists()){
            allUSIUFile4.createNewFile();
        }
        FileWriter all_usi4_fw = new FileWriter(allUSIUFile4.getAbsoluteFile());
        BufferedWriter bw_USI4 = new BufferedWriter(all_usi4_fw);

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
            //store up category and stress category
            ArrayList<ArrayList<Integer>> upCateAry = new ArrayList();//!!!
            ArrayList<ArrayList<Integer>> strCateAry = new ArrayList();//!!!
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
                int s_level = Integer.parseInt(s_elem[0]);

                String [] u_elem = uLevel.split(" ");
                int u_level = Integer.parseInt(u_elem[0]);

                //2-categories
                String [] s_cat_elem = scate.split(" ");
                String [] u_cat_elem = ucate.split(" ");

                //3-find the date
                if(s_cat_elem.length == 15){//namely, has post cur day
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
            }//current teen done
            sDaySta.close();
            sCateSta.close();

            ArrayList<ArrayList<Integer> > R= scheduleToAry(yearNum,name);//!!!!
            if(R.size()!=4){
                throw new Exception("schedule list error...");
            }
            ArrayList<Integer> small_upAry = R.get(0);
            ArrayList<Integer> big_upAry = R.get(1);
            ArrayList<Integer> small_strAry = R.get(2);
            ArrayList<Integer> big_strAry = R.get(3);

            ArrayList<ArrayList<Double> > scheSI_s_ResAry = new ArrayList();
            ArrayList<ArrayList<Double> > scheSI_u_ResAry = new ArrayList();
            int slideNum = 0;
            for(int k=0; k<small_strAry.size(); k++){
                int sDate = small_strAry.get(k);
                int eDate = big_strAry.get(k);

                ArrayList<Double> r_s = calcuSlide(sDate, eDate, stressDayAry, strCateAry, L);
                ArrayList<Double> r_u = calcuSlide(sDate, eDate, upDayAry, upCateAry, L);

                if(r_s.get(0)>0)
                {
                    slideNum++;
                    //stress
                    scheSI_s_ResAry.add(r_s);
                    //up
                    scheSI_u_ResAry.add(r_u);
                }
            }
            SI_NUM += slideNum;

            int up1_slideNum = 0;
            int up2_slideNum = 0;
            int up3_slideNum = 0;
            int up4_slideNum = 0;
            int NL_up_slideNum = 0;
            ArrayList<ArrayList<Double> > sche_L_USI1_s_ResAry = new ArrayList();
            ArrayList<ArrayList<Double> > sche_L_USI1_u_ResAry = new ArrayList();

            //后面的先定义stress measure相关的array去做knn
            ArrayList<ArrayList<Double> > sche_L_USI2_s_ResAry = new ArrayList();
            ArrayList<ArrayList<Double> > sche_L_USI3_s_ResAry = new ArrayList();
            ArrayList<ArrayList<Double> > sche_L_USI4_s_ResAry = new ArrayList();
            ArrayList<ArrayList<Double> > sche_NL_USI_s_ResAry = new ArrayList();

            for(int k=0; k<small_upAry.size(); k++){

                int sDate = small_upAry.get(k);
                int eDate = big_upAry.get(k);
                ArrayList<Double> r_s = calcuSlide(sDate, eDate, stressDayAry, strCateAry, L);//Up Slide - Stress
                ArrayList<Double> r_u = calcuSlide(sDate, eDate, upDayAry, upCateAry, L);//Up Slide - Up

                //if(r_s.get(0)>0) {
                    double ifEmo = r_u.get(0); //是否表达积极情感
                    double ifUplift = 0; //是否表达积极事件
                    for(int tmp_i = 5; tmp_i<=10; tmp_i++)
                        ifUplift = ifUplift + r_u.get(tmp_i);

                    if(ifEmo>0 && ifEmo>=ifUplift)
                    {
                        up1_slideNum = up1_slideNum +1;
                        //stress
                        sche_L_USI1_s_ResAry.add(r_s);
                        //up
                        sche_L_USI1_u_ResAry.add(r_u);
                        bw_USI1.write(cur_user + " " + String.valueOf(r_s) + " " + String.valueOf(r_u) + "\r\n");
                    }
                    if(ifUplift>0 && ifUplift>ifEmo){
                        up2_slideNum = up2_slideNum +1;
                        //stress
                        sche_L_USI2_s_ResAry.add(r_s);
                        bw_USI2.write(String.valueOf(r_s) + " " + String.valueOf(r_u) + "\r\n");
                    }
                    if(ifEmo>0 && ifUplift>0){
                        up3_slideNum = up3_slideNum +1;
                        //stress
                        sche_L_USI3_s_ResAry.add(r_s);
                        bw_USI3.write(String.valueOf(r_s) + " " + String.valueOf(r_u) + "\r\n");
                    }
                    if(ifEmo>0 || ifUplift>0){
                        up4_slideNum = up4_slideNum +1;
                        //stress
                        sche_L_USI4_s_ResAry.add(r_s);
                        bw_USI4.write(String.valueOf(r_s) + " " + String.valueOf(r_u) + "\r\n");
                    }
                    if(ifEmo==0 && ifUplift==0){
                        NL_up_slideNum = NL_up_slideNum+1;
                        //stress
                        sche_NL_USI_s_ResAry.add(r_s);
                    }
                //}
            }
            L_USI1_NUM += up1_slideNum;
            L_USI2_NUM += up2_slideNum;
            L_USI3_NUM += up3_slideNum;
            L_USI4_NUM += up4_slideNum;
            NL_USI_NUM += NL_up_slideNum;

//            Double r1 = knnMethod(scheSI_s_ResAry, sche_L_USI1_s_ResAry,K);//K is set to 2
//            Double r2 = knnMethod(scheSI_s_ResAry, sche_L_USI2_s_ResAry,K);//K is set to 2
//            Double r3 = knnMethod(scheSI_s_ResAry, sche_L_USI3_s_ResAry,K);//K is set to 2
//            Double r4 = knnMethod(scheSI_s_ResAry, sche_L_USI4_s_ResAry,K);//K is set to 2
//            Double r_nl = knnMethod(scheSI_s_ResAry, sche_NL_USI_s_ResAry,K);//K is set to 2

            ArrayList<ArrayList<Double>> FourMeasures_scheSI_s_ResAry = pickMeasures(scheSI_s_ResAry);
            ArrayList<ArrayList<Double>> FourMeasures_scheL_USI1_s_ResAry = pickMeasures(sche_L_USI1_s_ResAry);
            ArrayList<ArrayList<Double>> FourMeasures_scheL_USI2_s_ResAry = pickMeasures(sche_L_USI2_s_ResAry);
            ArrayList<ArrayList<Double>> FourMeasures_scheL_USI3_s_ResAry = pickMeasures(sche_L_USI3_s_ResAry);
            ArrayList<ArrayList<Double>> FourMeasures_scheL_USI4_s_ResAry = pickMeasures(sche_L_USI4_s_ResAry);
            ArrayList<ArrayList<Double>> FourMeasures_scheL_NL_USI_s_ResAry = pickMeasures(sche_NL_USI_s_ResAry);
            Double r1 = knnMethod(FourMeasures_scheSI_s_ResAry, FourMeasures_scheL_USI1_s_ResAry,K);//K is set to 2
            Double r2 = knnMethod(FourMeasures_scheSI_s_ResAry, FourMeasures_scheL_USI2_s_ResAry,K);//K is set to 2
            Double r3 = knnMethod(FourMeasures_scheSI_s_ResAry, FourMeasures_scheL_USI3_s_ResAry,K);//K is set to 2
            Double r4 = knnMethod(FourMeasures_scheSI_s_ResAry, FourMeasures_scheL_USI4_s_ResAry,K);//K is set to 2
            Double r_nl = knnMethod(FourMeasures_scheSI_s_ResAry, FourMeasures_scheL_NL_USI_s_ResAry,K);//K is set to 2

            Ling_Knn1_Ary.add(r1);
            Ling_Knn2_Ary.add(r2);
            Ling_Knn3_Ary.add(r3);
            Ling_Knn4_Ary.add(r4);
            NLing_Knn_Ary.add(r_nl);
            //UKnnAry.add(r2);

            bw_teen_stress.write(r1+"\r\n");
            //bw_teen_uplift.write(r2+"\r\n");
        }//current teen done
        bw_USI1.close();
        bw_USI2.close();
        bw_USI3.close();
        bw_USI4.close();

        //int AllTeen = Ling_Knn1_Ary.size();
        int AllTeen = 1;
        for(int tmp_i=0; tmp_i<Ling_Knn1_Ary.size(); tmp_i++){
//            if(Math.abs(Ling_Knn1_Ary.get(tmp_i))>0 || Math.abs(Ling_Knn2_Ary.get(tmp_i))>0 ||
//                    Math.abs(Ling_Knn3_Ary.get(tmp_i))>0 || Math.abs(Ling_Knn4_Ary.get(tmp_i))>0 ||
//                    Math.abs(NLing_Knn_Ary.get(tmp_i))>0)
                if(Ling_Knn1_Ary.get(tmp_i)>0 || Ling_Knn2_Ary.get(tmp_i)>0 ||
                        Ling_Knn3_Ary.get(tmp_i)>0 || Ling_Knn4_Ary.get(tmp_i)>0 ||
                        NLing_Knn_Ary.get(tmp_i)>0)
                AllTeen++;
        }
        int L_USI1_cor = 0;
        int L_USI2_cor = 0;
        int L_USI3_cor = 0;
        int L_USI4_cor = 0;
        int NL_USI_cor = 0;
//        int SKnnTeen = 0;
//        int UKnnTeen = 0;
//        int SUKnnTeen = 0;
//        int SUKnnTeenBoth = 0;

        for(int i=0; i<Ling_Knn1_Ary.size(); i++){
            if(Ling_Knn1_Ary.get(i)>1.96){
                //SKnnTeen++;
                L_USI1_cor = L_USI1_cor+1;
            }
            if(Ling_Knn2_Ary.get(i)>1.96){
                //UKnnTeen++;
                L_USI2_cor = L_USI2_cor+1;
            }
            if(Ling_Knn3_Ary.get(i)>1.96){
                //SUKnnTeen++;
                L_USI3_cor = L_USI3_cor+1;
            }
            if(Ling_Knn4_Ary.get(i)>1.96){
                //SUKnnTeenBoth++;
                L_USI4_cor = L_USI4_cor+1;
            }
            if(NLing_Knn_Ary.get(i)>1.96){
                //SUKnnTeenBoth++;
                NL_USI_cor = NL_USI_cor+1;
            }
        }

        teenAry.add(AllTeen);
        teenAry.add(L_USI1_cor);
        teenAry.add(L_USI2_cor);
        teenAry.add(L_USI3_cor);
        teenAry.add(L_USI4_cor);
        teenAry.add(NL_USI_cor);

        teenAry.add(SI_NUM);//当年的所有SI的数量
        teenAry.add(L_USI1_NUM);
        teenAry.add(L_USI2_NUM);
        teenAry.add(L_USI3_NUM);
        teenAry.add(L_USI4_NUM);
        teenAry.add(NL_USI_NUM);

        userSet.close();
        bw_teen_uplift.close();
        bw_teen_stress.close();

        return teenAry;
    }

    static ArrayList<ArrayList<Double>> pickMeasures(ArrayList<ArrayList<Double>> scheSI_s_ResAry) throws Exception {
        ArrayList<ArrayList<Double>> new_scheSI_s_ResAry = new ArrayList();
        for (int tmp_i = 0; tmp_i < scheSI_s_ResAry.size(); tmp_i++) {
            ArrayList<Double> new_r = new ArrayList();
            new_r.add(scheSI_s_ResAry.get(tmp_i).get(0));
            new_r.add(scheSI_s_ResAry.get(tmp_i).get(1));
            new_r.add(scheSI_s_ResAry.get(tmp_i).get(2));
            new_r.add(scheSI_s_ResAry.get(tmp_i).get(4));
            new_scheSI_s_ResAry.add(new_r);
        }
        return new_scheSI_s_ResAry;
    }

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

                //calculate distance
                ArrayList<Double> tmpAry = allAry.get(j);
                int L2 = tmpAry.size();

                //add 0 to the same length
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

                    //calculate
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

                    //calculate
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

        double KIND  = cateAry.get(sDate).size();//kind is 5/6
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

        res.add(accStr);
        res.add(avgStr);
        res.add(RMS);
        res.add(length);
        res.add(max);
        for(int i=0; i<KIND; i++)
            res.add(accCateAry.get(i));
        for(int i=0; i<KIND; i++)
            res.add(ratioCateAry.get(i));

        return res;
    }

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

    public static void main(String[] args) throws Exception {
        int K = 4;
        String NAME[] = {"Holiday","Activity","Party","Sport","ALL"};
        for(int name_i=0; name_i<NAME.length; name_i++){
            String name1 = NAME[name_i];
            String year[] = {"2012","2013","2014","2015"};

            String allTeenKNN = "C:\\TEST\\POSITIVE\\Schedule\\Result\\Express\\L_USI_knn_"+name1+"_K"+K+".txt";
            File allTeenKNNFile = new File(allTeenKNN);
            if(!allTeenKNNFile.exists()){
                allTeenKNNFile.createNewFile();
            }
            FileWriter all_knn_fw = new FileWriter(allTeenKNNFile.getAbsoluteFile());
            BufferedWriter bw_all_knn = new BufferedWriter(all_knn_fw);//note:这是个全局变量，存每个user

            ArrayList<Integer> resTeen = new ArrayList();
            for(int i=0; i<6; i++)
                resTeen.add(0);
            //	teenAry.add(AllTeen);
//			teenAry.add(SKnnTeen);
//			teenAry.add(UKnnTeen);
//			teenAry.add(SUKnnTeen);
//			teenAry.add(SUKnnTeenBoth);
            int SI = 0;
            int USI_exp1 = 0;
            int USI_exp2 = 0;
            int USI_exp3 = 0;
            int USI_exp4 = 0;
            int USI_NL = 0;
//			teenAry.add(SI_NUM);
//			teenAry.add(USI_NUM);

            for(int i=0; i<year.length;i++)//将各个年份的结果加起来
            {
                ArrayList<Integer> r = scheduleUplift(year[i],K,name1);
                for(int j=0; j<resTeen.size(); j++){
                    int pre = resTeen.get(j);
                    resTeen.set(j, pre+r.get(j));
                }
                SI += r.get(6);
                USI_exp1 += r.get(7);
                USI_exp2 += r.get(8);
                USI_exp3 += r.get(9);
                USI_exp4 += r.get(10);
                USI_NL += r.get(11);
                System.out.println("this year done..");
            }

            bw_all_knn.write("Teen number: " + resTeen.get(0) + "\r\n"
                    +"L_USI1_cor_ratio: " + resTeen.get(1)*1.0/resTeen.get(0)+"\r\n"
                    +"L_USI2_cor_ratio: " + resTeen.get(2)*1.0/resTeen.get(0)+"\r\n"
                    +"L_USI3_cor_ratio: " + resTeen.get(3)*1.0/resTeen.get(0)+"\r\n"
                    +"L_USI4_cor_ratio: " + resTeen.get(4)*1.0/resTeen.get(0)+"\r\n"
                    +"NL_USI_cor_ratio: " + resTeen.get(5)*1.0/resTeen.get(0)+"\r\n"
                    +"SI_NUM: " + SI + "\r\n"
                    +"L_USI1_NUM: " + USI_exp1 + "\r\n"
                    +"L_USI2_NUM: " + USI_exp2 + "\r\n"
                    +"L_USI3_NUM: " + USI_exp3 + "\r\n"
                    +"L_USI4_NUM: " + USI_exp4 + "\r\n"
                    +"NL_USI_NUM: " + USI_NL + "\r\n"
            );
            bw_all_knn.close();
        }
    }
}
