--hive --hiveconf databaseName=testdata --hiveconf tableName=testTable --hiveconf inputPath=/tmp/testdata/googlebooks-eng-all-1gram-20120701-t -f testdataset.hql
SET hive.exec.dynamic.partition.mode=nonstrict;
SET hive.exec.max.dynamic.partitions=40000;
SET hive.exec.max.dynamic.partitions.pernode=10000;
CREATE DATABASE IF NOT EXISTS ${hiveconf:databaseName};
USE ${hiveconf:databaseName};
DROP TABLE IF EXISTS tmp;
CREATE TABLE tmp (word STRING, year SMALLINT, occur BIGINT, bookcount INT)
 ROW FORMAT DELIMITED
   FIELDS TERMINATED BY '\t'
 STORED AS TEXTFILE;
LOAD DATA INPATH '${hiveconf:inputPath}' OVERWRITE INTO TABLE tmp;
DROP TABLE IF EXISTS ${hiveconf:tableName};
CREATE TABLE ${hiveconf:tableName} (word STRING, occur BIGINT, bookcount INT)
PARTITIONED by (year SMALLINT)
STORED AS ORC;
INSERT OVERWRITE TABLE ${hiveconf:tableName} PARTITION(year) 
 SELECT * FROM tmp DISTRIBUTE BY year SORT by occur;
DROP TABLE tmp;
--SELECT * from ${hiveconf:tableName} where year=;

CREATE TABLE testTable (word STRING, occur BIGINT, bookcount INT)
PARTITIONED by (year SMALLINT)
STORED AS ORC;
--INSERT OVERWRITE TABLE testTable PARTITION(year) 
 SELECT * FROM tmp DISTRIBUTE BY year SORT by occur;

LOAD DATA INPATH '/tmp/testdata/googlebooks-eng-all-1gram-20120701-t' OVERWRITE INTO TABLE tmp;