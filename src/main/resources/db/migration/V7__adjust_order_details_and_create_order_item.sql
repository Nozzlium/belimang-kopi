ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS total_distance BIGINT,
    ADD COLUMN IF NOT EXISTS estimated_delivery_time BIGINT,
    ADD COLUMN IF NOT EXISTS total_price BIGINT;


CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT REFERENCES merchant_items(id) ON UPDATE CASCADE ON DELETE CASCADE,
    order_detail_id BIGINT REFERENCES order_details(id) ON UPDATE CASCADE ON DELETE CASCADE,
    category VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    price BIGINT NOT NULL,
    image_url VARCHAR,
    quantity INT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);