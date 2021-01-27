DROP TABLE  `DOUBLE_SETTING`;
create table `DOUBLE_SETTING`
(
    REF_CD      varchar(50)                             not null
        primary key,
    VALUE       double                                  not null,
    MODIFIED_TS timestamp default '0000-00-00 00:00:00' not null on update current_timestamp(),
    `GROUP`     varchar(60)                             null,
    DESCRIPTION varchar(255)                            null,
    VALUE_TYPE  varchar(10)                             null
)
    engine = MyISAM
    charset = utf8mb4;

INSERT INTO `DOUBLE_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('TEMP_THRESHOLD', 5, '2021-01-14 10:29:39', 'Zavlažovanie', 'Minimálna teplota na zavlažovanie', 'number');
INSERT INTO `DOUBLE_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_OVERHEATED', 85, '2021-01-14 10:29:39', 'Kolektory', 'Teplota pri ktorej sa kolektory považujú za prehriate', 'number');
INSERT INTO `DOUBLE_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_CIRCULAR_PUMP_DIFF_STOP_TEMP', 5, '2021-01-18 09:37:03', 'Kúrenie', 'Rozdiel teplôt pri ktorej sa obehové čerpadlo kolektorov vypne', 'number');
INSERT INTO `DOUBLE_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_CIRCULAR_PUMP_DIFF_START_TEMP', 10, '2021-01-18 09:37:03', 'Kúrenie', 'Rozdiel teplôt pri ktorej sa obehové čerpadlo kolektorov zapne', 'number');
