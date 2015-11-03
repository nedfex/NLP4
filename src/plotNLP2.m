clear all;
fid = fopen('DATA.txt');
C = textscan(fid,'%f,%f,%f,%f,%f');
A = cell2mat(C);
fclose(fid);
preplex = zeros(18,18);
%%
ind = find(round(A(:,1)*100) == 400);
for i=1:1:length(ind)
    preplex(round(A(ind(i),3)*100)/5,round(A(ind(i),4)*100)/5) = A(ind(i),5); 
end
preplex(preplex==0)=NaN;
%%
close all;

figure;
subplot(1,2,1);
surf([0.05:0.05:0.05*size(preplex,1)],[0.05:0.05:0.05*size(preplex,2)],preplex);
xlabel('TOKEN','FontSize',20);ylabel('Tri-gram','FontSize',20);zlabel('Accurancy','FontSize',20);
%% draw 2D alpha
ind = find(round(A(:,2)*100)==20 & round(A(:,3)*100)==65 & round(A(:,4)*100)==15);
Z=[];
for i=1:1:length(ind)
    Z(i,1) = A(ind(i),1); 
    Z(i,2) = A(ind(i),5); 
end
Z = sortrows(Z,2);
subplot(1,2,2);
plot([0.25:0.25:0.25*length(Z)],Z(:,2));grid on;ylabel('Accuracy','FontSize',20);xlabel('alpha','FontSize',20);

%% perceptron result
iter =[1,5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,100,120,150,200,500];
acc=[0.179,0.554,0.63,0.693,0.7,0.742,0.774,0.788,0.806,0.798,0.824,0.826,0.8268,0.8337,0.834,0.839,0.838,0.842,0.842,0.842,0.842,0.842];
figure;
plot(iter,acc);
ylabel('Accuracy','FontSize',20);xlabel('Iterations','FontSize',20);grid on;
%% linyu
clear all;
close all;
fid = fopen('output_tri_var.txt');
C = textscan(fid,'%f,%f');
A = cell2mat(C);
fclose(fid);
figure;
plot(A(:,1),A(:,2));
ylabel('Accuracy','FontSize',20);xlabel('Trigram weight','FontSize',20);grid on;
%%
fid = fopen('output_iter2.txt');
C = textscan(fid,'%f,%f');
A = cell2mat(C);
fclose(fid);
figure;
plot(A(:,1),A(:,2));
ylabel('Accuracy','FontSize',20);xlabel('Perceptron Iterations','FontSize',20);grid on;
%%
fid = fopen('output_four_var.txt');
C = textscan(fid,'%f,%f');
A = cell2mat(C);
fclose(fid);
figure;
plot(A(:,1),A(:,2));
ylabel('Accuracy','FontSize',20);xlabel('Four-gram weight','FontSize',20);grid on;
%%
fid = fopen('output_segma.txt');
C = textscan(fid,'%f,%f');
A = cell2mat(C);
fclose(fid);
figure;
plot(A(:,1),A(:,2));
ylabel('Accuracy','FontSize',20);xlabel('Sigma value','FontSize',20);grid on;
%%
fid = fopen('output_word_var.txt');
C = textscan(fid,'%f,%f');
A = cell2mat(C);
fclose(fid);
figure;
plot(A(:,1),A(:,2));
ylabel('Accuracy','FontSize',20);xlabel('Word feature weight','FontSize',20);grid on;