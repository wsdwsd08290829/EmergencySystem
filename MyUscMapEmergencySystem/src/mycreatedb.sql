create table mybuildings(
  buildingID char(50) primary key,
  buildingName char(50),
  shape SDO_GEOMETRY
);


create table mystudents(
  studentID char(50) primary key,
  shape SDO_GEOMETRY
);


create table myannouncementsystems(
  announcementSystemID char(50)  primary key,
  shape SDO_GEOMETRY,
  center SDO_GEOMETRY,
  radius NUMBER
);

INSERT INTO USER_SDO_GEOM_METADATA VALUES ('mybuildings', 'shape', SDO_DIM_ARRAY(SDO_DIM_ELEMENT('X', 0, 850, 0.005), SDO_DIM_ELEMENT('Y', 0, 600, 0.005)), NULL);

INSERT INTO USER_SDO_GEOM_METADATA VALUES ('mystudents', 'shape', SDO_DIM_ARRAY(SDO_DIM_ELEMENT('X', 0, 850, 0.005), SDO_DIM_ELEMENT('Y', 0, 600, 0.005)), NULL);

INSERT INTO USER_SDO_GEOM_METADATA VALUES ('myannouncementsystems', 'shape', SDO_DIM_ARRAY(SDO_DIM_ELEMENT('X', 0, 850, 0.005), SDO_DIM_ELEMENT('Y', 0, 600, 0.005)), NULL);

INSERT INTO USER_SDO_GEOM_METADATA VALUES ('myannouncementsystems', 'center', SDO_DIM_ARRAY(SDO_DIM_ELEMENT('X', 0, 850, 0.005), SDO_DIM_ELEMENT('Y', 0, 600, 0.005)), NULL);

CREATE INDEX mybuildings_idx ON mybuildings (shape) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

CREATE INDEX mystudents_idx ON mystudents (shape) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

CREATE INDEX myannouncement_idx ON myannouncementsystems (shape) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

CREATE INDEX myannouncementCenter_idx ON myannouncementsystems (center) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

