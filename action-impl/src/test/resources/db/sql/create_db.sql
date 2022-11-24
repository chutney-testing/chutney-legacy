DROP TABLE users;

CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY,
  name VARCHAR(30),
  email  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS allsqltypes (
  col_boolean BIT,
  col_tinyint TINYINT,
  col_smallint SMALLINT,
  col_mediumint MEDIUMINT,
  col_integer INTEGER,
  col_bigint BIGINT,
  col_float REAL,
  col_double DOUBLE,
  col_decimal DECIMAL(20,4),
  col_date DATE,
  col_time TIME,
  col_timestamp TIMESTAMP,
  col_interval_year INTERVAL YEAR,
  col_interval_second INTERVAL SECOND,
  col_char CHAR,
  col_varchar VARCHAR
);
