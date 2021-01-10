CREATE TABLE `STATS` (
                         `ID` int(11) NOT NULL AUTO_INCREMENT,
                         `NAME` varchar(100) DEFAULT NULL,
                         `FROM_DATE` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
                         `TO_DATE` timestamp NULL DEFAULT NULL,
                         PRIMARY KEY (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `STRING_SETTING` (REF_CD, VALUE, MODIFIED_TS) VALUES ('LAST_HEARTBEAT', '10. 01. 2021 12:55:22 CET', '2021-01-10 12:55:19');
