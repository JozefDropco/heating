create table `APP_LOG`
(
    ID integer auto_increment,
    SEQ_ID integer not null,
    LOG_LEVEL varchar(10) not null,
    MSG_DATE timestamp not null,
    MESSAGE varchar(256) null CHARACTER SET utf8,
    constraint APP_LOG_PK
        primary key (ID)
);

create index APP_LOG_logLevel_index
	on `APP_LOG` (LOG_LEVEL);

INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS) VALUES ('CIRCULAR_PUMP_PORT', 'GPIO 7', '2021-01-10 12:55:19');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS) VALUES ('THREE_WAY_PORT', 'GPIO 8', '2021-01-10 12:55:19');
INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS) VALUES ('BOILER_PORT', 'GPIO 9', '2021-01-10 12:55:19');

