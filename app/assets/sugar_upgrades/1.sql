-- 版本: 1
-- 功能: 原生创建脚本, 不涉及数据迁移

BEGIN TRANSACTION;

DROP TABLE IF EXISTS "main"."global_config";
CREATE TABLE "main"."global_config" (
	 "name" text NOT NULL DEFAULT 1, 
	 "value" text DEFAULT NULL,
	PRIMARY KEY("name")
);

DROP TABLE IF EXISTS "main"."account";
CREATE TABLE "main"."account" (
	 "site_name" text NOT NULL DEFAULT NULL,
	 "site_url" text DEFAULT NULL,
	 "user_name" text NOT NULL,
	 "user_passwd" text DEFAULT NULL,
	PRIMARY KEY("user_name","site_name")
);

COMMIT;