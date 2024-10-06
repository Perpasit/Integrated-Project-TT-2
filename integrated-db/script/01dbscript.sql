DROP DATABASE IF EXISTS integrateddatabase;
CREATE DATABASE integrateddatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE integrateddatabase;
CREATE TABLE global_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    maximumTaskLimit INT NOT NULL DEFAULT 10,
    limitMaximumTask BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE users (
    oid CHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_oid CHECK (oid REGEXP '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'),
    CONSTRAINT chk_name_length CHECK (TRIM(name) != '' AND LENGTH(TRIM(name)) <= 100),
    CONSTRAINT chk_username_length CHECK (TRIM(username) != '' AND LENGTH(TRIM(username)) <= 50),
    CONSTRAINT chk_email_length CHECK (TRIM(email) != '' AND LENGTH(TRIM(email)) <= 50)
);
CREATE TABLE boards (
    id CHAR(10) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    owner_id CHAR(36) NOT NULL,
    visibility ENUM('PUBLIC', 'PRIVATE') NOT NULL DEFAULT 'PRIVATE', -- ใช้ ENUM สำหรับการตั้งค่า visibility
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_board_id CHECK (id REGEXP '^[A-Za-z0-9_-]{10}$'),
    CONSTRAINT chk_board_name CHECK (TRIM(name) != ''),
    FOREIGN KEY (owner_id) REFERENCES users(oid) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE TABLE statuses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    color VARCHAR(7) NOT NULL,
    board_id CHAR(10) NOT NULL,
    global_settings_id INT NOT NULL,
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status_name_length CHECK (TRIM(name) != '' AND LENGTH(TRIM(name)) <= 50),
    CONSTRAINT chk_status_description CHECK (description IS NULL OR (TRIM(description) != '' AND LENGTH(TRIM(description)) <= 200)),
    CONSTRAINT chk_status_color CHECK (color REGEXP '^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$'),
    CONSTRAINT fk_global_settings FOREIGN KEY (global_settings_id) REFERENCES global_settings(id),
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    UNIQUE KEY unique_status_per_board (name, board_id)
);
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    board_id CHAR(10) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    assignees VARCHAR(30),
    status_id INT,
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_task_title_length CHECK (TRIM(title) != '' AND LENGTH(TRIM(title)) <= 100),
    CONSTRAINT chk_task_description CHECK (description IS NULL OR (TRIM(description) != '' AND LENGTH(TRIM(description)) <= 500)),
    CONSTRAINT chk_task_assignees CHECK (assignees IS NULL OR (TRIM(assignees) != '' AND LENGTH(TRIM(assignees)) <= 30)),
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    FOREIGN KEY (status_id) REFERENCES statuses(id) ON DELETE SET NULL
);
CREATE TABLE collaborators (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    board_id CHAR(10) NOT NULL,
    access_right ENUM('READ', 'WRITE') NOT NULL DEFAULT 'READ',
    added_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(oid) ON DELETE CASCADE,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    UNIQUE KEY unique_collaborator_per_board (user_id, board_id)
);