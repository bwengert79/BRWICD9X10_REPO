CREATE TABLE icd9 (_id INTEGER PRIMARY KEY AUTOINCREMENT,icd9_code TEXT,long_desc TEXT);
CREATE TABLE icd10 (_id INTEGER PRIMARY KEY AUTOINCREMENT,icd10_code TEXT,long_desc TEXT);
CREATE TABLE icd9_gem (_id INTEGER PRIMARY KEY AUTOINCREMENT,icd9_code TEXT,icd10_code TEXT,approx_flag TEXT,nomap_flag TEXT,combo_flag TEXT,scenario_flag TEXT,choice_flag TEXT);
CREATE TABLE icd10_gem (_id INTEGER PRIMARY KEY AUTOINCREMENT,icd10_code TEXT,icd9_code TEXT,approx_flag TEXT,nomap_flag TEXT,combo_flag TEXT,scenario_flag TEXT,choice_flag TEXT);
CREATE TABLE icd9_group (_id INTEGER PRIMARY KEY AUTOINCREMENT,type INTEGER,name TEXT);
CREATE TABLE icd9_groupitem (_id INTEGER PRIMARY KEY AUTOINCREMENT,group_id INTEGER,icd9_id INTEGER);
CREATE TABLE icd10_group (_id INTEGER PRIMARY KEY AUTOINCREMENT,type INTEGER,name TEXT);
CREATE TABLE icd10_groupitem (_id INTEGER PRIMARY KEY AUTOINCREMENT,group_id INTEGER,icd10_id INTEGER);