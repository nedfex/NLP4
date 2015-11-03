fid = fopen('triresult.txt');
C = textscan(fid,'%f,%f,%f,%f,%f');
A = cell2mat(C);
fclose(fid);


preplex = zeros(100,100);
err = zeros(100,100);
%%
for i=1:1:size(A,1)
    preplex(round(A(i,1)*100),round(A(i,2)*100)) = A(i,4); 
    err(round(A(i,1)*100),round(A(i,2)*100)) = A(i,5);
end
preplex(preplex==0) =NaN;
err(err==0) =NaN;
%%
close all;

figure;
subplot(1,2,1);
meshc([0.01:0.01:1.0],[0.01:0.01:1.0],preplex);
xlabel('lambda1');ylabel('lambda2');
subplot(1,2,2);
meshc([0.01:0.01:1.0],[0.01:0.01:1.0],err);
xlabel('lambda1');ylabel('lambda2');

%%
fid = fopen('biresult.txt');
C = textscan(fid,'%f,%f,%f,%f');
A = cell2mat(C);
fclose(fid);

preplex = zeros(100,1);
err = zeros(100,1);
%%
for i=1:1:size(A,1)
    preplex(round(A(i,1)*100),1) = A(i,3); 
    err(round(A(i,1)*100),1) = A(i,4);
end
preplex(preplex==0) =NaN;
err(err==0) =NaN;
%%

figure;
subplot(1,2,1);
plot([0.01:0.01:1.0],preplex);
xlabel('lambda1');ylabel('Perplexity');
subplot(1,2,2);
plot([0.01:0.01:1.0],err);
xlabel('lambda1');ylabel('WER');
