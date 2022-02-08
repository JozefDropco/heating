alter table solar_schedule
drop column SUN_RISE_ABS_POS_HOR;

alter table solar_schedule
drop column SUN_RISE_ABS_POS_VERT;

alter table solar_schedule
drop column SUN_SET_ABS_POS_HOR;

alter table solar_schedule
drop column SUN_SET_ABS_POS_VERT;


alter table solar_heating
drop column TO_TIME;

