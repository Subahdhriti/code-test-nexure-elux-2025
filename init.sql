-- Sample data for testing
-- This file can be used to initialize the database with test data

INSERT INTO products (id, name, base_price, country) VALUES
    ('laptop-1', 'Professional Laptop', 1000.0, 'india'),
    ('laptop-2', 'Gaming Laptop', 1500.0, 'usa'),
    ('phone-1', 'Smartphone Pro', 800.0, 'germany'),
    ('phone-2', 'Budget Phone', 300.0, 'india'),
    ('tablet-1', 'Premium Tablet', 600.0, 'usa')
ON CONFLICT (id) DO NOTHING;

-- Sample discounts (optional)
INSERT INTO product_discounts (product_id, discount_id, percent) VALUES
    ('laptop-1', 'WELCOME10', 10.0),
    ('phone-1', 'WINTER2026', 15.0)
ON CONFLICT (product_id, discount_id) DO NOTHING;

-- Sample country VAT rates
INSERT INTO country_vat (country, vat) VALUES
    ('india', 18.0),
    ('usa', 7.5),
    ('germany', 19.0)
ON CONFLICT (country) DO NOTHING;