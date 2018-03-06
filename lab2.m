clc;
clear;
O = xlsread('occupancy.xlsx');
max = max(O)
figure(1);
histogram(O,max+1);
xlabel('Occupancy');
ylabel('Occurances');
title('Part 1: Occupancy Occurances');
