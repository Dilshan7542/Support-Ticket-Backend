ALTER TABLE ticket
    ADD COLUMN company_id BIGINT NULL AFTER customer_id,
    ADD COLUMN category_id BIGINT NULL AFTER department_id;

CREATE INDEX idx_ticket_department_id ON ticket (department_id);
CREATE INDEX idx_ticket_category_id ON ticket (category_id);

UPDATE ticket t
JOIN ticket_category c ON c.code = t.category_code
SET
    t.category_id = COALESCE(t.category_id, c.id),
    t.department_id = COALESCE(t.department_id, c.department_id),
    t.company_id = COALESCE(t.company_id, c.company_id)
WHERE t.category_code IS NOT NULL;

UPDATE ticket t
JOIN department d ON d.id = t.department_id
SET t.company_id = COALESCE(t.company_id, d.company_id)
WHERE t.department_id IS NOT NULL;

UPDATE ticket t
LEFT JOIN company c ON c.id = t.company_id
SET t.company_id = NULL
WHERE t.company_id IS NOT NULL
  AND c.id IS NULL;

UPDATE ticket t
LEFT JOIN department d ON d.id = t.department_id
SET t.department_id = NULL
WHERE t.department_id IS NOT NULL
  AND d.id IS NULL;

UPDATE ticket t
LEFT JOIN ticket_category c ON c.id = t.category_id
SET t.category_id = NULL
WHERE t.category_id IS NOT NULL
  AND c.id IS NULL;

UPDATE ticket t
LEFT JOIN app_user u ON u.id = t.assigned_staff_id
SET t.assigned_staff_id = NULL
WHERE t.assigned_staff_id IS NOT NULL
  AND u.id IS NULL;

ALTER TABLE department
    ADD CONSTRAINT fk_department_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL;

ALTER TABLE ticket_category
    ADD CONSTRAINT fk_ticket_category_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_ticket_category_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL;

ALTER TABLE ticket
    ADD CONSTRAINT fk_ticket_customer FOREIGN KEY (customer_id) REFERENCES app_user(id),
    ADD CONSTRAINT fk_ticket_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_ticket_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_ticket_category FOREIGN KEY (category_id) REFERENCES ticket_category(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_ticket_assigned_staff FOREIGN KEY (assigned_staff_id) REFERENCES app_user(id) ON DELETE SET NULL;
