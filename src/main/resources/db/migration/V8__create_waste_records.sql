
CREATE TABLE waste_records (

                               id BIGSERIAL PRIMARY KEY,

                               inventory_id BIGINT NOT NULL
                                   REFERENCES store_inventory(id),

                               batch_id BIGINT NOT NULL
                                   REFERENCES inventory_batches(id),

                               stock_movement_id BIGINT NOT NULL
                                   REFERENCES stock_movements(id),

                               waste_reason VARCHAR(30) NOT NULL,

                               quantity NUMERIC(12,3) NOT NULL
                                   CHECK (quantity > 0),

                               unit_cost NUMERIC(12,4) NOT NULL
                                   CHECK (unit_cost >= 0),

                               total_loss NUMERIC(20,2) NOT NULL
                                   CHECK (total_loss >= 0),

                               notes VARCHAR(500),

                               created_at TIMESTAMPTZ NOT NULL
                                   DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT uk_waste_stock_movement
                                   UNIQUE (stock_movement_id),

                               CONSTRAINT chk_waste_reason
                                   CHECK (
                                       waste_reason IN (
                                                        'EXPIRED',
                                                        'DAMAGED',
                                                        'SPOILAGE',
                                                        'QUALITY_ISSUE',
                                                        'TEMPERATURE_ABUSE',
                                                        'CONTAMINATION',
                                                        'OTHER'
                                           )
                                       ),

                               CONSTRAINT chk_waste_total_loss
                                   CHECK (
                                       total_loss = ROUND(quantity * unit_cost, 2)
                                       )
);

CREATE INDEX idx_waste_inventory
    ON waste_records(inventory_id);

CREATE INDEX idx_waste_batch
    ON waste_records(batch_id);

CREATE INDEX idx_waste_reason
    ON waste_records(waste_reason);

CREATE INDEX idx_waste_created_at
    ON waste_records(created_at);
