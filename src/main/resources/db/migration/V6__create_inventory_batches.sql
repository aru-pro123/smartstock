
CREATE TABLE inventory_batches (
                                   id BIGSERIAL PRIMARY KEY,

                                   inventory_id BIGINT NOT NULL
                                       REFERENCES store_inventory(id),

                                   batch_number VARCHAR(100) NOT NULL,

                                   quantity_received NUMERIC(12,3) NOT NULL
                                       CHECK (quantity_received > 0),

                                   quantity_remaining NUMERIC(12,3) NOT NULL
                                       CHECK (quantity_remaining >= 0),

                                   manufactured_date DATE,

                                   expiry_date DATE,

                                   received_at TIMESTAMPTZ NOT NULL
                                       DEFAULT CURRENT_TIMESTAMP,

                                   created_at TIMESTAMPTZ NOT NULL
                                       DEFAULT CURRENT_TIMESTAMP,

                                   updated_at TIMESTAMPTZ NOT NULL
                                       DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT uk_inventory_batch
                                       UNIQUE (inventory_id, batch_number),

                                   CONSTRAINT chk_batch_quantity
                                       CHECK (quantity_remaining <= quantity_received),

                                   CONSTRAINT chk_batch_dates
                                       CHECK (
                                           manufactured_date IS NULL
                                               OR expiry_date IS NULL
                                               OR expiry_date >= manufactured_date
                                           )
);

CREATE INDEX idx_inventory_batches_inventory
    ON inventory_batches(inventory_id);

CREATE INDEX idx_inventory_batches_expiry
    ON inventory_batches(expiry_date);

CREATE INDEX idx_inventory_batches_fefo
    ON inventory_batches(
                         inventory_id,
                         expiry_date,
                         received_at,
                         id
        )
    WHERE quantity_remaining > 0;


CREATE TABLE batch_movements (
                                 id BIGSERIAL PRIMARY KEY,

                                 batch_id BIGINT NOT NULL
                                     REFERENCES inventory_batches(id),

                                 stock_movement_id BIGINT NOT NULL
                                     REFERENCES stock_movements(id),

                                 quantity_change NUMERIC(12,3) NOT NULL
                                     CHECK (quantity_change <> 0),

                                 quantity_before NUMERIC(12,3) NOT NULL
                                     CHECK (quantity_before >= 0),

                                 quantity_after NUMERIC(12,3) NOT NULL
                                     CHECK (quantity_after >= 0),

                                 created_at TIMESTAMPTZ NOT NULL
                                     DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT chk_batch_movement_balance
                                     CHECK (
                                         quantity_after =
                                         quantity_before + quantity_change
                                         ),

                                 CONSTRAINT uk_batch_stock_movement
                                     UNIQUE (batch_id, stock_movement_id)
);

CREATE INDEX idx_batch_movements_batch
    ON batch_movements(batch_id);

CREATE INDEX idx_batch_movements_stock
    ON batch_movements(stock_movement_id);
