filelist = dir('E:\TEST\POSITIVE\Depart\upLevel\*.txt');
for pos = 1:length(filelist)
    path_up = ['E:\TEST\POSITIVE\upDaySta\',filelist(pos).name];
    path_str = ['E:\TEST\POSITIVE\DaySta\',filelist(pos).name];
    
    foutPathUp = ['E:\TEST\POSITIVE\smoothUp\',filelist(pos).name];
    foutPathStr = ['E:\TEST\POSITIVE\smoothStr\',filelist(pos).name];
    foutUp = fopen(foutPathUp,'w');
    foutStr = fopen(foutPathStr,'w');
    
    data_up = importdata(path_up);
    data_str = importdata(path_str);
        
    y_up = data_up(:,1);
    y_str = data_str(:,1);
    
    [m,n] = size(y_up);
    for i=1:1:m
        y_up(i,1)=y_up(i,1)*(-1);
    end
    
    x_up = ones(m,1);
    for i=1:1:m
        x_up(i,1)=i;
    end
    L = m*0.05;
    
    y_up2 = smooth(y_up,15,'lowess');
    y_str2 = smooth(y_str,15,'lowess');
    
    for i=1:1:m
        fprintf(foutUp,'%.4f \r\n', y_up2(i,1));
        fprintf(foutStr,'%.4f \r\n', y_str2(i,1));
    end
        
    fclose(foutUp);
    fclose(foutStr);
end