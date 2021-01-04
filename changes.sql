DELETE FROM `SOLAR_POSITION` WHERE ID IN (
    select y.POS_ID
    FROM (SELECT s.ID,
                 s.MONTH,
                 s.DAY,
                 s.HOUR,
                 s.MINUTE,
                 p.ID as POS_ID,
                 p.HORIZONTAL,
                 p.VERTICAL
          FROM `SOLAR_SCHEDULE` s
                   JOIN `SOLAR_POSITION` p on p.ID = s.POSITION
         ) x
             JOIN (SELECT s.ID,
                          s.MONTH,
                          s.DAY,
                          s.HOUR,
                          s.MINUTE,
                          p.ID as POS_ID,
                          p.HORIZONTAL,
                          p.VERTICAL
                   FROM `SOLAR_SCHEDULE` s
                            JOIN `SOLAR_POSITION` p on p.ID = s.POSITION
    ) y ON x.ID = (y.ID - 1) AND x.MONTH = y.MONTH AND x.DAY = y.DAY and
           ((x.HORIZONTAL IS NULL and y.HORIZONTAL IS NULL) or (x.HORIZONTAL = y.HORIZONTAL))
        and ((x.VERTICAL IS NULL and y.VERTICAL IS NULL) or (x.VERTICAL = y.VERTICAL))
);

DELETE FROM `SOLAR_SCHEDULE` WHERE POSITION NOT IN (SELECT ID FROM `SOLAR_POSITION`);

UPDATE `SOLAR_POSITION` p JOIN `SOLAR_SCHEDULE` s ON p.ID=s.POSITION
    SET p.HORIZONTAL =
    (SELECT HORIZONTAL from `SOLAR_POSITION` p1 JOIN `SOLAR_SCHEDULE` s1 on s1.POSITION=p1.ID
    WHERE s1.MONTH=s.MONTH and s1.DAY=s.DAY and s1.ID<s.ID AND p1.HORIZONTAL IS NOT NULL ORDER BY s1.ID DESC LIMIT 1)

WHERE p.HORIZONTAL IS NULL ;

UPDATE `SOLAR_POSITION` p JOIN `SOLAR_SCHEDULE` s ON p.ID=s.POSITION
    SET p.VERTICAL =
    (SELECT VERTICAL from `SOLAR_POSITION` p1 JOIN `SOLAR_SCHEDULE` s1 on s1.POSITION=p1.ID
    WHERE s1.MONTH=s.MONTH and s1.DAY=s.DAY and s1.ID<s.ID AND p1.VERTICAL IS NOT NULL ORDER BY s1.ID DESC LIMIT 1)

WHERE p.VERTICAL IS NULL ;

DELETE FROM `SOLAR_POSITION` WHERE ID IN (
    select y.POS_ID
    FROM (SELECT s.ID,
                 s.MONTH,
                 s.DAY,
                 s.HOUR,
                 s.MINUTE,
                 p.ID as POS_ID,
                 p.HORIZONTAL,
                 p.VERTICAL
          FROM `SOLAR_SCHEDULE` s
                   JOIN `SOLAR_POSITION` p on p.ID = s.POSITION
         ) x
             JOIN (SELECT s.ID,
                          s.MONTH,
                          s.DAY,
                          s.HOUR,
                          s.MINUTE,
                          p.ID as POS_ID,
                          p.HORIZONTAL,
                          p.VERTICAL
                   FROM `SOLAR_SCHEDULE` s
                            JOIN `SOLAR_POSITION` p on p.ID = s.POSITION
    ) y ON x.ID = (y.ID - 1) AND x.MONTH = y.MONTH AND x.DAY = y.DAY and
           ((x.HORIZONTAL IS NULL and y.HORIZONTAL IS NULL) or (x.HORIZONTAL = y.HORIZONTAL))
        and ((x.VERTICAL IS NULL and y.VERTICAL IS NULL) or (x.VERTICAL = y.VERTICAL))
);

DELETE FROM `SOLAR_SCHEDULE` WHERE POSITION NOT IN (SELECT ID FROM `SOLAR_POSITION`);

UPDATE `SOLAR_POSITION` SET HORIZONTAL = 0, VERTICAL=-135 WHERE ID=2;

UPDATE `SOLAR_POSITION` SET HORIZONTAL = null, VERTICAL=0 WHERE ID=3;
