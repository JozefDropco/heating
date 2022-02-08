insert into `SOLAR_SCHEDULE` ("MONTH", HORIZONTAL_STEP, VERTICAL_STEP, SUN_RISE_HOUR, SUN_RISE_MINUTE, SUN_SET_HOUR,
                              SUN_SET_MINUTE)
values (3, 86, 29, 6, 30, 21, 0);

insert into `SOLAR_SCHEDULE` ("MONTH", HORIZONTAL_STEP, VERTICAL_STEP, SUN_RISE_HOUR, SUN_RISE_MINUTE, SUN_SET_HOUR,
                              SUN_SET_MINUTE)
values (4, 86, 38, 6, 30, 21, 0);

insert into `SOLAR_MOVE` ("MONTH", "HOUR", "MINUTE", HORIZONTAL, VERTICAL)
values (3, 8, 10, 0, -1),
       (3, 9, 10, 0, -1),
       (3, 10, 10, 0, -1),
       (3, 11, 10, 0, -1),
       (3, 11, 40, -1, 0),
       (3, 12, 10, -1, -1),
       (3, 12, 40, -1, 0),
       (3, 13, 10, -1, -1),
       (3, 13, 40, -1, 0),
       (3, 14, 10, -1, 1),
       (3, 14, 40, -1, 0),
       (3, 15, 10, -1, 1),
       (3, 16, 10, 0, 1),
       (3, 17, 10, 0, 1),
       (3, 18, 10, 0, 1),
       (4, 8, 10, 0, -1),
       (4, 9, 10, 0, -1),
       (4, 10, 10, 0, -1),
       (4, 11, 10, 0, -1),
       (4, 11, 40, -1, 0),
       (4, 12, 10, -1, -1),
       (4, 12, 40, -1, 0),
       (4, 13, 10, -1, -1),
       (4, 13, 40, -1, 0),
       (4, 14, 10, -1, 1),
       (4, 14, 40, -1, 0),
       (4, 15, 10, -1, 1),
       (4, 16, 10, 0, 1),
       (4, 17, 10, 0, 1),
       (4, 18, 10, 0, 1);
