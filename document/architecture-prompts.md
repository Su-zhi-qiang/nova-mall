# 架构图AI绘图提示词

---

## 1. 数据库 ER 图（完整版 - 78张表）

请生成一张电商系统数据库 ER 图，包含以下全部 78 张表，按业务域分组：

### PMS 商品管理（16张表）
- pms_product (商品主表)
- pms_product_attribute (商品属性)
- pms_product_attribute_category (属性分类)
- pms_product_attribute_value (属性值)
- pms_product_category (商品分类)
- pms_product_category_attribute_relation (分类属性关联)
- pms_product_full_reduction (满减)
- pms_product_ladder (阶梯价格)
- pms_product_operate_log (操作日志)
- pms_product_vertify_record (审核记录)
- pms_brand (品牌)
- pms_sku_stock (SKU库存)
- pms_album (相册)
- pms_album_pic (相册图片)
- pms_comment (评论)
- pms_comment_replay (评论回复)
- pms_feight_template (运费模板)
- pms_member_price (会员价格)

### OMS 订单管理（8张表）
- oms_order (订单主表)
- oms_order_item (订单项)
- oms_order_comment (订单评价)
- oms_order_operate_history (操作历史)
- oms_order_return_apply (退货申请)
- oms_order_return_reason (退货原因)
- oms_order_setting (订单设置)
- oms_cart_item (购物车)
- oms_company_address (公司地址)

### SMS 营销管理（12张表）
- sms_coupon (优惠券)
- sms_coupon_history (领取记录)
- sms_coupon_product_relation (优惠券商品关联)
- sms_coupon_product_category_relation (优惠券分类关联)
- sms_flash_promotion (秒杀活动)
- sms_flash_promotion_session (秒杀场次)
- sms_flash_promotion_product_relation (秒杀商品关联)
- sms_flash_promotion_daily_stock (每日库存快照)
- sms_flash_promotion_log (秒杀日志)
- sms_home_advertise (广告)
- sms_home_brand (品牌推荐)
- sms_home_new_product (新品推荐)
- sms_home_recommend_product (热销推荐)
- sms_home_recommend_subject (专题推荐)

### UMS 用户管理（23张表）
- ums_admin (管理员)
- ums_admin_login_log (登录日志)
- ums_admin_role_relation (管理员角色关联)
- ums_admin_permission_relation (管理员权限关联)
- ums_role (角色)
- ums_role_menu_relation (角色菜单关联)
- ums_role_permission关系)
- ums_role_menu_relation (角色菜单关联)
- ums_role_permission_relation (角色权限关联)
- ums_role_resource_relation (角色资源关联)
- ums_menu (菜单)
- ums_permission (权限)
- ums_resource (资源)
- ums_resource_category (资源分类)
- ums_member (会员)
- ums_member_level (会员等级)
- ums_member_login_log (会员登录日志)
- ums_member_receive_address (收货地址)
- ums_member_product_category_relation (会员分类关联)
- ums_member_member_tag_relation (会员标签关联)
- ums_member_tag (会员标签)
- ums_member_statistics_info (统计信息)
- ums_member_task (会员任务)
- ums_member_rule_setting (规则设置)
- ums_member_integration_change_history (积分变更)
- ums_growth_change_history (成长值变更)
- ums_integration_consume_setting (积分消费设置)

### CMS 内容管理（12张表）
- cms_help (帮助)
- cms_help_category (帮助分类)
- cms_member_report (用户举报)
- cms_prefrence_area (优选专区)
- cms_prefrence_area_product_relation (专区商品关联)
- cms_subject (专题)
- cms_subject_category (专题分类)
- cms_subject_comment (专题评论)
- cms_subject_product_relation (专题商品关联)
- cms_topic (话题)
- cms_topic_category (话题分类)
- cms_topic_comment (话题评论)

### 核心关联关系
- pms_product ↔ pms_brand (多对一)
- pms_product ↔ pms_product_category (多对一)
- pms_product ↔ pms_sku_stock (一对多)
- oms_order ↔ oms_order_item (一对多)
- oms_order ↔ ums_member (多对一)
- sms_coupon ↔ sms_coupon_history (一对多)
- sms_flash_promotion ↔ sms_flash_promotion_session (一对多)
- ums_admin ↔ ums_role (多对多，通过关联表)
- ums_role ↔ ums_resource (多对多，通过关联表)

风格：专业数据库建模风格，类似 PowerDesigner 输出，白底，用线条表示表间关联关系，按业务域分区布局。

---

## 2. 订单责任链流程图

请生成一张订单生成责任链流程图，展示以下 Handler 执行顺序：

**下单流程（13个Handler）**：
ValidateAddress → LoadCart → FlashValidation → BuildOrderItems → CheckStock → HandleCoupon → HandleIntegration → HandleRealAmount → LockStock → BuildOrder → PersistOrder → PostOrder

**支付流程（6个Handler）**：
LoadOrderDetail → UpdatePaymentStatus → DeductStock → UpdateSales → UpdateCouponStatusForPay

**取消流程（5个Handler）**：
LoadOrder → LoadAndValidateCancelOrder → UpdateCancelStatus → RestoreStockForCancel → RestoreCouponForCancel → ReturnIntegration

风格：技术流程图，每个 Handler 用圆角矩形表示，箭头连接表示执行顺序，风格简洁专业，白底，三个流程可用不同颜色区分。

---

## 3. 促销策略类图

请生成一张 UML 类图，展示促销策略模式设计：

**策略接口层**：
- PromotionStrategy (apply 方法)
- PromotionStrategyFactory (getStrategy 方法)
- PromotionContext

**5 个具体策略实现**：
- NoPromotionStrategy (无优惠)
- SingleItemPromotionStrategy (单品促销)
- LadderDiscountPromotionStrategy (阶梯折扣)
- FullReductionPromotionStrategy (满减)
- FlashPromotionStrategy (秒杀)

**优惠券策略**：
- CouponScopeStrategy (策略接口)
- CouponScopeStrategyFactory (策略工厂)
- AllProductCouponScopeStrategy (全场通用)
- CategoryCouponScopeStrategy (指定分类)
- ProductCouponScopeStrategy (指定商品)

**支付策略**：
- PaymentStrategy (策略接口)
- PaymentStrategyFactory (策略工厂)
- AlipayPaymentStrategy (支付宝)

风格：标准 UML 类图，白底，类名清晰，箭头表示继承/实现关系，虚线箭头表示依赖。
