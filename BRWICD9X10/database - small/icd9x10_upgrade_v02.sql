CREATE TABLE icd9_folder (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT);$
CREATE TABLE icd9_folderitem (_id INTEGER PRIMARY KEY AUTOINCREMENT,folder_id INTEGER,icd9_id INTEGER);$
CREATE TABLE icd10_folder (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT);$
CREATE TABLE icd10_folderitem (_id INTEGER PRIMARY KEY AUTOINCREMENT,folder_id INTEGER,icd10_id INTEGER);$

INSERT INTO icd9_folder VALUES(1,'My Favorites');$
INSERT INTO icd10_folder VALUES(1,'My Favorites');$

INSERT INTO "sqlite_sequence" VALUES('icd9_folder',1);$
INSERT INTO "sqlite_sequence" VALUES('icd10_folder',1);$

insert into icd9_folderitem (folder_id, icd9_id) select 1 as folder_id, icd9_id from icd9_groupitem where group_id in (select _id from icd9_group where type = 1 and name = 'FAV01');$
insert into icd10_folderitem (folder_id, icd10_id) select 1 as folder_id, icd10_id from icd10_groupitem where group_id in (select _id from icd10_group where type = 1 and name = 'FAV01');$

drop view icd9x10_view;$
drop view icd10x9_view;$

create view icd9x10_view as
select
	g._id,
	t9._id as icd9_id,
	t9.icd9_code,
	t9.long_desc as icd9_long_desc,
	t10._id as icd10_id,
	t10.icd10_code,
	t10.long_desc as icd10_long_desc
from
	icd10 as t10
	join icd9_gem as g on g.icd10_code = t10.icd10_code
	join icd9 as t9 on t9.icd9_code = g.icd9_code;$	

create view icd10x9_view as
select
	g._id,
	t9._id as icd9_id,
	t9.icd9_code,
	t9.long_desc as icd9_long_desc,
	t10._id as icd10_id,
	t10.icd10_code,
	t10.long_desc as icd10_long_desc
from
	icd9 as t9
	join icd10_gem as g on g.icd9_code = t9.icd9_code
	join icd10 as t10 on t10.icd10_code = g.icd10_code;$	
	
create view icd9_folder_view as
select
	i._id,
	i.folder_id, 
	f.name AS folder_name, 
	i.icd9_id, 
	t.icd9_code, 
	t.long_desc 
from
	icd9_folder AS f 
	join icd9_folderitem AS i on i.folder_id = f._id 
	join icd9 AS t on t._id = i.icd9_id;$

create view icd10_folder_view as
select
	i._id,
	i.folder_id, 
	f.name AS folder_name, 
	i.icd10_id, 
	t.icd10_code, 
	t.long_desc 
from
	icd10_folder AS f 
	join icd10_folderitem AS i on i.folder_id = f._id 
	join icd10 AS t on t._id = i.icd10_id;$
	
CREATE UNIQUE INDEX icd9_folder_idx ON icd9_folder(name);$
CREATE UNIQUE INDEX icd9_folderitem_idx ON icd9_folderitem(folder_id,icd9_id);$
CREATE UNIQUE INDEX icd10_folder_idx ON icd10_folder(name);$
CREATE UNIQUE INDEX icd10_folderitem_idx ON icd10_folderitem(folder_id,icd10_id);$

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

drop table icd9_group;$
drop table icd9_groupitem;$
drop table icd10_group;$
drop table icd10_groupitem;$
drop view icd9_fav01_view;$
drop view icd9_group_user_view;$
drop view icd10_fav01_view;$
drop view icd10_group_user_view;$
