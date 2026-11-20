
CREATE TABLE purchase_orders (
                                 id BIGSERIAL PRIMARY KEY,

                                 order_number VARCHAR(100) NOT NULL UNIQUE,

                                 supplier_id BIGINT NOT NULL
                                     REFERENCES suppliers(id),

                                 store_id BIGINT NOT NULL
                                     REFERENCES stores(id),

                                 status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

                                 expected_delivery_date DATE,

                                 notes VARCHAR(500),

                                 ordered_at TIMESTAMPTZ,

                                 received_at TIMESTAMPTZ,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT chk_purchase_order_status
                                     CHECK (
                                         status IN (
                                                    'DRAFT',
                                                    'ORDERED',
                                                    'PARTIALLY_RECEIVED',
                                                    'RECEIVED',
                                                    'CANCELLED'
                                             )
                                         )
);


CREATE TABLE purchase_order_items (
                                      id BIGSERIAL PRIMARY KEY,

                                      purchase_order_id BIGINT NOT NULL
                                          REFERENCES purchase_orders(id),

                                      product_id BIGINT NOT NULL
                                          REFERENCES products(id),

                                      quantity_ordered NUMERIC(12, 3) NOT NULL,

                                      quantity_received NUMERIC(12, 3) NOT NULL DEFAULT 0,

                                      unit_cost NUMERIC(12, 4) NOT NULL,

                                      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT uq_purchase_order_product
                                          UNIQUE (purchase_order_id, product_id),

                                      CONSTRAINT chk_po_quantity_ordered
                                          CHECK (quantity_ordered > 0),

                                      CONSTRAINT chk_po_quantity_received
                                          CHECK (
                                              quantity_received >= 0
                                                  AND quantity_received <= quantity_ordered
                                              ),

                                      CONSTRAINT chk_po_unit_cost
                                          CHECK (unit_cost >= 0)
);


CREATE INDEX idx_purchase_orders_supplier
    ON purchase_orders(supplier_id);

CREATE INDEX idx_purchase_orders_store_status
    ON purchase_orders(store_id, status);

CREATE INDEX idx_purchase_order_items_product
    ON purchase_order_items(product_id);
