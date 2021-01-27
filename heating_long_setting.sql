DROP TABLE  `LONG_SETTING`;
create table `LONG_SETTING`
(
    REF_CD      varchar(50)                             not null
        primary key,
    VALUE       int                                     not null,
    MODIFIED_TS timestamp default '0000-00-00 00:00:00' not null on update current_timestamp(),
    `GROUP`     varchar(60)                             null,
    DESCRIPTION varchar(255)                            null,
    VALUE_TYPE  varchar(10)                             null
)
    charset = utf8mb4;

INSERT INTO `LONG_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('DAYLIGHT', 0, '2021-01-26 09:03:45', 'Kolektory', 'Stav jasu pre tento deň', 'number');
INSERT INTO `LONG_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('LAST_KNOWN_POSITION', 1, '2021-01-14 10:24:47', 'Kolektory', 'ID poslednej známej pozície kolektorov', 'number');
INSERT INTO `LONG_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('LIGHT_THRESHOLD', 420, '2021-01-14 10:24:47', 'Kolektory', 'Počet sekúnd nepretržitého jasu, aby bola splnená podmienka pre stav jasu ', 'number');
INSERT INTO `LONG_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('OVERHEAT_POSITION', 2, '2021-01-14 10:24:47', 'Kolektory', 'ID pozície kolektorov v prípade prehriatia', 'number');
INSERT INTO `LONG_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('SOLAR_PANEL_DELAY', 2, '2021-01-14 10:24:47', 'Kolektory', 'Počet sekúnd pozastavenia otáčania kolektorov v prípade náhlej zmeny polohy', 'number');
INSERT INTO `LONG_SETTING` (REF_CD, VALUE, MODIFIED_TS, `GROUP`, DESCRIPTION, VALUE_TYPE) VALUES ('STRONG_WIND_POSITION', 3, '2021-01-14 10:24:47', 'Kolektory', 'ID pozície kolektorov v prípade silného vetra', 'number');
