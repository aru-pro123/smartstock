CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,

                             store_id BIGINT NOT NULL,

                             department_code VARCHAR(30) NOT NULL,

                             name VARCHAR(100) NOT NULL,

                             description VARCHAR(255),

                             active BOOLEAN NOT NULL DEFAULT TRUE,

                             created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_department_store
                                 FOREIGN KEY (store_id)
                                     REFERENCES stores(id),

                             CONSTRAINT uk_store_department_code
                                 UNIQUE (store_id, department_code)
);

CREATE INDEX idx_departments_store_id
    ON departments(store_id);