CREATE TABLE stores (
                        id BIGSERIAL PRIMARY KEY,

                        store_code VARCHAR(20) NOT NULL UNIQUE,

                        name VARCHAR(150) NOT NULL,

                        city VARCHAR(100) NOT NULL,

                        province VARCHAR(50) NOT NULL,

                        address VARCHAR(255),

                        active BOOLEAN NOT NULL DEFAULT TRUE,

                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);