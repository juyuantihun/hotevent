/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : localhost:3306
 Source Schema         : hot_events_db

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 15/07/2025 08:21:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for country
-- ----------------------------
DROP TABLE IF EXISTS `country`;
CREATE TABLE `country`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '国家名称',
  `short_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '简称',
  `population` bigint(20) NULL DEFAULT NULL COMMENT '人口',
  `area` double NULL DEFAULT NULL COMMENT '面积（平方公里）',
  `capital` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '首都',
  `language` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '官方语言',
  `currency` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '货币',
  `created_at` datetime(0) NULL DEFAULT NULL,
  `updated_at` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '国家实体表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of country
-- ----------------------------
INSERT INTO `country` VALUES (1, '中华人民共和国', '中国', 1400000000, 9600000, '北京', '中文', '人民币', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (2, '美利坚合众国', '美国', 330000000, 9834000, '华盛顿', '英语', '美元', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (3, '俄罗斯联邦', '俄罗斯', 146000000, 17100000, '莫斯科', '俄语', '卢布', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (4, '日本国', '日本', 125000000, 378000, '东京', '日语', '日元', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (5, '大韩民国', '韩国', 52000000, 100000, '首尔', '韩语', '韩元', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (6, '朝鲜民主主义人民共和国', '朝鲜', 26000000, 121000, '平壤', '朝鲜语', '朝鲜元', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (7, '德意志联邦共和国', '德国', 83000000, 358000, '柏林', '德语', '欧元', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (8, '法兰西共和国', '法国', 68000000, 644000, '巴黎', '法语', '欧元', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (9, '大不列颠及北爱尔兰联合王国', '英国', 67000000, 244000, '伦敦', '英语', '英镑', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (10, '以色列国', '以色列', 9000000, 21000, '耶路撒冷', '希伯来语', '以色列谢克尔', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (11, '伊朗伊斯兰共和国', '伊朗', 84000000, 1648000, '德黑兰', '波斯语', '伊朗里亚尔', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (12, '乌克兰', '乌克兰', 44000000, 604000, '基辅', '乌克兰语', '格里夫纳', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (13, '加拿大', '加拿大', 38000000, 9985000, '渥太华', '英语/法语', '加元', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (14, '澳大利亚联邦', '澳大利亚', 26000000, 7692000, '堪培拉', '英语', '澳元', '2025-07-10 06:59:47', '2025-07-10 06:59:47');
INSERT INTO `country` VALUES (15, '印度共和国', '印度', 1380000000, 3287000, '新德里', '印地语/英语', '印度卢比', '2025-07-10 06:59:47', '2025-07-10 06:59:47');

-- ----------------------------
-- Table structure for dictionary
-- ----------------------------
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  `dict_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型：country-国家，region-地区，city-城市，event_type-事件类型，subject-主体，object-客体',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称',
  `dict_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '字典描述',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父级ID',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_auto_added` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动添加：0-否，1-是',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `created_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实体类型',
  `entity_id` bigint(20) NULL DEFAULT NULL COMMENT '实体ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type_code`(`dict_type`, `dict_code`) USING BTREE,
  INDEX `idx_dict_type`(`dict_type`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 115 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dictionary
-- ----------------------------
INSERT INTO `dictionary` VALUES (41, '国家', 'CN', '中国', '中华人民共和国', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 06:59:47', 'system', 'system', 'country', 1);
INSERT INTO `dictionary` VALUES (42, '国家', 'US', '美国', '美利坚合众国', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 06:59:47', 'system', 'system', 'country', 2);
INSERT INTO `dictionary` VALUES (43, '国家', 'RU', '俄罗斯', '俄罗斯联邦', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 06:59:47', 'system', 'system', 'country', 3);
INSERT INTO `dictionary` VALUES (44, '国家', 'JP', '日本', '日本国', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 06:59:47', 'system', 'system', 'country', 4);
INSERT INTO `dictionary` VALUES (45, '国家', 'KR', '韩国', '大韩民国', 0, 5, 1, 0, '2025-07-10 09:31:06', '2025-07-10 06:59:47', 'system', 'system', 'country', 5);
INSERT INTO `dictionary` VALUES (46, '国家', 'IL', '以色列', '以色列国', 0, 6, 1, 0, '2025-07-10 09:31:06', '2025-07-10 06:59:47', 'system', 'system', 'country', 10);
INSERT INTO `dictionary` VALUES (47, '国家', 'IR', '伊朗', '伊朗伊斯兰共和国', 0, 7, 1, 0, '2025-07-10 09:31:06', '2025-07-10 06:59:47', 'system', 'system', 'country', 11);
INSERT INTO `dictionary` VALUES (48, '国家', 'LB', '黎巴嫩', '黎巴嫩共和国', 0, 8, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (49, '地区', 'ASIA', '亚洲', '亚洲地区', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (50, '地区', 'EUROPE', '欧洲', '欧洲地区', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (51, '地区', 'NORTH_AMERICA', '北美洲', '北美洲地区', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (52, '地区', 'MIDDLE_EAST', '中东', '中东地区', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (53, '城市', 'BEIJING', '北京', '中国首都', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (54, '城市', 'WASHINGTON', '华盛顿', '美国首都', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (55, '城市', 'MOSCOW', '莫斯科', '俄罗斯首都', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (56, '城市', 'TOKYO', '东京', '日本首都', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (57, '事件类型', 'MILITARY_ATTACK', '军事攻击', '军事攻击事件', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (58, '事件类型', 'POLITICAL_NEGOTIATE', '政治谈判', '政治谈判事件', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (59, '事件类型', 'ECONOMIC_SANCTION', '经济制裁', '经济制裁事件', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (60, '事件类型', 'TERRORISM', '恐怖主义', '恐怖主义事件', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (61, '事件主体', 'GOVERNMENT', '政府', '政府机构', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (62, '事件主体', 'MILITARY', '军队', '军事组织', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (63, '事件主体', 'TERRORIST_GROUP', '恐怖组织', '恐怖主义组织', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (64, '事件主体', 'INTERNATIONAL_ORG', '国际组织', '国际组织', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (65, '事件客体', 'CIVILIAN_TARGET', '平民目标', '平民目标', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (66, '事件客体', 'MILITARY_FACILITY', '军事设施', '军事设施目标', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (67, '事件客体', 'INFRASTRUCTURE', '基础设施', '基础设施目标', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (68, '事件客体', 'GOVERNMENT_BUILDING', '政府建筑', '政府建筑目标', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (69, '关联关系类型', 'CAUSE', '导致', '因果关系', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (70, '关联关系类型', 'TRIGGER', '触发', '触发关系', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (71, '关联关系类型', 'RETALIATE', '报复', '报复关系', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (72, '关联关系类型', 'RESPOND', '回应', '回应关系', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (73, '来源类型', 'AUTO_FETCH', '自动获取', '自动获取数据', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (74, '来源类型', 'MANUAL_INPUT', '手动输入', '手动输入数据', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (75, '来源类型', 'IMPORT', '导入', '批量导入数据', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (76, '来源类型', 'THIRD_PARTY', '第三方API', '第三方API数据', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (77, '事件状态', 'DISABLED', '禁用', '禁用状态', 0, 1, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (78, '事件状态', 'ENABLED', '启用', '启用状态', 0, 2, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (79, '事件状态', 'DRAFT', '草稿', '草稿状态', 0, 3, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (80, '事件状态', 'REVIEWING', '审核中', '审核中状态', 0, 4, 1, 0, '2025-07-10 09:31:06', '2025-07-10 09:31:06', 'system', 'system', NULL, NULL);
INSERT INTO `dictionary` VALUES (81, '关系类型', 'neighbor', '邻国', '国家与国家之间的邻居关系', 0, 1, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (82, '关系类型', 'ally', '盟友', '国家与国家之间的盟友关系', 0, 2, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (83, '关系类型', 'support', '支持', '国家与国家之间的支持关系', 0, 3, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (84, '关系类型', 'capital', '首都', '国家与城市之间的首都关系', 0, 4, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (85, '关系类型', 'nationality', '国籍', '国家与人物之间的国籍关系', 0, 5, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (86, '关系类型', 'wanted', '通缉', '国家与人物之间的通缉关系', 0, 6, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (87, '关系类型', 'founder', '创始人', '组织与人物之间的创始人关系', 0, 7, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (88, '关系类型', 'member', '隶属', '组织与人物之间的隶属关系', 0, 8, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (89, '关系类型', 'leader', '领导', '组织与人物之间的领导关系', 0, 9, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (90, '关系类型', 'employee', '雇佣', '组织与人物之间的雇佣关系', 0, 10, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (91, '关系类型', 'cooperation', '合作', '组织与组织之间的合作关系', 0, 11, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (92, '关系类型', 'competition', '竞争', '组织与组织之间的竞争关系', 0, 12, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (93, '关系类型', 'subsidiary', '子公司', '组织与组织之间的子公司关系', 0, 13, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (94, '关系类型', 'partner', '合伙', '人物与人物之间的合伙关系', 0, 14, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (95, '关系类型', 'family', '家庭', '人物与人物之间的家庭关系', 0, 15, 1, 0, '2025-07-10 03:59:36', '2025-07-10 03:59:36', NULL, NULL, NULL, NULL);
INSERT INTO `dictionary` VALUES (96, '组织', 'UN', '联合国', '联合国', 0, 1, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'organization', 1);
INSERT INTO `dictionary` VALUES (97, '组织', 'NATO', '北约', '北大西洋公约组织', 0, 2, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'organization', 2);
INSERT INTO `dictionary` VALUES (98, '组织', 'EU', '欧盟', '欧洲联盟', 0, 3, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'organization', 3);
INSERT INTO `dictionary` VALUES (99, '组织', 'CPC', '中国共产党', '中国共产党', 0, 4, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'organization', 5);
INSERT INTO `dictionary` VALUES (100, '组织', 'Hamas', '哈马斯', '巴勒斯坦伊斯兰抵抗运动', 0, 5, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'organization', 8);
INSERT INTO `dictionary` VALUES (101, '人物', 'xi_jinping', '习近平', '中华人民共和国主席', 0, 1, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'person', 1);
INSERT INTO `dictionary` VALUES (102, '人物', 'biden', '拜登', '美国总统', 0, 2, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'person', 2);
INSERT INTO `dictionary` VALUES (103, '人物', 'putin', '普京', '俄罗斯总统', 0, 3, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'person', 3);
INSERT INTO `dictionary` VALUES (104, '人物', 'netanyahu', '内塔尼亚胡', '以色列总理', 0, 4, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'person', 8);
INSERT INTO `dictionary` VALUES (105, '人物', 'khamenei', '哈梅内伊', '伊朗最高领袖', 0, 5, 1, 0, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', 'person', 9);
INSERT INTO `dictionary` VALUES (114, '关系类型', 'strike', '军事打击', '', 0, 16, 1, 0, '2025-07-14 15:01:32', '2025-07-14 15:01:32', 'system', 'system', NULL, NULL);

-- ----------------------------
-- Table structure for entity_relationship
-- ----------------------------
DROP TABLE IF EXISTS `entity_relationship`;
CREATE TABLE `entity_relationship`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `source_entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源实体类型（country/organization/person）',
  `source_entity_id` bigint(20) NOT NULL COMMENT '源实体ID',
  `target_entity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标实体类型（country/organization/person）',
  `target_entity_id` bigint(20) NOT NULL COMMENT '目标实体ID',
  `relationship_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关系类型',
  `relationship_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '关系描述',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_source`(`source_entity_type`, `source_entity_id`) USING BTREE,
  INDEX `idx_target`(`target_entity_type`, `target_entity_id`) USING BTREE,
  INDEX `idx_relationship_type`(`relationship_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '实体关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of entity_relationship
-- ----------------------------
INSERT INTO `entity_relationship` VALUES (1, 'country', 1, 'country', 3, '邻国', '中国与俄罗斯是邻国关系', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (2, 'country', 1, 'country', 6, '邻国', '中国与朝鲜是邻国关系', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (3, 'country', 2, 'country', 4, '盟友', '美国与日本是盟友关系', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (4, 'country', 2, 'country', 5, '盟友', '美国与韩国是盟友关系', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (5, 'country', 10, 'country', 2, '盟友', '以色列与美国是盟友关系', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (6, 'country', 11, 'country', 3, '支持', '伊朗得到俄罗斯支持', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (7, 'country', 7, 'country', 8, '邻国', '德国与法国是邻国关系', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (8, 'country', 9, 'country', 8, '邻国', '英国与法国是邻国关系', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (9, 'country', 13, 'country', 2, '邻国', '加拿大与美国是邻国关系', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (10, 'country', 1, 'person', 1, '国籍', '习近平具有中国国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (11, 'country', 2, 'person', 2, '国籍', '拜登具有美国国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (12, 'country', 3, 'person', 3, '国籍', '普京具有俄罗斯国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (13, 'country', 4, 'person', 4, '国籍', '岸田文雄具有日本国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (14, 'country', 5, 'person', 5, '国籍', '尹锡悦具有韩国国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (15, 'country', 6, 'person', 6, '国籍', '金正恩具有朝鲜国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (16, 'country', 12, 'person', 7, '国籍', '泽连斯基具有乌克兰国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (17, 'country', 10, 'person', 8, '国籍', '内塔尼亚胡具有以色列国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (18, 'country', 11, 'person', 9, '国籍', '哈梅内伊具有伊朗国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (19, 'country', 8, 'person', 10, '国籍', '马克龙具有法国国籍', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (20, 'organization', 5, 'person', 1, '领导', '习近平领导中国共产党', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (21, 'organization', 6, 'person', 2, '隶属', '拜登隶属于美国国务院系统', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (22, 'organization', 7, 'person', 3, '领导', '普京领导俄罗斯联邦政府', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (23, 'organization', 13, 'person', 4, '隶属', '岸田文雄隶属于自由民主党', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (24, 'organization', 10, 'person', 9, '隶属', '哈梅内伊隶属于伊斯兰革命卫队', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (25, 'organization', 15, 'person', 11, '隶属', '朔尔茨隶属于德国联邦议院', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (26, 'organization', 14, 'person', 12, '隶属', '苏纳克隶属于英国工党', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (27, 'organization', 2, 'organization', 3, '合作', '北约与欧盟在防务方面合作', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (28, 'organization', 8, 'organization', 9, '合作', '哈马斯与真主党在中东地区合作', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (29, 'organization', 1, 'organization', 4, '合作', '联合国与世贸组织在国际事务中合作', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (30, 'organization', 11, 'organization', 12, '合作', 'APEC与上海合作组织在亚太地区合作', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (31, 'country', 2, 'organization', 2, '成员', '美国是北约成员', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (32, 'country', 8, 'organization', 3, '成员', '法国是欧盟成员', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (33, 'country', 9, 'organization', 2, '成员', '英国是北约成员', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (34, 'country', 7, 'organization', 3, '成员', '德国是欧盟成员', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (35, 'country', 1, 'organization', 12, '成员', '中国是上海合作组织成员', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (36, 'country', 3, 'organization', 12, '成员', '俄罗斯是上海合作组织成员', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (37, 'country', 4, 'organization', 11, '成员', '日本是APEC成员', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `entity_relationship` VALUES (38, 'country', 2, 'organization', 11, '成员', '美国是APEC成员', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');

-- ----------------------------
-- Table structure for event
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `event_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件编码',
  `event_time` datetime(0) NOT NULL COMMENT '事件发生时间',
  `event_location` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事件地点',
  `event_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事件类型',
  `event_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '事件描述',
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事件主体',
  `object` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '事件客体',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `source_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '来源类型：1-自动获取，2-手动录入',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `created_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  `relation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关系类型',
  `relation_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关系名称',
  `intensity_level` int(11) NULL DEFAULT 1 COMMENT '强度等级(1-5)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_event_code`(`event_code`) USING BTREE,
  INDEX `idx_event_time`(`event_time`) USING BTREE,
  INDEX `idx_event_type`(`event_type`) USING BTREE,
  INDEX `idx_subject`(`subject`) USING BTREE,
  INDEX `idx_object`(`object`) USING BTREE,
  INDEX `idx_location`(`longitude`, `latitude`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 167 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '事件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of event
-- ----------------------------
INSERT INTO `event` VALUES (1, 'EVT_001', '2024-01-15 14:30:00', '加沙地带', '冲突', '以色列军队对加沙地带进行军事行动', '以色列', '哈马斯', 34.4668000, 31.5017000, 1, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', NULL, NULL, 1);
INSERT INTO `event` VALUES (2, 'EVT_002', '2024-01-16 09:15:00', '华盛顿特区', '外交', '美国国务卿与中国外交部长举行会谈', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'deepseek', 'deepseek', NULL, NULL, 1);
INSERT INTO `event` VALUES (3, 'EVT_003', '2024-01-17 16:45:00', '乌克兰东部', '冲突', '俄乌边境地区发生武装冲突', '俄罗斯', '乌克兰', 37.6173000, 55.7558000, 1, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', NULL, NULL, 1);
INSERT INTO `event` VALUES (4, 'EVT_004', '2024-01-18 11:20:00', '联合国总部', '外交', '联合国安理会就中东问题举行紧急会议', '联合国', '中东各国', -73.9665000, 40.7489000, 2, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'deepseek', 'deepseek', NULL, NULL, 1);
INSERT INTO `event` VALUES (5, 'EVT_005', '2024-01-19 20:30:00', '德黑兰', '制裁', '伊朗宣布暂停执行核协议部分条款', '伊朗', '国际社会', 51.3890000, 35.6892000, 1, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', NULL, NULL, 1);
INSERT INTO `event` VALUES (6, 'EVT_006', '2024-01-20 08:45:00', '平壤', '外交', '朝鲜与俄罗斯签署军事合作协议', '朝鲜', '俄罗斯', 125.7625000, 39.0392000, 2, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'deepseek', 'deepseek', NULL, NULL, 1);
INSERT INTO `event` VALUES (7, 'EVT_007', '2024-01-21 15:10:00', '台海', '冲突', '台海地区紧张局势升级', '中国', '台湾', 120.9605000, 23.6978000, 1, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', NULL, NULL, 1);
INSERT INTO `event` VALUES (8, 'EVT_008', '2024-01-22 12:00:00', '布鲁塞尔', '制裁', '欧盟宣布对俄罗斯实施新一轮制裁', '欧盟', '俄罗斯', 4.3517000, 50.8503000, 2, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'deepseek', 'deepseek', NULL, NULL, 1);
INSERT INTO `event` VALUES (9, 'EVT_009', '2024-01-23 19:30:00', '首尔', '外交', '韩美日三方领导人举行峰会', '韩国', '美国', 126.9780000, 37.5665000, 1, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin', NULL, NULL, 1);
INSERT INTO `event` VALUES (10, 'EVT_010', '2024-01-24 10:15:00', '东京', '经济', '日本央行调整货币政策', '日本', '全球市场', 139.6917000, 35.6895000, 2, 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'deepseek', 'deepseek', NULL, NULL, 1);
INSERT INTO `event` VALUES (13, 'MANUAL_1752132714368_0', '2025-07-09 21:31:54', '华盛顿', '冲突', '美国冲突朝鲜', '美国', '朝鲜', 108.3695039, 23.1320055, 2, 1, '2025-07-10 15:31:55', '2025-07-10 15:31:55', 'manual_fetch', 'manual_fetch', NULL, NULL, 1);
INSERT INTO `event` VALUES (14, 'MANUAL_1752132714641_1', '2025-07-10 09:31:54', '巴黎', '外交', '日本外交朝鲜', '日本', '朝鲜', 97.4442923, -49.5011101, 2, 1, '2025-07-10 15:31:55', '2025-07-10 15:31:55', 'manual_fetch', 'manual_fetch', NULL, NULL, 1);
INSERT INTO `event` VALUES (15, 'MANUAL_1752132714676_2', '2025-07-09 20:31:54', '柏林', '制裁', '德国制裁乌克兰', '德国', '乌克兰', 173.6747444, 34.8330455, 2, 1, '2025-07-10 15:31:55', '2025-07-10 15:31:55', 'manual_fetch', 'manual_fetch', NULL, NULL, 1);
INSERT INTO `event` VALUES (16, 'DEEPSEEK_1752132880985_0', '2025-07-09 19:34:41', '伦敦', '谈判', '英国谈判土耳其', '英国', '土耳其', -31.3694554, -21.9445288, 2, 1, '2025-07-10 15:34:41', '2025-07-10 15:34:41', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (17, 'DEEPSEEK_1752132881250_1', '2025-07-10 11:34:41', '华盛顿', '外交', '中国外交朝鲜', '中国', '朝鲜', -10.2165122, 31.6045676, 2, 1, '2025-07-10 15:34:41', '2025-07-10 15:34:41', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (18, 'DEEPSEEK_1752132881271_2', '2025-07-10 10:34:41', '伦敦', '外交', '美国外交朝鲜', '美国', '朝鲜', 134.0120068, 15.8618199, 2, 1, '2025-07-10 15:34:41', '2025-07-10 15:34:41', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (19, 'DEEPSEEK_1752132881290_3', '2025-07-10 14:34:41', '东京', '谈判', '中国谈判台湾', '中国', '台湾', -138.8253119, 13.7699319, 2, 1, '2025-07-10 15:34:41', '2025-07-10 15:34:41', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (20, 'DEEPSEEK_1752132881310_4', '2025-07-10 15:34:41', '巴黎', '谈判', '中国谈判乌克兰', '中国', '乌克兰', -3.0543166, -66.5156811, 2, 1, '2025-07-10 15:34:41', '2025-07-10 15:34:41', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (21, 'DEEPSEEK_1752133238365_0', '2025-07-08 12:40:38', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-10 15:40:39', '2025-07-10 15:40:39', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (22, 'DEEPSEEK_1752133238593_1', '2025-07-09 11:40:38', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-10 15:40:39', '2025-07-10 15:40:39', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (23, 'DEEPSEEK_1752133238622_2', '2025-07-08 00:40:38', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-10 15:40:39', '2025-07-10 15:40:39', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (24, 'DEEPSEEK_1752133238649_3', '2025-07-08 01:40:38', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-10 15:40:39', '2025-07-10 15:40:39', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (25, 'DEEPSEEK_1752133238675_4', '2025-07-10 02:40:38', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-10 15:40:39', '2025-07-10 15:40:39', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (26, 'DEEPSEEK_1752133275850_0', '2025-07-09 01:41:16', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-10 15:41:16', '2025-07-10 15:41:16', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (27, 'DEEPSEEK_1752133276132_1', '2025-07-10 04:41:16', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-10 15:41:16', '2025-07-10 15:41:16', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (28, 'DEEPSEEK_1752133276161_2', '2025-07-08 07:41:16', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-10 15:41:16', '2025-07-10 15:41:16', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (29, 'DEEPSEEK_1752133276191_3', '2025-07-08 05:41:16', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-10 15:41:16', '2025-07-10 15:41:16', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (30, 'DEEPSEEK_1752133276217_4', '2025-07-08 05:41:16', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-10 15:41:16', '2025-07-10 15:41:16', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (31, 'MANUAL_1752134513840_0', '2025-07-08 16:01:54', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-10 16:01:54', '2025-07-10 16:01:54', 'manual_fetch', 'manual_fetch', NULL, NULL, 1);
INSERT INTO `event` VALUES (32, 'DEEPSEEK_1752136876563_0', '2025-07-10 07:41:17', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-10 16:41:17', '2025-07-10 16:41:17', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (33, 'DEEPSEEK_1752136876606_1', '2025-07-08 21:41:17', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-10 16:41:17', '2025-07-10 16:41:17', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (34, 'DEEPSEEK_1752136876626_2', '2025-07-09 18:41:17', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-10 16:41:17', '2025-07-10 16:41:17', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (35, 'DEEPSEEK_1752136876652_3', '2025-07-10 05:41:17', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-10 16:41:17', '2025-07-10 16:41:17', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (36, 'DEEPSEEK_1752136876677_4', '2025-07-09 17:41:17', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-10 16:41:17', '2025-07-10 16:41:17', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (37, 'DEEPSEEK_1752137156010_0', '2025-07-09 04:45:56', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-10 16:45:56', '2025-07-10 16:45:56', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (38, 'DEEPSEEK_1752137156398_1', '2025-07-08 22:45:56', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-10 16:45:56', '2025-07-10 16:45:56', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (39, 'DEEPSEEK_1752137156428_2', '2025-07-08 09:45:56', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-10 16:45:56', '2025-07-10 16:45:56', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (40, 'DEEPSEEK_1752137156458_3', '2025-07-08 07:45:56', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-10 16:45:56', '2025-07-10 16:45:56', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (41, 'DEEPSEEK_1752137156482_4', '2025-07-10 11:45:56', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-10 16:45:56', '2025-07-10 16:45:56', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (42, 'DEEPSEEK_1752193513700_0', '2025-07-10 00:25:14', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 08:25:33', '2025-07-11 08:25:33', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (43, 'DEEPSEEK_1752193532696_1', '2025-07-09 16:25:14', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 08:25:33', '2025-07-11 08:25:33', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (44, 'DEEPSEEK_1752193532730_2', '2025-07-09 04:25:14', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 08:25:33', '2025-07-11 08:25:33', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (45, 'DEEPSEEK_1752193532799_3', '2025-07-10 15:25:14', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 08:25:33', '2025-07-11 08:25:33', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (46, 'DEEPSEEK_1752193532826_4', '2025-07-11 05:25:14', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 08:25:33', '2025-07-11 08:25:33', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (47, 'DEEPSEEK_1752194727969_0', '2025-07-09 09:45:28', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 08:45:28', '2025-07-11 08:45:28', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (48, 'DEEPSEEK_1752194728226_1', '2025-07-09 12:45:28', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 08:45:28', '2025-07-11 08:45:28', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (49, 'DEEPSEEK_1752194728249_2', '2025-07-10 18:45:28', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 08:45:28', '2025-07-11 08:45:28', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (50, 'DEEPSEEK_1752194728270_3', '2025-07-11 04:45:28', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 08:45:28', '2025-07-11 08:45:28', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (51, 'DEEPSEEK_1752194728296_4', '2025-07-09 00:45:28', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 08:45:28', '2025-07-11 08:45:28', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (52, 'DEEPSEEK_1752200406160_0', '2025-07-10 01:20:06', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 10:20:06', '2025-07-11 10:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (53, 'DEEPSEEK_1752200406514_1', '2025-07-10 23:20:06', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 10:20:07', '2025-07-11 10:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (54, 'DEEPSEEK_1752200406540_2', '2025-07-09 15:20:06', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 10:20:07', '2025-07-11 10:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (55, 'DEEPSEEK_1752200406574_3', '2025-07-10 23:20:06', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 10:20:07', '2025-07-11 10:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (56, 'DEEPSEEK_1752200406601_4', '2025-07-10 08:20:06', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 10:20:07', '2025-07-11 10:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (57, 'DEEPSEEK_1752204005650_0', '2025-07-08 12:20:06', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 11:20:06', '2025-07-11 11:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (58, 'DEEPSEEK_1752204005686_1', '2025-07-09 11:20:06', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 11:20:06', '2025-07-11 11:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (59, 'DEEPSEEK_1752204005702_2', '2025-07-09 15:20:06', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 11:20:06', '2025-07-11 11:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (60, 'DEEPSEEK_1752204005741_3', '2025-07-10 18:20:06', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 11:20:06', '2025-07-11 11:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (61, 'DEEPSEEK_1752204005772_4', '2025-07-11 01:20:06', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 11:20:06', '2025-07-11 11:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (62, 'DEEPSEEK_1752207605631_0', '2025-07-09 15:20:06', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 12:20:06', '2025-07-11 12:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (63, 'DEEPSEEK_1752207605658_1', '2025-07-10 14:20:06', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 12:20:06', '2025-07-11 12:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (64, 'DEEPSEEK_1752207605672_2', '2025-07-10 12:20:06', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 12:20:06', '2025-07-11 12:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (65, 'DEEPSEEK_1752207605684_3', '2025-07-10 13:20:06', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 12:20:06', '2025-07-11 12:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (66, 'DEEPSEEK_1752207605696_4', '2025-07-10 03:20:06', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 12:20:06', '2025-07-11 12:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (67, 'DEEPSEEK_1752211206639_0', '2025-07-08 18:20:07', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 13:20:07', '2025-07-11 13:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (68, 'DEEPSEEK_1752211206671_1', '2025-07-11 07:20:07', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 13:20:07', '2025-07-11 13:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (69, 'DEEPSEEK_1752211206684_2', '2025-07-10 23:20:07', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 13:20:07', '2025-07-11 13:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (70, 'DEEPSEEK_1752211206698_3', '2025-07-09 22:20:07', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 13:20:07', '2025-07-11 13:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (71, 'DEEPSEEK_1752211206710_4', '2025-07-08 15:20:07', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 13:20:07', '2025-07-11 13:20:07', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (72, 'DEEPSEEK_1752214805635_0', '2025-07-10 03:20:06', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 14:20:06', '2025-07-11 14:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (73, 'DEEPSEEK_1752214805658_1', '2025-07-09 02:20:06', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 14:20:06', '2025-07-11 14:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (74, 'DEEPSEEK_1752214805671_2', '2025-07-11 12:20:06', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 14:20:06', '2025-07-11 14:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (75, 'DEEPSEEK_1752214805683_3', '2025-07-10 21:20:06', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 14:20:06', '2025-07-11 14:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (76, 'DEEPSEEK_1752214805696_4', '2025-07-11 01:20:06', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 14:20:06', '2025-07-11 14:20:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (77, 'DEEPSEEK_1752218401069_0', '2025-07-11 12:20:01', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 15:20:01', '2025-07-11 15:20:01', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (78, 'DEEPSEEK_1752218401092_1', '2025-07-09 05:20:01', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 15:20:01', '2025-07-11 15:20:01', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (79, 'DEEPSEEK_1752218401110_2', '2025-07-10 10:20:01', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 15:20:01', '2025-07-11 15:20:01', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (80, 'DEEPSEEK_1752218401125_3', '2025-07-11 01:20:01', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 15:20:01', '2025-07-11 15:20:01', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (81, 'DEEPSEEK_1752218401142_4', '2025-07-10 07:20:01', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 15:20:01', '2025-07-11 15:20:01', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (82, 'DEEPSEEK_1752218931340_0', '2025-07-10 17:28:51', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 15:28:52', '2025-07-11 15:28:52', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (83, 'DEEPSEEK_1752218931597_1', '2025-07-11 10:28:51', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 15:28:52', '2025-07-11 15:28:52', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (84, 'DEEPSEEK_1752218931626_2', '2025-07-09 16:28:51', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 15:28:52', '2025-07-11 15:28:52', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (85, 'DEEPSEEK_1752218931655_3', '2025-07-08 21:28:51', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 15:28:52', '2025-07-11 15:28:52', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (86, 'DEEPSEEK_1752218931678_4', '2025-07-11 13:28:51', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 15:28:52', '2025-07-11 15:28:52', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (87, 'DEEPSEEK_1752219571512_0', '2025-07-10 00:39:32', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 15:39:32', '2025-07-11 15:39:32', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (88, 'DEEPSEEK_1752219571777_1', '2025-07-09 15:39:32', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 15:39:32', '2025-07-11 15:39:32', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (89, 'DEEPSEEK_1752219571803_2', '2025-07-10 02:39:32', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 15:39:32', '2025-07-11 15:39:32', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (90, 'DEEPSEEK_1752219571826_3', '2025-07-08 19:39:32', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 15:39:32', '2025-07-11 15:39:32', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (91, 'DEEPSEEK_1752219571850_4', '2025-07-10 17:39:32', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 15:39:32', '2025-07-11 15:39:32', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (92, 'DEEPSEEK_1752221803955_0', '2025-07-09 11:16:44', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 16:16:44', '2025-07-11 16:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (93, 'DEEPSEEK_1752221804249_1', '2025-07-10 13:16:44', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 16:16:44', '2025-07-11 16:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (94, 'DEEPSEEK_1752221804278_2', '2025-07-10 03:16:44', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 16:16:44', '2025-07-11 16:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (95, 'DEEPSEEK_1752221804314_3', '2025-07-10 23:16:44', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 16:16:44', '2025-07-11 16:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (96, 'DEEPSEEK_1752221804341_4', '2025-07-10 05:16:44', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 16:16:44', '2025-07-11 16:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (97, 'DEEPSEEK_1752225403669_0', '2025-07-09 01:16:44', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-11 17:16:44', '2025-07-11 17:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (98, 'DEEPSEEK_1752225403718_1', '2025-07-11 05:16:44', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-11 17:16:44', '2025-07-11 17:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (99, 'DEEPSEEK_1752225403748_2', '2025-07-09 20:16:44', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-11 17:16:44', '2025-07-11 17:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (100, 'DEEPSEEK_1752225403774_3', '2025-07-10 01:16:44', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-11 17:16:44', '2025-07-11 17:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (101, 'DEEPSEEK_1752225403800_4', '2025-07-10 06:16:44', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-11 17:16:44', '2025-07-11 17:16:44', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (102, 'DEEPSEEK_1752453047067_0', '2025-07-13 19:30:47', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 08:43:24', '2025-07-14 08:43:24', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (103, 'DEEPSEEK_1752453803944_1', '2025-07-12 18:30:47', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 08:43:24', '2025-07-14 08:43:24', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (104, 'DEEPSEEK_1752453804042_2', '2025-07-13 14:30:47', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 08:43:24', '2025-07-14 08:43:24', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (105, 'DEEPSEEK_1752453804104_3', '2025-07-13 22:30:47', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 08:43:24', '2025-07-14 08:43:24', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (106, 'DEEPSEEK_1752453804133_4', '2025-07-12 08:30:47', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 08:43:24', '2025-07-14 08:43:24', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (107, 'DEEPSEEK_1752457009398_0', '2025-07-11 17:36:49', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 09:36:50', '2025-07-14 09:36:50', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (108, 'DEEPSEEK_1752457009631_1', '2025-07-14 00:36:49', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 09:36:50', '2025-07-14 09:36:50', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (109, 'DEEPSEEK_1752457009656_2', '2025-07-12 12:36:49', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 09:36:50', '2025-07-14 09:36:50', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (110, 'DEEPSEEK_1752457009681_3', '2025-07-13 11:36:49', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 09:36:50', '2025-07-14 09:36:50', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (111, 'DEEPSEEK_1752457009703_4', '2025-07-12 13:36:49', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 09:36:50', '2025-07-14 09:36:50', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (112, 'DEEPSEEK_1752462117737_0', '2025-07-13 14:01:58', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 11:01:58', '2025-07-14 11:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (113, 'DEEPSEEK_1752462118098_1', '2025-07-13 03:01:58', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 11:01:58', '2025-07-14 11:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (114, 'DEEPSEEK_1752462118134_2', '2025-07-13 21:01:58', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 11:01:58', '2025-07-14 11:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (115, 'DEEPSEEK_1752462118159_3', '2025-07-12 04:01:58', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 11:01:58', '2025-07-14 11:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (116, 'DEEPSEEK_1752462118186_4', '2025-07-12 05:01:58', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 11:01:58', '2025-07-14 11:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (117, 'DEEPSEEK_1752465717331_0', '2025-07-13 02:01:57', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 12:01:57', '2025-07-14 12:01:57', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (118, 'DEEPSEEK_1752465717408_1', '2025-07-11 16:01:57', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 12:01:57', '2025-07-14 12:01:57', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (119, 'DEEPSEEK_1752465717431_2', '2025-07-12 00:01:57', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 12:01:57', '2025-07-14 12:01:57', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (120, 'DEEPSEEK_1752465717486_3', '2025-07-11 23:01:57', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 12:01:57', '2025-07-14 12:01:57', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (121, 'DEEPSEEK_1752465717508_4', '2025-07-11 12:01:57', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 12:01:58', '2025-07-14 12:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (122, 'DEEPSEEK_1752469317531_0', '2025-07-11 17:01:58', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 13:01:58', '2025-07-14 13:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (123, 'DEEPSEEK_1752469317564_1', '2025-07-14 12:01:58', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 13:01:58', '2025-07-14 13:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (124, 'DEEPSEEK_1752469317576_2', '2025-07-13 21:01:58', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 13:01:58', '2025-07-14 13:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (125, 'DEEPSEEK_1752469317591_3', '2025-07-14 07:01:58', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 13:01:58', '2025-07-14 13:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (126, 'DEEPSEEK_1752469317602_4', '2025-07-14 02:01:58', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 13:01:58', '2025-07-14 13:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (127, 'DEEPSEEK_1752472917838_0', '2025-07-13 13:01:58', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668010, 31.5017010, 2, 1, '2025-07-14 14:01:58', '2025-07-14 14:22:05', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (128, 'DEEPSEEK_1752472917864_1', '2025-07-13 07:01:58', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 14:01:58', '2025-07-14 14:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (129, 'DEEPSEEK_1752472917876_2', '2025-07-14 00:01:58', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 14:01:58', '2025-07-14 14:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (130, 'DEEPSEEK_1752472917889_3', '2025-07-13 19:01:58', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 14:01:58', '2025-07-14 14:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (131, 'DEEPSEEK_1752472917902_4', '2025-07-13 10:01:58', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 14:01:58', '2025-07-14 14:01:58', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (132, 'DEEPSEEK_1752475714614_0', '2025-07-12 12:48:35', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 14:48:35', '2025-07-14 14:48:35', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (133, 'DEEPSEEK_1752475714967_1', '2025-07-11 20:48:35', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 14:48:35', '2025-07-14 14:48:35', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (134, 'DEEPSEEK_1752475714997_2', '2025-07-14 11:48:35', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 14:48:35', '2025-07-14 14:48:35', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (135, 'DEEPSEEK_1752475715025_3', '2025-07-11 23:48:35', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 14:48:35', '2025-07-14 14:48:35', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (136, 'DEEPSEEK_1752475715048_4', '2025-07-14 07:48:35', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 14:48:35', '2025-07-14 14:48:35', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (137, 'DEEPSEEK_1752476426995_0', '2025-07-12 03:00:27', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 15:00:27', '2025-07-14 15:00:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (138, 'DEEPSEEK_1752476427336_1', '2025-07-13 17:00:27', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 15:00:27', '2025-07-14 15:00:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (139, 'DEEPSEEK_1752476427374_2', '2025-07-13 00:00:27', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 15:00:27', '2025-07-14 15:00:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (140, 'DEEPSEEK_1752476427403_3', '2025-07-13 16:00:27', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 15:00:27', '2025-07-14 15:00:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (141, 'DEEPSEEK_1752476427432_4', '2025-07-14 12:00:27', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 15:00:27', '2025-07-14 15:00:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (142, 'DEEPSEEK_1752477806604_0', '2025-07-11 17:23:27', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 15:23:27', '2025-07-14 15:23:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (143, 'DEEPSEEK_1752477806939_1', '2025-07-14 13:23:27', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 15:23:27', '2025-07-14 15:23:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (144, 'DEEPSEEK_1752477806970_2', '2025-07-13 16:23:27', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 15:23:27', '2025-07-14 15:23:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (145, 'DEEPSEEK_1752477807001_3', '2025-07-13 01:23:27', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 15:23:27', '2025-07-14 15:23:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (146, 'DEEPSEEK_1752477807025_4', '2025-07-14 06:23:27', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 15:23:27', '2025-07-14 15:23:27', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (147, 'DEEPSEEK_1752478399564_0', '2025-07-14 05:33:20', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 15:33:20', '2025-07-14 15:33:20', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (148, 'DEEPSEEK_1752478399976_1', '2025-07-13 18:33:20', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 15:33:20', '2025-07-14 15:33:20', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (149, 'DEEPSEEK_1752478400005_2', '2025-07-14 03:33:20', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 15:33:20', '2025-07-14 15:33:20', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (150, 'DEEPSEEK_1752478400027_3', '2025-07-14 09:33:20', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 15:33:20', '2025-07-14 15:33:20', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (151, 'DEEPSEEK_1752478400055_4', '2025-07-12 14:33:20', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 15:33:20', '2025-07-14 15:33:20', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (152, 'DEEPSEEK_1752479981978_0', '2025-07-14 01:59:42', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 15:59:42', '2025-07-14 15:59:42', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (153, 'DEEPSEEK_1752479982512_1', '2025-07-13 09:59:42', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 15:59:43', '2025-07-14 15:59:43', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (154, 'DEEPSEEK_1752479982544_2', '2025-07-13 18:59:42', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 15:59:43', '2025-07-14 15:59:43', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (155, 'DEEPSEEK_1752479982568_3', '2025-07-13 09:59:42', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 15:59:43', '2025-07-14 15:59:43', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (156, 'DEEPSEEK_1752479982587_4', '2025-07-13 09:59:42', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 15:59:43', '2025-07-14 15:59:43', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (157, 'DEEPSEEK_1752481553587_0', '2025-07-13 14:25:54', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 16:25:54', '2025-07-14 16:25:54', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (158, 'DEEPSEEK_1752481553900_1', '2025-07-12 05:25:54', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 16:25:54', '2025-07-14 16:25:54', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (159, 'DEEPSEEK_1752481553929_2', '2025-07-14 01:25:54', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 16:25:54', '2025-07-14 16:25:54', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (160, 'DEEPSEEK_1752481553956_3', '2025-07-12 14:25:54', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 16:25:54', '2025-07-14 16:25:54', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (161, 'DEEPSEEK_1752481553980_4', '2025-07-13 18:25:54', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 16:25:54', '2025-07-14 16:25:54', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (162, 'DEEPSEEK_1752483725419_0', '2025-07-13 11:02:05', '加沙地带', '军事冲突', '以色列军队出动战机和无人机，对加沙地带北部和中部的哈马斯军事目标发动了大规模空袭行动，以报复当天早些时候来自加沙地带的火箭弹袭击。此次空袭持续了4个小时，摧毁了多个武器储存设施和指挥中心，造成至少15名武装分子死亡。以军发言人表示，这是对恐怖主义活动的坚决回应。', '以色列军队', '哈马斯', 34.4668000, 31.5017000, 2, 1, '2025-07-14 17:02:06', '2025-07-14 17:02:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (163, 'DEEPSEEK_1752483725642_1', '2025-07-11 23:02:05', '华盛顿特区', '外交会谈', '美国总统拜登与中国国家主席习近平通过视频连线举行了长达3小时的双边会晤，双方就台湾问题、贸易争端、气候变化和朝鲜核问题等关键议题进行了深入讨论。会晤期间，两国领导人重申了管控分歧、避免误判的重要性，并同意建立经济工作小组，为下一轮高级别对话做准备。', '美国', '中国', -77.0369000, 38.9072000, 2, 1, '2025-07-14 17:02:06', '2025-07-14 17:02:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (164, 'DEEPSEEK_1752483725663_2', '2025-07-12 05:02:05', '顿涅茨克', '军事行动', '俄罗斯军队在乌克兰东部顿涅茨克地区发动了今年以来规模最大的攻势，动用了超过300门火炮、50辆坦克和多架苏-25攻击机对乌军防线进行密集轰炸。经过48小时的激烈交火，俄军成功占领了3个战略要点，迫使乌军向西撤退约15公里。乌克兰国防部确认了此次战斗的激烈程度。', '俄罗斯军队', '乌克兰', 37.8044000, 48.0159000, 2, 1, '2025-07-14 17:02:06', '2025-07-14 17:02:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (165, 'DEEPSEEK_1752483725686_3', '2025-07-11 18:02:05', '布鲁塞尔', '经济制裁', '欧盟委员会宣布对伊朗实施新一轮制裁措施，涉及该国石油出口、金融机构和军工企业等多个领域。此次制裁是对伊朗继续发展核计划和支持地区代理武装的回应。制裁清单包括20家伊朗企业和15名高级官员，预计将严重影响伊朗的经济和军事能力。', '欧盟', '伊朗', 4.3517000, 50.8503000, 2, 1, '2025-07-14 17:02:06', '2025-07-14 17:02:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);
INSERT INTO `event` VALUES (166, 'DEEPSEEK_1752483725706_4', '2025-07-12 07:02:05', '平壤', '导弹试验', '朝鲜在平壤以北的试验场成功发射了一枚新型中程弹道导弹，飞行距离达到1200公里，落入日本海指定海域。朝鲜官方媒体称这是\'重要的国防科技成就\'，旨在增强国家自卫能力。韩国军方对此表示强烈谴责，并与美日两国协调应对措施。', '朝鲜', '国际社会', 125.7625000, 39.0392000, 2, 1, '2025-07-14 17:02:06', '2025-07-14 17:02:06', 'deepseek_task', 'deepseek_task', NULL, NULL, 1);

-- ----------------------------
-- Table structure for event_keyword
-- ----------------------------
DROP TABLE IF EXISTS `event_keyword`;
CREATE TABLE `event_keyword`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `event_id` bigint(20) NOT NULL COMMENT '事件ID',
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_id`(`event_id`) USING BTREE,
  INDEX `idx_keyword`(`keyword`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 830 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '事件关键词表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of event_keyword
-- ----------------------------
INSERT INTO `event_keyword` VALUES (1, 1, '以色列', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (2, 1, '加沙', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (3, 1, '军事冲突', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (4, 1, '哈马斯', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (5, 1, '中东', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (6, 2, '美中关系', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (7, 2, '外交', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (8, 2, '国务卿', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (9, 2, '会谈', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (10, 2, '双边关系', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (11, 3, '俄乌冲突', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (12, 3, '边境', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (13, 3, '武装冲突', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (14, 3, '东欧', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (15, 3, '地缘政治', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (16, 4, '联合国', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (17, 4, '安理会', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (18, 4, '中东', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (19, 4, '紧急会议', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (20, 4, '国际调解', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (21, 5, '伊朗', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (22, 5, '核协议', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (23, 5, '制裁', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (24, 5, '核武器', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (25, 5, '国际法', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (26, 6, '朝俄合作', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (27, 6, '军事协议', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (28, 6, '平壤', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (29, 6, '东北亚', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (30, 6, '军事联盟', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (31, 7, '台海', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (32, 7, '两岸关系', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (33, 7, '紧张局势', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (34, 7, '军事演习', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (35, 7, '地区稳定', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (36, 8, '欧盟制裁', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (37, 8, '俄罗斯', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (38, 8, '经济制裁', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (39, 8, '国际制裁', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (40, 8, '经济战', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (41, 9, '韩美日', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (42, 9, '三方峰会', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (43, 9, '军事合作', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (44, 9, '印太战略', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (45, 9, '地区安全', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (46, 10, '日本央行', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (47, 10, '货币政策', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (48, 10, '经济政策', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (49, 10, '利率', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (50, 10, '金融市场', '2025-07-10 06:59:47');
INSERT INTO `event_keyword` VALUES (61, 13, '国际', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (62, 13, '热点', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (63, 13, '事件', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (64, 14, '国际', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (65, 14, '热点', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (66, 14, '事件', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (67, 15, '国际', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (68, 15, '热点', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (69, 15, '事件', '2025-07-10 15:31:55');
INSERT INTO `event_keyword` VALUES (70, 16, '国际', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (71, 16, '热点', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (72, 16, '事件', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (73, 17, '国际', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (74, 17, '热点', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (75, 17, '事件', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (76, 18, '国际', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (77, 18, '热点', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (78, 18, '事件', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (79, 19, '国际', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (80, 19, '热点', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (81, 19, '事件', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (82, 20, '国际', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (83, 20, '热点', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (84, 20, '事件', '2025-07-10 15:34:41');
INSERT INTO `event_keyword` VALUES (85, 21, '以色列', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (86, 21, '加沙', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (87, 21, '哈马斯', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (88, 21, '空袭', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (89, 21, '军事冲突', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (90, 22, '中美关系', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (91, 22, '双边会谈', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (92, 22, '台湾问题', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (93, 22, '贸易争端', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (94, 22, '气候变化', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (95, 23, '俄乌冲突', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (96, 23, '军事攻势', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (97, 23, '顿涅茨克', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (98, 23, '坦克', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (99, 23, '战略要点', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (100, 24, '欧盟制裁', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (101, 24, '伊朗核问题', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (102, 24, '石油出口', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (103, 24, '金融制裁', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (104, 24, '军工企业', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (105, 25, '朝鲜导弹', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (106, 25, '弹道导弹', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (107, 25, '日本海', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (108, 25, '国防科技', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (109, 25, '韩美日', '2025-07-10 15:40:39');
INSERT INTO `event_keyword` VALUES (110, 26, '以色列', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (111, 26, '加沙', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (112, 26, '哈马斯', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (113, 26, '空袭', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (114, 26, '军事冲突', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (115, 27, '中美关系', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (116, 27, '双边会谈', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (117, 27, '台湾问题', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (118, 27, '贸易争端', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (119, 27, '气候变化', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (120, 28, '俄乌冲突', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (121, 28, '军事攻势', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (122, 28, '顿涅茨克', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (123, 28, '坦克', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (124, 28, '战略要点', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (125, 29, '欧盟制裁', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (126, 29, '伊朗核问题', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (127, 29, '石油出口', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (128, 29, '金融制裁', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (129, 29, '军工企业', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (130, 30, '朝鲜导弹', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (131, 30, '弹道导弹', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (132, 30, '日本海', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (133, 30, '国防科技', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (134, 30, '韩美日', '2025-07-10 15:41:16');
INSERT INTO `event_keyword` VALUES (135, 31, '以色列', '2025-07-10 16:01:54');
INSERT INTO `event_keyword` VALUES (136, 31, '加沙', '2025-07-10 16:01:54');
INSERT INTO `event_keyword` VALUES (137, 31, '哈马斯', '2025-07-10 16:01:54');
INSERT INTO `event_keyword` VALUES (138, 31, '空袭', '2025-07-10 16:01:54');
INSERT INTO `event_keyword` VALUES (139, 31, '军事冲突', '2025-07-10 16:01:54');
INSERT INTO `event_keyword` VALUES (140, 32, '以色列', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (141, 32, '加沙', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (142, 32, '哈马斯', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (143, 32, '空袭', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (144, 32, '军事冲突', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (145, 33, '中美关系', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (146, 33, '双边会谈', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (147, 33, '台湾问题', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (148, 33, '贸易争端', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (149, 33, '气候变化', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (150, 34, '俄乌冲突', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (151, 34, '军事攻势', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (152, 34, '顿涅茨克', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (153, 34, '坦克', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (154, 34, '战略要点', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (155, 35, '欧盟制裁', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (156, 35, '伊朗核问题', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (157, 35, '石油出口', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (158, 35, '金融制裁', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (159, 35, '军工企业', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (160, 36, '朝鲜导弹', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (161, 36, '弹道导弹', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (162, 36, '日本海', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (163, 36, '国防科技', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (164, 36, '韩美日', '2025-07-10 16:41:17');
INSERT INTO `event_keyword` VALUES (165, 37, '以色列', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (166, 37, '加沙', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (167, 37, '哈马斯', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (168, 37, '空袭', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (169, 37, '军事冲突', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (170, 38, '中美关系', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (171, 38, '双边会谈', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (172, 38, '台湾问题', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (173, 38, '贸易争端', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (174, 38, '气候变化', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (175, 39, '俄乌冲突', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (176, 39, '军事攻势', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (177, 39, '顿涅茨克', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (178, 39, '坦克', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (179, 39, '战略要点', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (180, 40, '欧盟制裁', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (181, 40, '伊朗核问题', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (182, 40, '石油出口', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (183, 40, '金融制裁', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (184, 40, '军工企业', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (185, 41, '朝鲜导弹', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (186, 41, '弹道导弹', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (187, 41, '日本海', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (188, 41, '国防科技', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (189, 41, '韩美日', '2025-07-10 16:45:56');
INSERT INTO `event_keyword` VALUES (190, 42, '以色列', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (191, 42, '加沙', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (192, 42, '哈马斯', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (193, 42, '空袭', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (194, 42, '军事冲突', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (195, 43, '中美关系', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (196, 43, '双边会谈', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (197, 43, '台湾问题', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (198, 43, '贸易争端', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (199, 43, '气候变化', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (200, 44, '俄乌冲突', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (201, 44, '军事攻势', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (202, 44, '顿涅茨克', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (203, 44, '坦克', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (204, 44, '战略要点', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (205, 45, '欧盟制裁', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (206, 45, '伊朗核问题', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (207, 45, '石油出口', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (208, 45, '金融制裁', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (209, 45, '军工企业', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (210, 46, '朝鲜导弹', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (211, 46, '弹道导弹', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (212, 46, '日本海', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (213, 46, '国防科技', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (214, 46, '韩美日', '2025-07-11 08:25:33');
INSERT INTO `event_keyword` VALUES (215, 47, '以色列', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (216, 47, '加沙', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (217, 47, '哈马斯', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (218, 47, '空袭', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (219, 47, '军事冲突', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (220, 48, '中美关系', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (221, 48, '双边会谈', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (222, 48, '台湾问题', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (223, 48, '贸易争端', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (224, 48, '气候变化', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (225, 49, '俄乌冲突', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (226, 49, '军事攻势', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (227, 49, '顿涅茨克', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (228, 49, '坦克', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (229, 49, '战略要点', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (230, 50, '欧盟制裁', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (231, 50, '伊朗核问题', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (232, 50, '石油出口', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (233, 50, '金融制裁', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (234, 50, '军工企业', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (235, 51, '朝鲜导弹', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (236, 51, '弹道导弹', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (237, 51, '日本海', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (238, 51, '国防科技', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (239, 51, '韩美日', '2025-07-11 08:45:28');
INSERT INTO `event_keyword` VALUES (240, 52, '以色列', '2025-07-11 10:20:06');
INSERT INTO `event_keyword` VALUES (241, 52, '加沙', '2025-07-11 10:20:06');
INSERT INTO `event_keyword` VALUES (242, 52, '哈马斯', '2025-07-11 10:20:06');
INSERT INTO `event_keyword` VALUES (243, 52, '空袭', '2025-07-11 10:20:06');
INSERT INTO `event_keyword` VALUES (244, 52, '军事冲突', '2025-07-11 10:20:06');
INSERT INTO `event_keyword` VALUES (245, 53, '中美关系', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (246, 53, '双边会谈', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (247, 53, '台湾问题', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (248, 53, '贸易争端', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (249, 53, '气候变化', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (250, 54, '俄乌冲突', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (251, 54, '军事攻势', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (252, 54, '顿涅茨克', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (253, 54, '坦克', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (254, 54, '战略要点', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (255, 55, '欧盟制裁', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (256, 55, '伊朗核问题', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (257, 55, '石油出口', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (258, 55, '金融制裁', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (259, 55, '军工企业', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (260, 56, '朝鲜导弹', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (261, 56, '弹道导弹', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (262, 56, '日本海', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (263, 56, '国防科技', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (264, 56, '韩美日', '2025-07-11 10:20:07');
INSERT INTO `event_keyword` VALUES (265, 57, '以色列', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (266, 57, '加沙', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (267, 57, '哈马斯', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (268, 57, '空袭', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (269, 57, '军事冲突', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (270, 58, '中美关系', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (271, 58, '双边会谈', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (272, 58, '台湾问题', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (273, 58, '贸易争端', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (274, 58, '气候变化', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (275, 59, '俄乌冲突', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (276, 59, '军事攻势', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (277, 59, '顿涅茨克', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (278, 59, '坦克', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (279, 59, '战略要点', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (280, 60, '欧盟制裁', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (281, 60, '伊朗核问题', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (282, 60, '石油出口', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (283, 60, '金融制裁', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (284, 60, '军工企业', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (285, 61, '朝鲜导弹', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (286, 61, '弹道导弹', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (287, 61, '日本海', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (288, 61, '国防科技', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (289, 61, '韩美日', '2025-07-11 11:20:06');
INSERT INTO `event_keyword` VALUES (290, 62, '以色列', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (291, 62, '加沙', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (292, 62, '哈马斯', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (293, 62, '空袭', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (294, 62, '军事冲突', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (295, 63, '中美关系', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (296, 63, '双边会谈', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (297, 63, '台湾问题', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (298, 63, '贸易争端', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (299, 63, '气候变化', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (300, 64, '俄乌冲突', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (301, 64, '军事攻势', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (302, 64, '顿涅茨克', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (303, 64, '坦克', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (304, 64, '战略要点', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (305, 65, '欧盟制裁', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (306, 65, '伊朗核问题', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (307, 65, '石油出口', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (308, 65, '金融制裁', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (309, 65, '军工企业', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (310, 66, '朝鲜导弹', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (311, 66, '弹道导弹', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (312, 66, '日本海', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (313, 66, '国防科技', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (314, 66, '韩美日', '2025-07-11 12:20:06');
INSERT INTO `event_keyword` VALUES (315, 67, '以色列', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (316, 67, '加沙', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (317, 67, '哈马斯', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (318, 67, '空袭', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (319, 67, '军事冲突', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (320, 68, '中美关系', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (321, 68, '双边会谈', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (322, 68, '台湾问题', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (323, 68, '贸易争端', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (324, 68, '气候变化', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (325, 69, '俄乌冲突', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (326, 69, '军事攻势', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (327, 69, '顿涅茨克', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (328, 69, '坦克', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (329, 69, '战略要点', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (330, 70, '欧盟制裁', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (331, 70, '伊朗核问题', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (332, 70, '石油出口', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (333, 70, '金融制裁', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (334, 70, '军工企业', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (335, 71, '朝鲜导弹', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (336, 71, '弹道导弹', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (337, 71, '日本海', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (338, 71, '国防科技', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (339, 71, '韩美日', '2025-07-11 13:20:07');
INSERT INTO `event_keyword` VALUES (340, 72, '以色列', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (341, 72, '加沙', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (342, 72, '哈马斯', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (343, 72, '空袭', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (344, 72, '军事冲突', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (345, 73, '中美关系', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (346, 73, '双边会谈', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (347, 73, '台湾问题', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (348, 73, '贸易争端', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (349, 73, '气候变化', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (350, 74, '俄乌冲突', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (351, 74, '军事攻势', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (352, 74, '顿涅茨克', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (353, 74, '坦克', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (354, 74, '战略要点', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (355, 75, '欧盟制裁', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (356, 75, '伊朗核问题', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (357, 75, '石油出口', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (358, 75, '金融制裁', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (359, 75, '军工企业', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (360, 76, '朝鲜导弹', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (361, 76, '弹道导弹', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (362, 76, '日本海', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (363, 76, '国防科技', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (364, 76, '韩美日', '2025-07-11 14:20:06');
INSERT INTO `event_keyword` VALUES (365, 77, '以色列', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (366, 77, '加沙', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (367, 77, '哈马斯', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (368, 77, '空袭', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (369, 77, '军事冲突', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (370, 78, '中美关系', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (371, 78, '双边会谈', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (372, 78, '台湾问题', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (373, 78, '贸易争端', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (374, 78, '气候变化', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (375, 79, '俄乌冲突', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (376, 79, '军事攻势', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (377, 79, '顿涅茨克', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (378, 79, '坦克', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (379, 79, '战略要点', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (380, 80, '欧盟制裁', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (381, 80, '伊朗核问题', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (382, 80, '石油出口', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (383, 80, '金融制裁', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (384, 80, '军工企业', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (385, 81, '朝鲜导弹', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (386, 81, '弹道导弹', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (387, 81, '日本海', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (388, 81, '国防科技', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (389, 81, '韩美日', '2025-07-11 15:20:01');
INSERT INTO `event_keyword` VALUES (390, 82, '以色列', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (391, 82, '加沙', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (392, 82, '哈马斯', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (393, 82, '空袭', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (394, 82, '军事冲突', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (395, 83, '中美关系', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (396, 83, '双边会谈', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (397, 83, '台湾问题', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (398, 83, '贸易争端', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (399, 83, '气候变化', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (400, 84, '俄乌冲突', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (401, 84, '军事攻势', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (402, 84, '顿涅茨克', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (403, 84, '坦克', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (404, 84, '战略要点', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (405, 85, '欧盟制裁', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (406, 85, '伊朗核问题', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (407, 85, '石油出口', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (408, 85, '金融制裁', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (409, 85, '军工企业', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (410, 86, '朝鲜导弹', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (411, 86, '弹道导弹', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (412, 86, '日本海', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (413, 86, '国防科技', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (414, 86, '韩美日', '2025-07-11 15:28:52');
INSERT INTO `event_keyword` VALUES (415, 87, '以色列', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (416, 87, '加沙', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (417, 87, '哈马斯', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (418, 87, '空袭', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (419, 87, '军事冲突', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (420, 88, '中美关系', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (421, 88, '双边会谈', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (422, 88, '台湾问题', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (423, 88, '贸易争端', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (424, 88, '气候变化', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (425, 89, '俄乌冲突', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (426, 89, '军事攻势', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (427, 89, '顿涅茨克', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (428, 89, '坦克', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (429, 89, '战略要点', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (430, 90, '欧盟制裁', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (431, 90, '伊朗核问题', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (432, 90, '石油出口', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (433, 90, '金融制裁', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (434, 90, '军工企业', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (435, 91, '朝鲜导弹', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (436, 91, '弹道导弹', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (437, 91, '日本海', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (438, 91, '国防科技', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (439, 91, '韩美日', '2025-07-11 15:39:32');
INSERT INTO `event_keyword` VALUES (440, 92, '以色列', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (441, 92, '加沙', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (442, 92, '哈马斯', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (443, 92, '空袭', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (444, 92, '军事冲突', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (445, 93, '中美关系', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (446, 93, '双边会谈', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (447, 93, '台湾问题', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (448, 93, '贸易争端', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (449, 93, '气候变化', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (450, 94, '俄乌冲突', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (451, 94, '军事攻势', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (452, 94, '顿涅茨克', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (453, 94, '坦克', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (454, 94, '战略要点', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (455, 95, '欧盟制裁', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (456, 95, '伊朗核问题', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (457, 95, '石油出口', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (458, 95, '金融制裁', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (459, 95, '军工企业', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (460, 96, '朝鲜导弹', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (461, 96, '弹道导弹', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (462, 96, '日本海', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (463, 96, '国防科技', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (464, 96, '韩美日', '2025-07-11 16:16:44');
INSERT INTO `event_keyword` VALUES (465, 97, '以色列', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (466, 97, '加沙', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (467, 97, '哈马斯', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (468, 97, '空袭', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (469, 97, '军事冲突', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (470, 98, '中美关系', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (471, 98, '双边会谈', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (472, 98, '台湾问题', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (473, 98, '贸易争端', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (474, 98, '气候变化', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (475, 99, '俄乌冲突', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (476, 99, '军事攻势', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (477, 99, '顿涅茨克', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (478, 99, '坦克', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (479, 99, '战略要点', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (480, 100, '欧盟制裁', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (481, 100, '伊朗核问题', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (482, 100, '石油出口', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (483, 100, '金融制裁', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (484, 100, '军工企业', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (485, 101, '朝鲜导弹', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (486, 101, '弹道导弹', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (487, 101, '日本海', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (488, 101, '国防科技', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (489, 101, '韩美日', '2025-07-11 17:16:44');
INSERT INTO `event_keyword` VALUES (490, 102, '以色列', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (491, 102, '加沙', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (492, 102, '哈马斯', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (493, 102, '空袭', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (494, 102, '军事冲突', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (495, 103, '中美关系', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (496, 103, '双边会谈', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (497, 103, '台湾问题', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (498, 103, '贸易争端', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (499, 103, '气候变化', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (500, 104, '俄乌冲突', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (501, 104, '军事攻势', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (502, 104, '顿涅茨克', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (503, 104, '坦克', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (504, 104, '战略要点', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (505, 105, '欧盟制裁', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (506, 105, '伊朗核问题', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (507, 105, '石油出口', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (508, 105, '金融制裁', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (509, 105, '军工企业', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (510, 106, '朝鲜导弹', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (511, 106, '弹道导弹', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (512, 106, '日本海', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (513, 106, '国防科技', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (514, 106, '韩美日', '2025-07-14 08:43:24');
INSERT INTO `event_keyword` VALUES (515, 107, '以色列', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (516, 107, '加沙', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (517, 107, '哈马斯', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (518, 107, '空袭', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (519, 107, '军事冲突', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (520, 108, '中美关系', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (521, 108, '双边会谈', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (522, 108, '台湾问题', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (523, 108, '贸易争端', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (524, 108, '气候变化', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (525, 109, '俄乌冲突', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (526, 109, '军事攻势', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (527, 109, '顿涅茨克', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (528, 109, '坦克', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (529, 109, '战略要点', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (530, 110, '欧盟制裁', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (531, 110, '伊朗核问题', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (532, 110, '石油出口', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (533, 110, '金融制裁', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (534, 110, '军工企业', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (535, 111, '朝鲜导弹', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (536, 111, '弹道导弹', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (537, 111, '日本海', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (538, 111, '国防科技', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (539, 111, '韩美日', '2025-07-14 09:36:50');
INSERT INTO `event_keyword` VALUES (540, 112, '以色列', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (541, 112, '加沙', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (542, 112, '哈马斯', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (543, 112, '空袭', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (544, 112, '军事冲突', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (545, 113, '中美关系', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (546, 113, '双边会谈', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (547, 113, '台湾问题', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (548, 113, '贸易争端', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (549, 113, '气候变化', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (550, 114, '俄乌冲突', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (551, 114, '军事攻势', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (552, 114, '顿涅茨克', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (553, 114, '坦克', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (554, 114, '战略要点', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (555, 115, '欧盟制裁', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (556, 115, '伊朗核问题', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (557, 115, '石油出口', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (558, 115, '金融制裁', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (559, 115, '军工企业', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (560, 116, '朝鲜导弹', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (561, 116, '弹道导弹', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (562, 116, '日本海', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (563, 116, '国防科技', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (564, 116, '韩美日', '2025-07-14 11:01:58');
INSERT INTO `event_keyword` VALUES (565, 117, '以色列', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (566, 117, '加沙', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (567, 117, '哈马斯', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (568, 117, '空袭', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (569, 117, '军事冲突', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (570, 118, '中美关系', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (571, 118, '双边会谈', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (572, 118, '台湾问题', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (573, 118, '贸易争端', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (574, 118, '气候变化', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (575, 119, '俄乌冲突', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (576, 119, '军事攻势', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (577, 119, '顿涅茨克', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (578, 119, '坦克', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (579, 119, '战略要点', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (580, 120, '欧盟制裁', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (581, 120, '伊朗核问题', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (582, 120, '石油出口', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (583, 120, '金融制裁', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (584, 120, '军工企业', '2025-07-14 12:01:57');
INSERT INTO `event_keyword` VALUES (585, 121, '朝鲜导弹', '2025-07-14 12:01:58');
INSERT INTO `event_keyword` VALUES (586, 121, '弹道导弹', '2025-07-14 12:01:58');
INSERT INTO `event_keyword` VALUES (587, 121, '日本海', '2025-07-14 12:01:58');
INSERT INTO `event_keyword` VALUES (588, 121, '国防科技', '2025-07-14 12:01:58');
INSERT INTO `event_keyword` VALUES (589, 121, '韩美日', '2025-07-14 12:01:58');
INSERT INTO `event_keyword` VALUES (590, 122, '以色列', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (591, 122, '加沙', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (592, 122, '哈马斯', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (593, 122, '空袭', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (594, 122, '军事冲突', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (595, 123, '中美关系', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (596, 123, '双边会谈', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (597, 123, '台湾问题', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (598, 123, '贸易争端', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (599, 123, '气候变化', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (600, 124, '俄乌冲突', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (601, 124, '军事攻势', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (602, 124, '顿涅茨克', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (603, 124, '坦克', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (604, 124, '战略要点', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (605, 125, '欧盟制裁', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (606, 125, '伊朗核问题', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (607, 125, '石油出口', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (608, 125, '金融制裁', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (609, 125, '军工企业', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (610, 126, '朝鲜导弹', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (611, 126, '弹道导弹', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (612, 126, '日本海', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (613, 126, '国防科技', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (614, 126, '韩美日', '2025-07-14 13:01:58');
INSERT INTO `event_keyword` VALUES (620, 128, '中美关系', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (621, 128, '双边会谈', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (622, 128, '台湾问题', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (623, 128, '贸易争端', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (624, 128, '气候变化', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (625, 129, '俄乌冲突', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (626, 129, '军事攻势', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (627, 129, '顿涅茨克', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (628, 129, '坦克', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (629, 129, '战略要点', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (630, 130, '欧盟制裁', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (631, 130, '伊朗核问题', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (632, 130, '石油出口', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (633, 130, '金融制裁', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (634, 130, '军工企业', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (635, 131, '朝鲜导弹', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (636, 131, '弹道导弹', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (637, 131, '日本海', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (638, 131, '国防科技', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (639, 131, '韩美日', '2025-07-14 14:01:58');
INSERT INTO `event_keyword` VALUES (655, 132, '以色列', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (656, 132, '加沙', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (657, 132, '哈马斯', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (658, 132, '空袭', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (659, 132, '军事冲突', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (660, 133, '中美关系', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (661, 133, '双边会谈', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (662, 133, '台湾问题', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (663, 133, '贸易争端', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (664, 133, '气候变化', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (665, 134, '俄乌冲突', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (666, 134, '军事攻势', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (667, 134, '顿涅茨克', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (668, 134, '坦克', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (669, 134, '战略要点', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (670, 135, '欧盟制裁', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (671, 135, '伊朗核问题', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (672, 135, '石油出口', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (673, 135, '金融制裁', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (674, 135, '军工企业', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (675, 136, '朝鲜导弹', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (676, 136, '弹道导弹', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (677, 136, '日本海', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (678, 136, '国防科技', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (679, 136, '韩美日', '2025-07-14 14:48:35');
INSERT INTO `event_keyword` VALUES (680, 137, '以色列', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (681, 137, '加沙', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (682, 137, '哈马斯', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (683, 137, '空袭', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (684, 137, '军事冲突', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (685, 138, '中美关系', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (686, 138, '双边会谈', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (687, 138, '台湾问题', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (688, 138, '贸易争端', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (689, 138, '气候变化', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (690, 139, '俄乌冲突', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (691, 139, '军事攻势', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (692, 139, '顿涅茨克', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (693, 139, '坦克', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (694, 139, '战略要点', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (695, 140, '欧盟制裁', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (696, 140, '伊朗核问题', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (697, 140, '石油出口', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (698, 140, '金融制裁', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (699, 140, '军工企业', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (700, 141, '朝鲜导弹', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (701, 141, '弹道导弹', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (702, 141, '日本海', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (703, 141, '国防科技', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (704, 141, '韩美日', '2025-07-14 15:00:27');
INSERT INTO `event_keyword` VALUES (705, 142, '以色列', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (706, 142, '加沙', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (707, 142, '哈马斯', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (708, 142, '空袭', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (709, 142, '军事冲突', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (710, 143, '中美关系', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (711, 143, '双边会谈', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (712, 143, '台湾问题', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (713, 143, '贸易争端', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (714, 143, '气候变化', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (715, 144, '俄乌冲突', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (716, 144, '军事攻势', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (717, 144, '顿涅茨克', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (718, 144, '坦克', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (719, 144, '战略要点', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (720, 145, '欧盟制裁', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (721, 145, '伊朗核问题', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (722, 145, '石油出口', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (723, 145, '金融制裁', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (724, 145, '军工企业', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (725, 146, '朝鲜导弹', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (726, 146, '弹道导弹', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (727, 146, '日本海', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (728, 146, '国防科技', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (729, 146, '韩美日', '2025-07-14 15:23:27');
INSERT INTO `event_keyword` VALUES (730, 147, '以色列', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (731, 147, '加沙', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (732, 147, '哈马斯', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (733, 147, '空袭', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (734, 147, '军事冲突', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (735, 148, '中美关系', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (736, 148, '双边会谈', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (737, 148, '台湾问题', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (738, 148, '贸易争端', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (739, 148, '气候变化', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (740, 149, '俄乌冲突', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (741, 149, '军事攻势', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (742, 149, '顿涅茨克', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (743, 149, '坦克', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (744, 149, '战略要点', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (745, 150, '欧盟制裁', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (746, 150, '伊朗核问题', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (747, 150, '石油出口', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (748, 150, '金融制裁', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (749, 150, '军工企业', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (750, 151, '朝鲜导弹', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (751, 151, '弹道导弹', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (752, 151, '日本海', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (753, 151, '国防科技', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (754, 151, '韩美日', '2025-07-14 15:33:20');
INSERT INTO `event_keyword` VALUES (755, 152, '以色列', '2025-07-14 15:59:42');
INSERT INTO `event_keyword` VALUES (756, 152, '加沙', '2025-07-14 15:59:42');
INSERT INTO `event_keyword` VALUES (757, 152, '哈马斯', '2025-07-14 15:59:42');
INSERT INTO `event_keyword` VALUES (758, 152, '空袭', '2025-07-14 15:59:42');
INSERT INTO `event_keyword` VALUES (759, 152, '军事冲突', '2025-07-14 15:59:42');
INSERT INTO `event_keyword` VALUES (760, 153, '中美关系', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (761, 153, '双边会谈', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (762, 153, '台湾问题', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (763, 153, '贸易争端', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (764, 153, '气候变化', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (765, 154, '俄乌冲突', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (766, 154, '军事攻势', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (767, 154, '顿涅茨克', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (768, 154, '坦克', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (769, 154, '战略要点', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (770, 155, '欧盟制裁', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (771, 155, '伊朗核问题', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (772, 155, '石油出口', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (773, 155, '金融制裁', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (774, 155, '军工企业', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (775, 156, '朝鲜导弹', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (776, 156, '弹道导弹', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (777, 156, '日本海', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (778, 156, '国防科技', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (779, 156, '韩美日', '2025-07-14 15:59:43');
INSERT INTO `event_keyword` VALUES (780, 157, '以色列', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (781, 157, '加沙', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (782, 157, '哈马斯', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (783, 157, '空袭', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (784, 157, '军事冲突', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (785, 158, '中美关系', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (786, 158, '双边会谈', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (787, 158, '台湾问题', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (788, 158, '贸易争端', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (789, 158, '气候变化', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (790, 159, '俄乌冲突', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (791, 159, '军事攻势', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (792, 159, '顿涅茨克', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (793, 159, '坦克', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (794, 159, '战略要点', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (795, 160, '欧盟制裁', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (796, 160, '伊朗核问题', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (797, 160, '石油出口', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (798, 160, '金融制裁', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (799, 160, '军工企业', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (800, 161, '朝鲜导弹', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (801, 161, '弹道导弹', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (802, 161, '日本海', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (803, 161, '国防科技', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (804, 161, '韩美日', '2025-07-14 16:25:54');
INSERT INTO `event_keyword` VALUES (805, 162, '以色列', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (806, 162, '加沙', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (807, 162, '哈马斯', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (808, 162, '空袭', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (809, 162, '军事冲突', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (810, 163, '中美关系', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (811, 163, '双边会谈', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (812, 163, '台湾问题', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (813, 163, '贸易争端', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (814, 163, '气候变化', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (815, 164, '俄乌冲突', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (816, 164, '军事攻势', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (817, 164, '顿涅茨克', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (818, 164, '坦克', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (819, 164, '战略要点', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (820, 165, '欧盟制裁', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (821, 165, '伊朗核问题', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (822, 165, '石油出口', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (823, 165, '金融制裁', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (824, 165, '军工企业', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (825, 166, '朝鲜导弹', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (826, 166, '弹道导弹', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (827, 166, '日本海', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (828, 166, '国防科技', '2025-07-14 17:02:06');
INSERT INTO `event_keyword` VALUES (829, 166, '韩美日', '2025-07-14 17:02:06');

-- ----------------------------
-- Table structure for event_relation
-- ----------------------------
DROP TABLE IF EXISTS `event_relation`;
CREATE TABLE `event_relation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `source_event_id` bigint(20) NOT NULL COMMENT '源事件ID',
  `target_event_id` bigint(20) NOT NULL COMMENT '目标事件ID',
  `relation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联类型：导致、影响、关联等',
  `relation_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关系名称',
  `intensity_level` int(11) NULL DEFAULT NULL COMMENT '强度级别',
  `relation_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '关联描述',
  `confidence` decimal(3, 2) NULL DEFAULT 1.00 COMMENT '置信度（0-1）',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `created_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_source_event`(`source_event_id`) USING BTREE,
  INDEX `idx_target_event`(`target_event_id`) USING BTREE,
  INDEX `idx_relation_type`(`relation_type`) USING BTREE,
  INDEX `idx_event_relation_name`(`relation_name`) USING BTREE,
  INDEX `idx_event_relation_intensity`(`intensity_level`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '事件关联关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of event_relation
-- ----------------------------
INSERT INTO `event_relation` VALUES (1, 1, 4, '导致', '导致', 1, '加沙冲突导致联合国安理会召开紧急会议', 0.85, 1, '2025-07-10 06:59:47', '2025-07-11 00:57:12', 'admin', 'admin');
INSERT INTO `event_relation` VALUES (2, 3, 8, '导致', '导致', 1, '俄乌冲突导致欧盟对俄实施新制裁', 0.90, 1, '2025-07-10 06:59:47', '2025-07-11 00:57:12', 'admin', 'admin');
INSERT INTO `event_relation` VALUES (3, 5, 4, '影响', '影响', 1, '伊朗核协议问题影响中东局势', 0.75, 1, '2025-07-10 06:59:47', '2025-07-11 00:57:13', 'admin', 'admin');
INSERT INTO `event_relation` VALUES (4, 6, 3, '支持', '支持', 1, '朝俄军事合作可能影响乌克兰局势', 0.60, 1, '2025-07-10 06:59:47', '2025-07-11 00:57:13', 'admin', 'admin');
INSERT INTO `event_relation` VALUES (5, 2, 7, '关联', '关联', 1, '美中外交会谈与台海局势相关', 0.70, 1, '2025-07-10 06:59:47', '2025-07-11 00:57:17', 'admin', 'admin');
INSERT INTO `event_relation` VALUES (6, 9, 6, '对抗', '对抗', 1, '韩美日三方合作对朝俄合作的回应', 0.65, 1, '2025-07-10 06:59:47', '2025-07-11 00:57:14', 'admin', 'admin');
INSERT INTO `event_relation` VALUES (7, 11, 1, '扩散', '扩散', 1, '红海袭击与中东冲突的扩散效应', 0.55, 1, '2025-07-10 06:59:47', '2025-07-11 00:57:15', 'admin', 'admin');
INSERT INTO `event_relation` VALUES (8, 10, 8, '影响', '影响', 1, '日本货币政策受俄乌冲突影响', 0.45, 1, '2025-07-10 06:59:47', '2025-07-11 00:57:19', 'admin', 'admin');

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '组织名称',
  `short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组织简称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组织类型（如国际组织、政府、企业等）',
  `country_id` bigint(20) NULL DEFAULT NULL COMMENT '所属国家ID',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组织描述',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '组织表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of organization
-- ----------------------------
INSERT INTO `organization` VALUES (1, '联合国', 'UN', '国际组织', NULL, '联合国是一个由主权国家组成的国际组织', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (2, '北大西洋公约组织', 'NATO', '军事组织', NULL, '北约是欧洲和北美国家的政治和军事联盟', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (3, '欧洲联盟', 'EU', '政治经济联盟', NULL, '欧盟是欧洲国家的政治和经济联盟', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (4, '世界贸易组织', 'WTO', '国际组织', NULL, '世界贸易组织是处理国家间贸易规则的国际组织', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (5, '中国共产党', 'CPC', '政党', 1, '中华人民共和国执政党', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (6, '美国国务院', 'DOS', '政府机构', 2, '美国负责外交事务的联邦政府部门', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (7, '俄罗斯联邦政府', 'RFG', '政府机构', 3, '俄罗斯联邦的行政机关', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (8, '哈马斯', 'Hamas', '政治军事组织', NULL, '巴勒斯坦伊斯兰抵抗运动', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (9, '真主党', 'Hezbollah', '政治军事组织', NULL, '黎巴嫩什叶派政治军事组织', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (10, '伊斯兰革命卫队', 'IRGC', '军事组织', 11, '伊朗伊斯兰革命卫队', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (11, '亚太经济合作组织', 'APEC', '经济组织', NULL, 'APEC是亚太地区经济合作论坛', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (12, '上海合作组织', 'SCO', '政治军事组织', NULL, '上海合作组织是欧亚地区的政治、经济、军事组织', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (13, '自由民主党', 'LDP', '政党', 4, '日本自由民主党', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (14, '工党', 'Labour', '政党', 9, '英国工党', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `organization` VALUES (15, '德国联邦议院', 'Bundestag', '政府机构', 7, '德国联邦议院', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');

-- ----------------------------
-- Table structure for person
-- ----------------------------
DROP TABLE IF EXISTS `person`;
CREATE TABLE `person`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '姓名',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `country_id` bigint(20) NULL DEFAULT NULL COMMENT '国籍ID',
  `organization_id` bigint(20) NULL DEFAULT NULL COMMENT '所属组织ID',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '人物描述',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '人物表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of person
-- ----------------------------
INSERT INTO `person` VALUES (1, '习大大', '男', '1953-06-15', 1, 5, '中华人民共和国主席、中国共产党中央委员会总书记', 1, '2025-07-10 06:59:47', '2025-07-10 07:00:39', 'admin', 'admin');
INSERT INTO `person` VALUES (2, '拜登', '男', '1942-11-20', 2, 6, '美国第46任总统', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (3, '普京', '男', '1952-10-07', 3, 7, '俄罗斯联邦总统', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (4, '岸田文雄', '男', '1957-07-29', 4, 13, '日本首相', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (5, '尹锡悦', '男', '1960-12-18', 5, NULL, '韩国总统', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (6, '金正恩', '男', '1984-01-08', 6, NULL, '朝鲜最高领导人', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (7, '泽连斯基', '男', '1978-01-25', 12, NULL, '乌克兰总统', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (8, '内塔尼亚胡', '男', '1949-10-21', 10, NULL, '以色列总理', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (9, '哈梅内伊', '男', '1939-04-19', 11, 10, '伊朗最高领袖', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (10, '马克龙', '男', '1977-12-21', 8, NULL, '法国总统', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (11, '朔尔茨', '男', '1958-06-14', 7, 15, '德国总理', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (12, '苏纳克', '男', '1980-05-12', 9, 14, '英国首相', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (13, '特鲁多', '男', '1971-12-25', 13, NULL, '加拿大总理', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (14, '阿尔巴尼斯', '男', '1963-03-02', 14, NULL, '澳大利亚总理', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');
INSERT INTO `person` VALUES (15, '莫迪', '男', '1950-09-17', 15, NULL, '印度总理', 1, '2025-07-10 06:59:47', '2025-07-10 06:59:47', 'admin', 'admin');

-- ----------------------------
-- Table structure for subject_object_relation
-- ----------------------------
DROP TABLE IF EXISTS `subject_object_relation`;
CREATE TABLE `subject_object_relation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `subject_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主体编码（来自dictionary表）',
  `object_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客体编码（来自dictionary表）',
  `relation_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关系类型',
  `relation_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关系名称',
  `intensity_level` int(11) NOT NULL DEFAULT 1 COMMENT '强度级别（1-5，1最弱，5最强）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '关系描述',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'system' COMMENT '创建人',
  `updated_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'system' COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_subject_object_relation`(`subject_code`, `object_code`, `relation_type`) USING BTREE,
  INDEX `idx_subject_code`(`subject_code`) USING BTREE,
  INDEX `idx_object_code`(`object_code`) USING BTREE,
  INDEX `idx_relation_type`(`relation_type`) USING BTREE,
  INDEX `idx_intensity_level`(`intensity_level`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '主体客体关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of subject_object_relation
-- ----------------------------

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `config_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置描述',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'STRING' COMMENT '配置类型：STRING,INTEGER,BOOLEAN,JSON',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'deepseek.api.key', '', 'DeepSeek API密钥', 'STRING', 1, '2025-07-09 00:56:49', '2025-07-09 00:56:49');
INSERT INTO `system_config` VALUES (2, 'deepseek.api.url', 'https://api.deepseek.com', 'DeepSeek API地址', 'STRING', 1, '2025-07-09 00:56:49', '2025-07-09 00:56:49');
INSERT INTO `system_config` VALUES (3, 'task.fetch.interval', '3600', '数据获取间隔（秒）', 'INTEGER', 1, '2025-07-09 00:56:49', '2025-07-09 00:56:49');
INSERT INTO `system_config` VALUES (4, 'task.fetch.enabled', 'true', '是否启用定时获取', 'BOOLEAN', 1, '2025-07-09 00:56:49', '2025-07-09 00:56:49');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdxp7.5lCgCCjkVm', '管理员', 'admin@example.com', NULL, 1, '2025-07-09 00:56:49', '2025-07-09 00:56:49');

SET FOREIGN_KEY_CHECKS = 1;
