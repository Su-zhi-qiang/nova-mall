-- 为oms_order_return_apply表添加delete_status字段，支持软删除
ALTER TABLE `oms_order_return_apply` ADD COLUMN `delete_status` int DEFAULT 0 COMMENT '删除状态：0->未删除；1->已删除' AFTER `receive_note`;
