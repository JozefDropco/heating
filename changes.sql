alter table ``LONG_SETTING``
    add `GROUP` VARCHAR(60) null;

alter table ``LONG_SETTING``
    add DESCRIPTION VARCHAR(255) null;

alter table ``LONG_SETTING``
    add VALUE_TYPE VARCHAR(10) null;

alter table ``DOUBLE_SETTING``
    add `GROUP` VARCHAR(60) null;

alter table ``DOUBLE_SETTING``
    add DESCRIPTION VARCHAR(255) null;

alter table ``DOUBLE_SETTING``
    add VALUE_TYPE VARCHAR(10) null;


alter table ``STRING_SETTING``
    add `GROUP` VARCHAR(60) null;

alter table ``STRING_SETTING``
    add DESCRIPTION VARCHAR(255) null;

alter table ``STRING_SETTING``
    add VALUE_TYPE VARCHAR(10) null;

DELETE
FROM `LONG_SETTING`
WHERE REF_CD LIKE 'WATERING_PUMP_STOP_DELAY' ESCAPE '#';

DELETE
FROM `LONG_SETTING`
WHERE REF_CD LIKE 'WATER_PUMP_WAIT_TIME' ESCAPE '#';

UPDATE `LONG_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'ID pozície kolektorov v prípade silného vetra',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'STRONG_WIND_POSITION' ESCAPE '#';

UPDATE `LONG_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'ID pozície kolektorov v prípade prehriatia',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'OVERHEAT_POSITION' ESCAPE '#';

UPDATE `LONG_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Stav jasu pre tento deň',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'DAYLIGHT' ESCAPE '#';

UPDATE `LONG_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Počet sekúnd nepretržitého jasu, aby bola splnená podmienka pre stav jasu ',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'LIGHT_THRESHOLD' ESCAPE '#';

UPDATE `LONG_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'ID poslednej známej pozície kolektorov',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'LAST_KNOWN_POSITION' ESCAPE '#';

UPDATE `LONG_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Počet sekúnd pozastavenia otáčania kolektorov v prípade náhlej zmeny polohy',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'SOLAR_PANEL_DELAY' ESCAPE '#';


UPDATE `DOUBLE_SETTING` t
SET t.`GROUP`     = 'Kúrenie',
    t.DESCRIPTION = 'Rozdiel teplôt pri ktorej sa 3-cesrtný ventil prepína na ohrev kotlom počas pracovného týždňa',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'THREE_WAY_VALVE_DIFF_STOP_TEMP' ESCAPE '#';

UPDATE `DOUBLE_SETTING` t
SET t.`GROUP`     = 'Kúrenie',
    t.DESCRIPTION = 'Rozdiel teplôt pri ktorej sa obehové čerpadlo kolektorov zapne',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'CIRCULAR_PUMP_DIFF_START_TEMP' ESCAPE '#';

UPDATE `DOUBLE_SETTING` t
SET t.`GROUP`     = 'Zavlažovanie',
    t.DESCRIPTION = 'Minimálna teplota na zavlažovanie',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'TEMP_THRESHOLD' ESCAPE '#';

UPDATE `DOUBLE_SETTING` t
SET t.`GROUP`     = 'Kúrenie',
    t.DESCRIPTION = 'Rozdiel teplôt pri ktorej sa obehové čerpadlo kolektorov vypne',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'CIRCULAR_PUMP_DIFF_STOP_TEMP' ESCAPE '#';

UPDATE `DOUBLE_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Teplota pri ktorej sa kolektory považujú za prehriate',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'SOLAR_OVERHEATED' ESCAPE '#';

UPDATE `DOUBLE_SETTING` t
SET t.`GROUP`     = 'Kúrenie',
    t.DESCRIPTION = 'Rozdiel teplôt pri ktorej sa 3-cesrtný ventil prepína na ohrev kotlom počas víkendu',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'WEEKEND_THREE_WAY_VALVE_DIFF_STOP_TEMP' ESCAPE '#';

UPDATE `DOUBLE_SETTING` t
SET t.DESCRIPTION = 'Rozdiel teplôt pri ktorej sa 3-cesrtný ventil prepína na ohrev kolektormi',
    t.VALUE_TYPE  = 'number'
WHERE t.REF_CD LIKE 'THREE_WAY_VALVE_DIFF_START_TEMP' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Zavlažovanie',
    t.DESCRIPTION = 'Výstupný pin pre zónu 1',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'WATERING1' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Výstupný pin pre posun kolektorov - sever',
    t.VALUE_TYPE  = 'ext-pin'
WHERE t.REF_CD LIKE 'SOLAR_NORTH' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Zavlažovanie',
    t.DESCRIPTION = 'Vstupný pin pre dážď',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'RAIN_SENSOR' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Výstupný pin pre posun kolektorov - juh',
    t.VALUE_TYPE  = 'ext-pin'
WHERE t.REF_CD LIKE 'SOLAR_SOUTH' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Výstupný pin pre posun kolektorov - západ',
    t.VALUE_TYPE  = 'ext-pin'
WHERE t.REF_CD LIKE 'SOLAR_WEST' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Výstupný pin pre ovládanie ANDu',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'EXTEND_GATE_PIN' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Vstupný pin pre meranie jasu',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'DAY_LIGHT_PIN' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Zavlažovanie',
    t.DESCRIPTION = 'Vstupný pin pre získanie stavu čerpadla',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'WATER_PUMP_FEEDBACK' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kúrenie',
    t.DESCRIPTION = 'Výstupný pin pre zapnutie obehového čerpadla kolektorov',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'CIRCULAR_PUMP_PORT' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Zavlažovanie',
    t.DESCRIPTION = 'Výstupný pin pre zónu 3',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'WATERING3' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Vstupný pin pre meranie silného vetra',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'STRONG_WIND_PIN' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Výstupný pin pre posun kolektorov - východ',
    t.VALUE_TYPE  = 'ext-pin'
WHERE t.REF_CD LIKE 'SOLAR_EAST' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Výstupný pin pre vystavenie hodnoty do posuvného registru',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'EXTEND_DATA_OUT_PIN' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kúrenie',
    t.DESCRIPTION = 'Výstupný pin pre povolenie ohrevu TA3 z kotla',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'BOILER_PORT' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Zavlažovanie',
    t.DESCRIPTION = 'Výstupný pin pre ovládanie čerpadla',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'WATER_PUMP' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Zavlažovanie',
    t.DESCRIPTION = 'Výstupný pin pre zónu 2',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'WATERING2' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Zavlažovanie',
    t.DESCRIPTION = 'Výstupný pin pre napúšťanie sudu',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'WATERING_BAREL' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Štatistiky',
    t.DESCRIPTION = 'Posledný čas kedy bolo RasPi ešte živé',
    t.VALUE_TYPE  = 'date'
WHERE t.REF_CD LIKE 'LAST_HEARTBEAT' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kúrenie',
    t.DESCRIPTION = 'Výstupný pin pre ovládanie 3-cestného ventila',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'THREE_WAY_PORT' ESCAPE '#';

UPDATE `STRING_SETTING` t
SET t.`GROUP`     = 'Kolektory',
    t.DESCRIPTION = 'Výstupný pin pre ovládanie posuvného registru (CLOCK)',
    t.VALUE_TYPE  = 'pin'
WHERE t.REF_CD LIKE 'EXTEND_CLOCK_PIN' ESCAPE '#';

UPDATE `DOUBLE_SETTING` t
SET t.`GROUP` = 'Kúrenie'
WHERE t.REF_CD LIKE 'THREE_WAY_VALVE_DIFF_START_TEMP' ESCAPE '#';


