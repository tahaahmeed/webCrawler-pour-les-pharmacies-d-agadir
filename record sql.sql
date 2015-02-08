CREATE TABLE IF NOT EXISTS `Record` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `URL` text NOT NULL,
   `lang` text ,
    `lati` text ,
     `nom` text NOT NULL,
      `adress` text NOT NULL,
       `tel` text NOT NULL,
  PRIMARY KEY (`RecordID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;