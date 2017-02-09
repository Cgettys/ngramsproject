raw_data = LOAD 'hdfs:///tmp/week1/test20.out' USING PigStorage(',') AS (
           word1:chararray,
           year1:int,
           count1:int,
           bookcount1:int     
);
dump raw_data;
STORE raw_data INTO 'hbase://wordcount' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage(
'word:word1
 year:year1 
 count:count1 
 bookcount:bookcount1'
);