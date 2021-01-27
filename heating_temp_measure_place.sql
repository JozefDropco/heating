create table temp_measure_place
(
    PLACE_REF_CD varchar(255) not null,
    DEVICE_ID    varchar(255) not null,
    NAME         varchar(255) not null
)
    charset = utf8mb4;

INSERT INTO temp_measure_place (PLACE_REF_CD, DEVICE_ID, NAME) VALUES ('EXT_TEMP', '23849284', 'Vonkajšia teplota');
INSERT INTO temp_measure_place (PLACE_REF_CD, DEVICE_ID, NAME) VALUES ('TA3', '28-01142f9f7b9a', 'Tatramat 300L');
INSERT INTO temp_measure_place (PLACE_REF_CD, DEVICE_ID, NAME) VALUES ('SOLAR', 'solar', 'Kolektory');
INSERT INTO temp_measure_place (PLACE_REF_CD, DEVICE_ID, NAME) VALUES ('PRED_TA3', 'PRED_TA3', 'PRED_TA3');
