
CREATE TABLE stock_movements (
                                 id BIGSERIAL PRIMARY KEY,

                                 inventory_id BIGINT NOT NULL
                                     REFERENCES store_inventory(id),

                                 movement_type VARCHAR(30) NOT NULL,

                                 quantity_change NUMERIC(12,3) NOT NULL
                                     CHECK (quantity_change <> 0),

                                 quantity_before NUMERIC(12,3) NOT NULL
                                     CHECK (quantity_before >= 0),

                                 quantity_after NUMERIC(12,3) NOT NULL
                                     CHECK (quantity_after >= 0),

                                 reference_number VARCHAR(100),

                                 reason VARCHAR(500),

                                 created_at TIMESTAMPTZ NOT NULL
                                     DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT chk_stock_movement_balance
                                     CHECK (
                                         quantity_after =
                                         quantity_before + quantity_change
                                         ),

                                 CONSTRAINT chk_stock_movement_type
                                     CHECK (
                                         movement_type IN (
                                                           'RECEIVE',
                                                           'SALE',
                                                           'DAMAGE',
                                                           'RETURN',
                                                           'ADJUSTMENT'
                                             )
                                         )
);

CREATE INDEX idx_stock_movements_inventory
    ON stock_movements(inventory_id);

CREATE INDEX idx_stock_movements_created_at
    ON stock_movements(created_at);
