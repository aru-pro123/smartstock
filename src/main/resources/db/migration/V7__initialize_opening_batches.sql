INSERT INTO inventory_batches (
    inventory_id,
    batch_number,
    quantity_received,
    quantity_remaining,
    received_at,
    created_at,
    updated_at
)
SELECT
    id,
    'OPENING-' || id,
    quantity_on_hand,
    quantity_on_hand,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM store_inventory
WHERE quantity_on_hand > 0
  AND NOT EXISTS (
    SELECT 1
    FROM inventory_batches b
    WHERE b.inventory_id = store_inventory.id
);