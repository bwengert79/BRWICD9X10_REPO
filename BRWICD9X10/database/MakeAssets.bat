@echo off

"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\schema_tables.zip" icd9x10_schema_tables.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\schema_views.zip" icd9x10_schema_views.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\schema_indexes.zip" icd9x10_schema_indexes.sql

"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\data_group.zip" icd9x10_data_group.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\data_icd9.zip" icd9x10_data_icd9.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\data_icd9_gem.zip" icd9x10_data_icd9_gem.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\data_icd10.zip" icd9x10_data_icd10.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\data_icd10_gem.zip" icd9x10_data_icd10_gem.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "S:\Eclipse\Workspaces\BRWICD9X10_WS\BRWICD9X10\assets\data_sequence.zip" icd9x10_data_sequence.sql
