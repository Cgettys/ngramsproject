SET DEFAULT_PARALLEL 100;--Because 5.2 GB of data
data = LOAD '/user/root/ngrams-dataset/twograms/' USING PigStorage('\t') AS(word:chararray, year:int, count:int, books:int);
flt = FILTER data BY INDEXOF(word, '_', -1)==-1 AND count>10000 AND year==2000;
grouped = GROUP flt by year;
sorted = FOREACH grouped{
s = ORDER flt by count ASC;
limited = LIMIT s 1000;
GENERATE limited;
};
DUMP sorted;
STORE sorted into '/tmp/gettyscw/test3/test2.out' using PigStorage('\t');