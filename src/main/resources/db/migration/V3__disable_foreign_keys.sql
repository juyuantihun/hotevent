-- 禁用外键约束
SET FOREIGN_KEY_CHECKS = 0;

-- 删除timeline_region表中的外键约束
ALTER TABLE `timeline_region` DROP FOREIGN KEY `fk_timeline_region_timeline`;
ALTER TABLE `timeline_region` DROP FOREIGN KEY `fk_timeline_region_region`;

-- 删除timeline_event表中的外键约束
ALTER TABLE `timeline_event` DROP FOREIGN KEY `fk_timeline_event_timeline`;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;