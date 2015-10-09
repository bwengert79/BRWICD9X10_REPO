CREATE TRIGGER trg_icd9_folder_del
BEFORE DELETE ON icd9_folder
FOR EACH ROW BEGIN 
    DELETE from icd9_folderitem WHERE folder_id = OLD._id;
END;$

CREATE TRIGGER trg_icd10_folder_del
BEFORE DELETE ON icd10_folder
FOR EACH ROW BEGIN 
    DELETE from icd10_folderitem WHERE folder_id = OLD._id;
END;$
