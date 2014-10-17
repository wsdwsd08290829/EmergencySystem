
drop index myannouncement_idx;
drop index myannouncementCenter_idx;
drop index mystudents_idx;
drop index mybuildings_idx;

drop table mystudents;
drop table mybuildings;
drop table myannouncementsystems;

delete from USER_SDO_GEOM_METADATA U where U.table_name = 'MYANNOUNCEMENTSYSTEMS' AND U.COLUMN_NAME = 'SHAPE';
delete from USER_SDO_GEOM_METADATA U where U.table_name = 'MYANNOUNCEMENTSYSTEMS' AND U.COLUMN_NAME = 'CENTER';
delete from USER_SDO_GEOM_METADATA U where U.table_name = 'MYBUILDINGS' AND U.COLUMN_NAME = 'SHAPE';
delete from USER_SDO_GEOM_METADATA U where U.table_name = 'MYSTUDENTS' AND U.COLUMN_NAME = 'SHAPE';
#have to be uppercase when delete

