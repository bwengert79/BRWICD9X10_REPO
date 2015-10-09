@echo off

"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\schema_tables.zip" icd9x10_schema_tables.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\schema_views.zip" icd9x10_schema_views.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\schema_indexes.zip" icd9x10_schema_indexes.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\schema_triggers.zip" icd9x10_schema_triggers.sql

"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\data_folder.zip" icd9x10_data_folder.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\data_icd9.zip" icd9x10_data_icd9.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\data_icd9_gem.zip" icd9x10_data_icd9_gem.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\data_icd10.zip" icd9x10_data_icd10.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\data_icd10_gem.zip" icd9x10_data_icd10_gem.sql
"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\data_sequence.zip" icd9x10_data_sequence.sql

"C:\Program Files\7-Zip\7z.exe" a -tzip "..\assets\upgrade_v02.zip" icd9x10_upgrade_v02.sql