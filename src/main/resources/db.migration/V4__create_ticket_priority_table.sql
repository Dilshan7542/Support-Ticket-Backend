CREATE TABLE IF NOT EXISTS ticket_priority (
                                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 name VARCHAR(100) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL
    );

INSERT INTO ticket_priority (name, code, description, status, created_at)
SELECT 'Low', 'LOW', 'Low priority ticket', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_priority WHERE code = 'LOW');

INSERT INTO ticket_priority (name, code, description, status, created_at)
SELECT 'Medium', 'MEDIUM', 'Medium priority ticket', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_priority WHERE code = 'MEDIUM');

INSERT INTO ticket_priority (name, code, description, status, created_at)
SELECT 'High', 'HIGH', 'High priority ticket', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_priority WHERE code = 'HIGH');

INSERT INTO ticket_priority (name, code, description, status, created_at)
SELECT 'Critical', 'CRITICAL', 'Critical priority ticket', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ticket_priority WHERE code = 'CRITICAL');
