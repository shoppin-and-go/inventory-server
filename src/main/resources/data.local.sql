insert into cart (id, code, created_at, updated_at) values
    (RANDOM_UUID(), 'cart-test_1', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (RANDOM_UUID(), 'cart-test_2', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (RANDOM_UUID(), 'cart-test_3', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

insert into cart_connection (id, cart_id, device_id, created_at, updated_at) values
    (RANDOM_UUID(), (select id from cart where code = 'cart-test_1'), 'device-test_1', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (RANDOM_UUID(), (select id from cart where code = 'cart-test_2'), 'device-test_2', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (RANDOM_UUID(), (select id from cart where code = 'cart-test_3'), 'device-test_3', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

insert into product (id, code, name, price, created_at, updated_at) values
    (RANDOM_UUID(), 'product-test_1', 'Apple', 100.00, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (RANDOM_UUID(), 'product-test_2', 'Banana', 200.00, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (RANDOM_UUID(), 'product-test_3', 'Carrot', 300.00, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());