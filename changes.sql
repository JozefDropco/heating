INSERT INTO heating.string_setting (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE)
VALUES ('HEATER_FLAME_PORT', 'GPIO 12', '2021-02-06 12:58:26', 'Kúrenie', 'Vstupný pin pre plameň v kotly', 'pin');

INSERT INTO heating.string_setting (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE)
VALUES ('HEATER_CIRCULAR_PUMP_PORT', 'GPIO 13', '2021-02-06 12:59:33', 'Kúrenie',
        'Vstupný pin pre chod čerpadla kúrenia ', 'pin');

INSERT INTO heating.string_setting (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE)
VALUES ('HEATER_BOILER_PORT', 'GPIO 14', '2021-02-06 13:00:38', 'Kúrenie', 'Vstupný pin pre ohrev TA3 plynovým kotlom ',
        'pin');

INSERT INTO heating.long_setting (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE)
VALUES ('HEATER_BLINK_STOP', 3000, '2021-02-06 13:02:24', 'Kúrenie',
        'Čas v milisekundach po ktorom sa má považovať vstup za vypnutý', 'number');

