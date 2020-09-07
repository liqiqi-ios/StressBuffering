pathAll = 'E:\TEST\POSITIVE\Pair\ttest\res\ttest.txt';
foutAll = fopen(pathAll,'w');
stress_n1 = 0;
p1 = 0;
p2 = 0;
p3 = 0;
p4 = 0;

un1 = 0;
up1 = 0;
up2 = 0;
up3 = 0;
up4 = 0;
    
for TOPIC = 0:1:4
    %out
    all_path = ['E:\TEST\POSITIVE\Pair\ttest\res\ttestSI',num2str(TOPIC),'.txt'];
    fout = fopen(all_path,'w');
    %out
    all_path_u = ['E:\TEST\POSITIVE\Pair\ttest\res\ttestUSI',num2str(TOPIC),'.txt'];
    fout_u = fopen(all_path_u,'w');

    %In: pre-SI, SI, post-SI
    path_SI = ['E:\TEST\POSITIVE\Pair\ttest\T',num2str(TOPIC),'\all_SI_TTest.txt'];      
    N1_pre = 0;
    N0_pre = 0;
    N1_post = 0;
    N0_post = 0;
    
    fid=fopen(path_SI);    
    s=fgetl(fid);
    l_num = 0;
    while ischar(s)
        l_num = l_num+1;
        %字符串转为数组
        temp='';
        f=[];
        for i=1:1:length(s)
            if s(i)~=' '
                temp=[temp,s(i)];
            else
                if length(temp)~=0
                    f=[f,str2num(temp)];
                    temp='';
                end
            end
        end
        if length(temp)~=0
            f=[f,str2num(temp)];
        end
        SI = f;
        %得到转化后的数组
        
        [m1,n1]=size(SI);
        pos1 = n1/3;
        pos2 = pos1*2;
        curPre = SI(1,1:pos1);
        curSI = SI(1,pos1+1:pos2);
        curPost = SI(1,pos2+1:n1);       
        
        [H_pre,P_pre]=ttest2(curPre,curSI);
        [H,P]=ttest2(curSI,curPost);
        if(H_pre == 0)
            N0_pre = N0_pre+1;
        end
        if(H_pre == 1)
            N1_pre = N1_pre+1;
        end
        if(H == 0 )
            N0_post = N0_post+1;
        end
        if(H == 1)
            N1_post = N1_post +1;
        end
        fprintf(fout,'%f %f %f %f\r\n',H_pre,P_pre,H,P);
        s = fgetl(fid);
    end    
    fclose(fid);
    fprintf(foutAll,'%d %f %f %f %f\r\n',TOPIC, N0_pre*1.0/l_num, N1_pre*1.0/l_num, N0_post*1.0/l_num, N1_post*1.0/l_num);
    p1= p1+N0_pre;%SI-pre H0
    p2 = p2 +N1_pre;%SI-pre H1
    p3 = p3 + N0_post;%SI-post H0
    p4 = p4 + N1_post;%SI-post H1
    stress_n1 =stress_n1+l_num;
    %end-SI
    
    %In: pre-USI, USI, post-USI
    path_USI = ['E:\TEST\POSITIVE\Pair\ttest\T',num2str(TOPIC),'\all_USI_TTest.txt'];
    UN1_pre = 0;
    UN0_pre = 0;
    UN1_post = 0;
    UN0_post = 0;
    fid_u=fopen(path_USI);    
    s_u=fgetl(fid_u);
    line_num = 0;
    while ischar(s_u)
        line_num = line_num+1;
        %字符串转为数组
        temp='';
        f=[];
        for i=1:1:length(s_u)
            if s_u(i)~=' '
                temp=[temp,s_u(i)];
            else
                if length(temp)~=0
                    f=[f,str2num(temp)];
                    temp='';
                end
            end
        end
        if length(temp)~=0
            f=[f,str2num(temp)];
        end
        USI = f;
        %得到转化后的数组
         
        [m2,n2]=size(USI);
        pos1 = n2/3;
        pos2 = pos1*2;
        curPre = USI(1,1:pos1);
        curSI = USI(1,pos1+1:pos2);
        curPost = USI(1,pos2+1:n2);
        [H_pre_u,P_pre_u]=ttest2(curPre,curSI);
        [H_u,P_u]=ttest2(curSI,curPost);
        if(H_pre_u == 0)
            UN0_pre = UN0_pre+1;
        end
        if(H_pre_u == 1)
            UN1_pre = UN1_pre+1;
        end
        if(H_u == 0 )
            UN0_post = UN0_post+1;
        end
        if(H_u == 1)
            UN1_post = UN1_post +1;
        end
        fprintf(fout_u,'%f %f %f %f\r\n',H_pre_u,P_pre_u,H_u,P_u);
        s_u = fgetl(fid_u);
    end
    fclose(fid_u);
    fprintf(foutAll,'%d %f %f %f %f\r\n',TOPIC, UN0_pre*1.0/line_num, UN1_pre*1.0/line_num, UN0_post*1.0/line_num, UN1_post*1.0/line_num);
    
    up1= up1+UN0_pre;%SI-pre H0
    up2 = up2 +UN1_pre;%SI-pre H1
    up3 = up3 + UN0_post;%SI-post H0
    up4 = up4 + UN1_post;%SI-post H1
    un1 = un1+line_num;
    %---end USI
    
    fclose(fout);
    fclose(fout_u);
end

fprintf(foutAll,'All-SI:');
fprintf(foutAll,'%f %f %f %f\r\n', p1*1.0/stress_n1, p2*1.0/stress_n1, p3*1.0/stress_n1, p4*1.0/stress_n1);

fprintf(foutAll,'All-USI:');
fprintf(foutAll,'%f %f %f %f\r\n', up1*1.0/un1, up2*1.0/un1, up3*1.0/un1, up4*1.0/un1);

fclose(foutAll);
