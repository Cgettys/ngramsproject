SET DEFAULT_PARALLEL 10;--Because 5.2 GB of data
data = LOAD '/user/root/ngrams-dataset/onegrams/' USING PigStorage('\t') AS(word:chararray, year:int, count:int, books:int);
grouped = GROUP data by year;
sorted = FOREACH grouped{
s = ORDER data by count ASC;
limited = LIMIT s 10000;
GENERATE limited;
};
DUMP sorted;
STORE sorted into '/tmp/week1/test2.out' using PigStorage('\t');