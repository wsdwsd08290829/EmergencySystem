This project is to simulate emergency alarm system which has following functionalities.
Results of query are reflected on map.jpg;
1. query for features: students, buildings, announcement systems;
   each of them can be activated and deactivated
2. query by different methods:
	(1). whole: 
	     query active feature and display all on map
	(2). point:
		 specify a point on map; 
		 query active feature within certain range(circle) around point, 
		 and mark nearest feature in different color
	(3). range: 
	     sequence of left click to get range (end with right click).
		 perform query for all active features within the range 
	(4). surround:
		 specify a point on map; 
		 system will find nearest announcement system and students within the system on map
	(5). based on (4), assume the announcement system is broken.
		 program will find second nearest announcement system for those student.(mark with same color)

3. Techniques used:
	Oracle database and geometry query.
	Junit and mockito testing.
	Modulize and refactor the code.
	Generate javaDoc
	eclipse project

4. Usage and run: 
	create db and user in oracle database
	run mycreatedb.sql to create table, insert meta data and set indexes; 
	load project to eclipse, add dependencies to class path and run emergency.java

5. Dependencies 
	sdoapi.jar(for spatial query), ojdbc6.jar(db driver)
	log4j-api.jar, junit4.jar, mockiot.jar

6. TODO Query Optimization