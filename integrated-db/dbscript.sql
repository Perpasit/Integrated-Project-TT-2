DROP DATABASE IF EXISTS integrateddatabase;
CREATE DATABASE integrateddatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE integrateddatabase;

CREATE TABLE statuses (
    statusId INT AUTO_INCREMENT PRIMARY KEY,
    statusName VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    statusDescription VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    colorCode VARCHAR(7) NOT NULL,
     createdOn DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT CHK_statusName_not_empty CHECK (statusName <> ''),
    CONSTRAINT CHK_statusName_trim CHECK (statusName = TRIM(statusName)),
    CONSTRAINT CHK_statusDescription_trim CHECK (statusDescription = TRIM(statusDescription) OR statusDescription IS NULL),
        CONSTRAINT CHK_colorCode_valid CHECK (colorCode RLIKE '^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$')  -- Regex to ensure valid hex >);
CREATE TABLE tasks (
    taskId INT AUTO_INCREMENT PRIMARY KEY,
    taskTitle VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    taskDescription VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    taskAssignees VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    statusId INT DEFAULT NULL,
    createdOn DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT CHK_taskTitle_not_empty CHECK (taskTitle <> ''),
    CONSTRAINT CHK_updatedOn_not_empty CHECK (updatedOn >= createdOn),
    CONSTRAINT fk_status FOREIGN KEY (statusId) REFERENCES statuses(statusId) ON DELETE SET NULL
);
