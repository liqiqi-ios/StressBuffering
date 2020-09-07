all_path = 'E:\TEST\POSITIVE\Pair\correlation\adjust\8patternAvg.txt';
foutAllAvg = fopen(all_path,'w');
    
for TOPIC = 0:1:4
    filelist = dir(['E:\TEST\POSITIVE\Pair\correlation\T',num2str(TOPIC),'\normal\*.txt']);
    
    path_avg = ['E:\TEST\POSITIVE\Pair\correlation\','avgUSI',num2str(TOPIC),'.txt'];
    data_avg = importdata(path_avg);
    
    path_str = ['E:\TEST\POSITIVE\Pair\correlation\','corStress',num2str(TOPIC),'.txt'];
    data_cor_stress = importdata(path_str);
    
    path_stressor = ['E:\TEST\POSITIVE\Pair\correlation\','corStressor',num2str(TOPIC),'.txt'];
    data_cor_stressor = importdata(path_stressor);
    
    path_post = ['E:\TEST\POSITIVE\Pair\correlation\','corPost',num2str(TOPIC),'.txt'];
    data_cor_post = importdata(path_post);
      
    m_path = ['E:\TEST\POSITIVE\Pair\correlation\adjust\','metricT',num2str(TOPIC),'.txt'];
    foutMetric = fopen(m_path,'w');
    
    lineNum = 101;
    colNum = 4;
    I1 = zeros(lineNum,colNum);
    I2 = zeros(lineNum,colNum);
    I3 = zeros(lineNum,colNum);
    I4 = zeros(lineNum,colNum);
    I5 = zeros(lineNum,colNum);
    I6 = zeros(lineNum,colNum);
    I7 = zeros(lineNum,colNum);
    I8 = zeros(lineNum,colNum);
    USER_ALL = 0;
    
    for pos = 1:1:length(filelist)
        disp(filelist(pos).name);

        path_predict = ['E:\TEST\POSITIVE\Pair\correlation\T',num2str(TOPIC),'\predict\',filelist(pos).name];
        path_normal = ['E:\TEST\POSITIVE\Pair\correlation\T',num2str(TOPIC),'\normal\',filelist(pos).name];
 
        lenHis = data_avg(pos,1);
        avgHis = data_avg(pos,2);
        corStressor = data_cor_stressor(pos,1);
        corStress = data_cor_stress(pos,1);
        corPost = data_cor_post(pos,1);
        
        disp('lenHis:');
        disp(lenHis);%
        disp('avgHis');
        disp(avgHis);%
        
        y = importdata(path_normal);
        y_s = importdata(path_predict);
        y_ori = y_s;
        [k1,k2] = size(y_ori);
        if(k1>0)
            USER_ALL = USER_ALL+1;
            
            label=zeros(1,8);
            if(corStressor<=1.96 && corStress<=1.96 && corPost <= 1.96)
                label(1,1)=1;
            end

            if(corStressor>1.96 && corStress<=1.96 && corPost <= 1.96)
                label(1,2)=1;
             end
             if(corStressor<=1.96 && corStress>1.96 && corPost <= 1.96)
                label(1,3)=1;
            end
            if(corStressor<=1.96 && corStress<=1.96 && corPost > 1.96)
                label(1,4)=1;
            end
 
            if(corStressor>1.96 && corStress>1.96 && corPost <= 1.96)
                label(1,5)=1;
            end
            if(corStressor>1.96 && corStress<=1.96 && corPost > 1.96)
                label(1,6)=1;
            end
            if(corStressor<=1.96 && corStress>1.96 && corPost > 1.96)
                label(1,7)=1;
            end
            if(corStressor > 1.96 && corStress > 1.96 && corPost > 1.96)
                label(1,8)=1;
            end
            
            for I=1:1:8
                for thresh = -0.5:0.01:0.5
                    y_s = y_ori;
                    if(label(1,I)==1)
                        for k=1:1:k1                            
                            y_s(k,1) = y_s(k,1) - avgHis*thresh;                       
                        end
                    end
                                     
                    for k = 1:1:k1 
                        if(y_s(k,1)<0)
                            y_s(k,1)=0;
                        end
                        if(y(k,1)<0)
                            y(k,1)=0;
                            pause;
                        end
                    end
                    
                    MSE = 0;
                    RMSE = 0;
                    MAPE = 0;
                    MAD = 0;
                    for i=1:1:k1
                        MSE = MSE+(y_s(i,1)-y(i,1))^2;
                        if(y(i,1)>y_s(i,1) && y(i,1)>0)
                            MAPE = MAPE + abs(y(i,1)-y_s(i,1))/y(i,1);
                        end
                        if(y_s(i,1)>=y(i,1)&& y_s(i,1)>0)
                            MAPE = MAPE + abs(y_s(i,1)-y(i,1))/y_s(i,1);
                        end                       
                        MAD = MAD + abs(y_s(i,1)-y(i,1));
                    end
                    if(k1>0) 
                        MSE = MSE/k1;
                        RMSE = sqrt(MSE);
                        MAPE = MAPE/k1;
                        MAD = MAD/k1;
                    end
                    
                    l_a = floor(thresh*(100)+51+eps(100));
                    if(I==1)
                        I1(l_a,1) = I1(l_a,1)+MSE;
                        I1(l_a,2) = I1(l_a,2)+RMSE;
                        I1(l_a,3) = I1(l_a,3)+MAPE;
                        I1(l_a,4) = I1(l_a,4)+MAD;
                    end
                    if(I==2)
                        I2(l_a,1) = I2(l_a,1)+MSE;
                        I2(l_a,2) = I2(l_a,2)+RMSE;
                        I2(l_a,3) = I2(l_a,3)+MAPE;
                        I2(l_a,4) = I2(l_a,4)+MAD;
                    end
                    if(I==3)
                        I3(l_a,1) = I3(l_a,1)+MSE;
                        I3(l_a,2) = I3(l_a,2)+RMSE;
                        I3(l_a,3) = I3(l_a,3)+MAPE;
                        I3(l_a,4) = I3(l_a,4)+MAD;
                    end
                    if(I==4)
                        I4(l_a,1) = I4(l_a,1)+MSE;
                        I4(l_a,2) = I4(l_a,2)+RMSE;
                        I4(l_a,3) = I4(l_a,3)+MAPE;
                        I4(l_a,4) = I4(l_a,4)+MAD;
                   
                    end
                    
                    if(I==5)
                        I5(l_a,1) = I5(l_a,1)+MSE;
                        I5(l_a,2) = I5(l_a,2)+RMSE;
                        I5(l_a,3) = I5(l_a,3)+MAPE;
                        I5(l_a,4) = I5(l_a,4)+MAD;
                    end
                    if(I==6)
                        I6(l_a,1) = I6(l_a,1)+MSE;
                        I6(l_a,2) = I6(l_a,2)+RMSE;
                        I6(l_a,3) = I6(l_a,3)+MAPE;
                        I6(l_a,4) = I6(l_a,4)+MAD;
                    end
                    if(I==7)
                        I7(l_a,1) = I7(l_a,1)+MSE;
                        I7(l_a,2) = I7(l_a,2)+RMSE;
                        I7(l_a,3) = I7(l_a,3)+MAPE;
                        I7(l_a,4) = I7(l_a,4)+MAD;
                    end
                    if(I==8)
                        I8(l_a,1) = I8(l_a,1)+MSE;
                        I8(l_a,2) = I8(l_a,2)+RMSE;
                        I8(l_a,3) = I8(l_a,3)+MAPE;
                        I8(l_a,4) = I8(l_a,4)+MAD;
                    end
                    
              end
            end
        end
        
        disp('file end-------------------------');
        
    end 
    
    if(USER_ALL>0)
        [s3,s4] = size(I1);
        for b_l=1:1:s3
            fprintf(foutMetric,'%d %d %.4f %.4f %.4f %.4f\r\n', TOPIC, 1, I1(b_l,1)/USER_ALL, I1(b_l,2)/USER_ALL,...
                I1(b_l,3)/USER_ALL, I1(b_l,4)/USER_ALL);
        end
        for b_l=1:1:s3
            fprintf(foutMetric,'%d %d %.4f %.4f %.4f %.4f\r\n', TOPIC, 2, I2(b_l,1)/USER_ALL, I2(b_l,2)/USER_ALL,...
                I2(b_l,3)/USER_ALL, I2(b_l,4)/USER_ALL);
        end
        for b_l=1:1:s3
            fprintf(foutMetric,'%d %d %.4f %.4f %.4f %.4f\r\n', TOPIC, 3, I3(b_l,1)/USER_ALL, I3(b_l,2)/USER_ALL,...
                I3(b_l,3)/USER_ALL, I3(b_l,4)/USER_ALL);
        end
        for b_l=1:1:s3
            fprintf(foutMetric,'%d %d %.4f %.4f %.4f %.4f\r\n', TOPIC, 4, I4(b_l,1)/USER_ALL, I4(b_l,2)/USER_ALL,...
                I4(b_l,3)/USER_ALL, I4(b_l,4)/USER_ALL);
        end
        %---
        for b_l=1:1:s3
            fprintf(foutMetric,'%d %d %.4f %.4f %.4f %.4f\r\n', TOPIC, 5, I5(b_l,1)/USER_ALL, I5(b_l,2)/USER_ALL,...
                I5(b_l,3)/USER_ALL, I5(b_l,4)/USER_ALL);
        end
        for b_l=1:1:s3
            fprintf(foutMetric,'%d %d %.4f %.4f %.4f %.4f\r\n', TOPIC, 6, I6(b_l,1)/USER_ALL, I6(b_l,2)/USER_ALL,...
                I6(b_l,3)/USER_ALL, I6(b_l,4)/USER_ALL);
        end
        for b_l=1:1:s3
            fprintf(foutMetric,'%d %d %.4f %.4f %.4f %.4f\r\n', TOPIC, 7, I7(b_l,1)/USER_ALL, I7(b_l,2)/USER_ALL,...
                I7(b_l,3)/USER_ALL, I7(b_l,4)/USER_ALL);
        end
        for b_l=1:1:s3
            fprintf(foutMetric,'%d %d %.4f %.4f %.4f %.4f\r\n', TOPIC, 8, I8(b_l,1)/USER_ALL, I8(b_l,2)/USER_ALL,...
                I8(b_l,3)/USER_ALL, I8(b_l,4)/USER_ALL);
        end
        
    end
    fclose(foutMetric);
end  
fclose(foutAllAvg);