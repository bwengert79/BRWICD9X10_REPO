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
