CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            category_code VARCHAR(30) NOT NULL UNIQUE,
                            name VARCHAR(100) NOT NULL,
                            description VARCHAR(255),
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,

                          sku VARCHAR(50) NOT NULL UNIQUE,
                          barcode VARCHAR(50) UNIQUE,

                          name VARCHAR(200) NOT NULL,
                          brand VARCHAR(100),

                          category_id BIGINT NOT NULL REFERENCES categories(id),

                          unit_of_measure VARCHAR(20) NOT NULL,

                          selling_price NUMERIC(12,2) NOT NULL
                              CHECK (selling_price >= 0),

                          taxable BOOLEAN NOT NULL DEFAULT FALSE,
                          perishable BOOLEAN NOT NULL DEFAULT FALSE,

                          shelf_life_days INTEGER
                              CHECK (shelf_life_days IS NULL OR shelf_life_days > 0),

                          active BOOLEAN NOT NULL DEFAULT TRUE,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_name ON products(name);