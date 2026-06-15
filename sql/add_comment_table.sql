-- 创建订单评价表
CREATE TABLE IF NOT EXISTS `oms_order_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `order_item_id` bigint DEFAULT NULL COMMENT '订单项ID',
  `product_id` bigint DEFAULT NULL COMMENT '商品ID',
  `member_id` bigint DEFAULT NULL COMMENT '会员ID',
  `member_nick_name` varchar(255) DEFAULT NULL COMMENT '会员昵称',
  `star` int DEFAULT NULL COMMENT '评分(1-5)',
  `content` text COMMENT '评价内容',
  `pics` varchar(1000) DEFAULT NULL COMMENT '图片URL，逗号分隔',
  `show_status` int DEFAULT 1 COMMENT '显示状态(0隐藏/1显示)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品评价表';

-- 给oms_order_item添加is_commented字段（是否已评价）
ALTER TABLE `oms_order_item` ADD COLUMN `is_commented` int DEFAULT 0 COMMENT '是否已评价(0未评价/1已评价)' AFTER `product_attr`;
