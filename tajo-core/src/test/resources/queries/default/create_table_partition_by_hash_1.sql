CREATE TABLE sales ( col1 int, col2 int)
PARTITION BY HASH (col1)
PARTITIONS 2;