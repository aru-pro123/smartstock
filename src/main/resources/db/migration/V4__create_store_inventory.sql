CREATE TABLE store_inventory (
                                 id BIGSERIAL PRIMARY KEY,

                                 store_id BIGINT NOT NULL
                                     REFERENCES stores(id),

                                 product_id BIGINT NOT NULL
                                     REFERENCES products(id),

                                 quantity_on_hand NUMERIC(12,3)
                                     NOT NULL DEFAULT 0
                                     CHECK (quantity_on_hand >= 0),

                                 quantity_reserved NUMERIC(12,3)
                                     NOT NULL DEFAULT 0
                                     CHECK (quantity_reserved >= 0),

                                 reorder_level NUMERIC(12,3)
                                     NOT NULL DEFAULT 0
                                     CHECK (reorder_level >= 0),

                                 reorder_quantity NUMERIC(12,3)
                                     NOT NULL DEFAULT 0
                                     CHECK (reorder_quantity >= 0),

                                 active BOOLEAN NOT NULL DEFAULT TRUE,

                                 created_at TIMESTAMPTZ
                                     NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 updated_at TIMESTAMPTZ
                                     NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT uk_store_inventory
                                     UNIQUE (store_id, product_id),

                                 CONSTRAINT chk_reserved_quantity
                                     CHECK (quantity_reserved <= quantity_on_hand)
);

CREATE INDEX idx_store_inventory_store
    ON store_inventory(store_id);

CREATE INDEX idx_store_inventory_product
    ON store_inventory(product_id);