DROP TABLE IF EXISTS todo
CREATE TABLE todo(todoid binary not null, accountid binary not null, email varchar(255), description varchar(255), completed boolean, primary key (todoid))