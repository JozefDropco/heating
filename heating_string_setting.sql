DROP table `STRING_SETTING`;
create table `STRING_SETTING`
(
    REF_CD      varchar(50)                             not null
        primary key,
    VALUE       varchar(255)                            not null,
    MODIFIED_TS timestamp default '0000-00-00 00:00:00' not null on update current_timestamp(),
    `GROUP`     varchar(60)                             null,
    DESCRIPTION varchar(255)                            null,
    VALUE_TYPE  varchar(10)                             null
)
    charset = utf8mb4;

INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('BOILER_BLOCK_PIN', 'GPIO 9', '2021-01-18 09:47:38', 'Kúrenie', 'Výstupný pin pre blokovanie ohrevu TA3 z kotla', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('DAY_LIGHT_PIN', 'GPIO 15', '2021-01-19 21:20:42', 'Kolektory', 'Vstupný pin pre meranie jasu', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('EXTEND_CLOCK_PIN', 'GPIO 2', '2021-01-14 10:44:52', 'Kolektory', 'Výstupný pin pre ovládanie posuvného registru (CLOCK)', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('EXTEND_DATA_OUT_PIN', 'GPIO 0', '2021-01-14 10:44:52', 'Kolektory', 'Výstupný pin pre vystavenie hodnoty do posuvného registru', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('EXTEND_GATE_PIN', 'GPIO 3', '2021-01-14 10:44:52', 'Kolektory', 'Výstupný pin pre ovládanie ANDu', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('LAST_HEARTBEAT', '26. 01. 2021 11:17:21 CET', '2021-01-26 11:17:21', 'Štatistiky', 'Posledný čas kedy bolo RasPi ešte živé', 'date');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('RAIN_SENSOR', 'GPIO 5', '2021-01-14 10:44:52', 'Zavlažovanie', 'Vstupný pin pre dážď', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_CIRCULAR_PUMP_PORT', 'GPIO 7', '2021-01-18 09:36:33', 'Kúrenie', 'Výstupný pin pre zapnutie obehového čerpadla kolektorov', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_CIRCULAR_PUMP_T1_MEASURE_PLACE', 'SOLAR', '2021-01-18 09:33:38', 'Kúrenie', 'Meracie miesto pre teplotu T1 obehového čerpadla kolektorov', 'string');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_CIRCULAR_PUMP_T2_MEASURE_PLACE', 'PRED_TA3', '2021-01-18 09:35:10', 'Kúrenie', 'Meracie miesto pre teplotu T2 obehového čerpadla kolektorov', 'string');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_EAST', 'GPIO 104', '2021-01-14 10:44:52', 'Kolektory', 'Výstupný pin pre posun kolektorov - východ', 'ext-pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_NORTH', 'GPIO 102', '2021-01-14 10:44:52', 'Kolektory', 'Výstupný pin pre posun kolektorov - sever', 'ext-pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_SOUTH', 'GPIO 101', '2021-01-14 10:44:52', 'Kolektory', 'Výstupný pin pre posun kolektorov - juh', 'ext-pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_WEST', 'GPIO 103', '2021-01-14 10:44:52', 'Kolektory', 'Výstupný pin pre posun kolektorov - západ', 'ext-pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('STRONG_WIND_PIN', 'GPIO 1', '2021-01-14 10:44:52', 'Kolektory', 'Vstupný pin pre meranie silného vetra', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('THREE_WAY_PORT', 'GPIO 8', '2021-01-14 10:44:52', 'Kúrenie', 'Výstupný pin pre ovládanie 3-cestného ventila', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('THREE_WAY_VALVE_T2_MEASURE_PLACE', 'TA3', '2021-01-18 09:40:18', 'Kúrenie', 'Meracie miesto pre teplotu T2 3-cestného ventilu', 'string');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('THREE_WAY_VALVE_T31_MEASURE_PLACE', 'PRED_TA3', '2021-01-18 09:40:18', 'Kúrenie', 'Meracie miesto pre teplotu T3.1 3-cestného ventilu', 'string');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('WATERING1', 'GPIO 29', '2021-01-14 10:44:52', 'Zavlažovanie', 'Výstupný pin pre zónu 1', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('WATERING2', 'GPIO 27', '2021-01-14 10:44:52', 'Zavlažovanie', 'Výstupný pin pre zónu 2', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('WATERING3', 'GPIO 25', '2021-01-14 10:44:52', 'Zavlažovanie', 'Výstupný pin pre zónu 3', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('WATERING_BAREL', 'GPIO 22', '2021-01-14 10:44:52', 'Zavlažovanie', 'Výstupný pin pre napúšťanie sudu', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('WATER_PUMP', 'GPIO 4', '2021-01-14 10:44:52', 'Zavlažovanie', 'Výstupný pin pre ovládanie čerpadla', 'pin');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('WATER_PUMP_FEEDBACK', 'GPIO 26', '2021-01-14 10:44:52', 'Zavlažovanie', 'Vstupný pin pre získanie stavu čerpadla', 'pin');
