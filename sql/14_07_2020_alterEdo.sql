ALTER TABLE `edo`
	ADD COLUMN `sztp` VARCHAR(10) NULL DEFAULT NULL COMMENT 'Size / Type' COLLATE 'utf8_bin' AFTER `voy_no`;