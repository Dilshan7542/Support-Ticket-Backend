drop database if exists ticket_v1;
CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(30),
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    active_session_id VARCHAR(80),
    refresh_token_hash VARCHAR(128),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS company (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(64) NOT NULL,
    company_id BIGINT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    UNIQUE KEY uk_department_company_code (company_id, code),
    CONSTRAINT fk_department_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ticket_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NULL,
    department_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    INDEX idx_ticket_category_department_id (department_id),
    CONSTRAINT fk_ticket_category_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_category_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ticket_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    company_id BIGINT NULL,
    department_id BIGINT NULL,
    category_id BIGINT NULL,
    assigned_staff_id BIGINT NULL,
    subject VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category_code VARCHAR(64),
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    INDEX idx_ticket_customer_id (customer_id),
    INDEX idx_ticket_department_id (department_id),
    INDEX idx_ticket_category_id (category_id),
    CONSTRAINT fk_ticket_customer FOREIGN KEY (customer_id) REFERENCES app_user(id),
    CONSTRAINT fk_ticket_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_category FOREIGN KEY (category_id) REFERENCES ticket_category(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_assigned_staff FOREIGN KEY (assigned_staff_id) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ticket_attachment (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  ticket_id BIGINT NULL,
                                                  uploaded_by_user_id BIGINT NOT NULL,
                                                  original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120),
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_ticket_attachment_ticket_id (ticket_id),
    CONSTRAINT fk_ticket_attachment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE SET NULL,
    CONSTRAINT fk_ticket_attachment_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES app_user(id)
    );


CREATE TABLE IF NOT EXISTS ticket_reply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    sender_user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_ticket_reply_ticket_id (ticket_id),
    CONSTRAINT fk_ticket_reply_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(id),
    CONSTRAINT fk_ticket_reply_sender FOREIGN KEY (sender_user_id) REFERENCES app_user(id)
);

CREATE TABLE IF NOT EXISTS ticket_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by_user_id BIGINT,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL,
    INDEX idx_ticket_status_history_ticket_id (ticket_id),
    CONSTRAINT fk_ticket_status_history_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(id),
    CONSTRAINT fk_ticket_status_history_changed_by FOREIGN KEY (changed_by_user_id) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ai_prediction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    predicted_category VARCHAR(50),
    predicted_priority VARCHAR(20),
    suggested_department_id BIGINT,
    confidence_score DECIMAL(5,2),
    raw_response TEXT,
    created_at DATETIME NOT NULL,
    INDEX idx_ai_prediction_ticket_id (ticket_id),
    CONSTRAINT fk_ai_prediction_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(id),
    CONSTRAINT fk_ai_prediction_suggested_department FOREIGN KEY (suggested_department_id) REFERENCES department(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS activity_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trace_id VARCHAR(80),
    user_id BIGINT NULL,
    http_method VARCHAR(10),
    endpoint VARCHAR(255),
    action VARCHAR(120),
    request_body TEXT,
    response_body TEXT,
    response_status INT,
    ip_address VARCHAR(80),
    user_agent VARCHAR(255),
    encryption_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    execution_time_ms BIGINT,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

CREATE TABLE IF NOT EXISTS mail_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient VARCHAR(150) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT NULL,
    created_at DATETIME NOT NULL
);

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'New', 'NEW', 'Ticket has been created', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'NEW');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Assigned', 'ASSIGNED', 'Ticket has been assigned', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'ASSIGNED');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'In Progress', 'IN_PROGRESS', 'Ticket is being handled', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'IN_PROGRESS');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Waiting For Customer', 'WAITING_FOR_CUSTOMER', 'Waiting for customer response', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'WAITING_FOR_CUSTOMER');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Resolved', 'RESOLVED', 'Ticket has been resolved', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'RESOLVED');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Closed', 'CLOSED', 'Ticket has been closed', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'CLOSED');

INSERT INTO ticket_status (name, code, description, status, created_at)
SELECT 'Reopened', 'REOPENED', 'Ticket has been reopened', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_status WHERE code = 'REOPENED');
