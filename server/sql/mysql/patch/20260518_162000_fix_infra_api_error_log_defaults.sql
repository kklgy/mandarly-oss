-- P3:infra_api_error_log exception location fields need DB defaults.
-- Root cause:some ServiceException logging paths can produce NULL exception location
-- fields; MySQL strict mode rejects INSERT before the error log is persisted.
-- Rollback:drop these DEFAULT clauses if strict NULL rejection is desired again.

ALTER TABLE `infra_api_error_log`
    MODIFY COLUMN `exception_class_name` VARCHAR(512) NOT NULL DEFAULT '' COMMENT '异常发生的类全名',
    MODIFY COLUMN `exception_file_name` VARCHAR(512) NOT NULL DEFAULT '' COMMENT '异常发生的类文件',
    MODIFY COLUMN `exception_method_name` VARCHAR(512) NOT NULL DEFAULT '' COMMENT '异常发生的方法名',
    MODIFY COLUMN `exception_line_number` INT NOT NULL DEFAULT 0 COMMENT '异常发生的方法所在行';
