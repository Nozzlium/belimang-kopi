ALTER TABLE orders
ADD COLUMN total_distance BIGINT,
ADD COLUMN estimated_delivery_time BIGINT,
ADD COLUMN total_price BIGINT;

ALTER TABLE order_details DROP COLUMN items;

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