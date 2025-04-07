INSERT INTO users (id, username, password, role, phone_number, balance, active)
VALUES
(RANDOM_UUID(), 'admin', '$2a$10$i1VgLZx5Qt4kiAkXTmQoB.iqxo2pqPWlUQLDKFi9P1c3DGEBlfC56', 'ADMIN', null, null, true),
(RANDOM_UUID(), 'manager', '$2a$10$AzIp0y7HfPlO6znkzIPGHuIDDC6syOtjCxb1.fs/guu6UFQy7FEpK', 'MANAGER', null, null, true),
(RANDOM_UUID(), 'user1', '$2a$10$KgaO/kHAtFeBUlcShKx6u.lrhqbyfYpRyBvbqv8sTbtD1POs5EeE2', 'USER', null, 1000.0, true),
(RANDOM_UUID(), 'user2', '$2a$10$KgaO/kHAtFeBUlcShKx6u.lrhqbyfYpRyBvbqv8sTbtD1POs5EeE2', 'USER', null, 500.0, true);

INSERT INTO vouchers (id, title, description, price, tour_type, transfer_type, hotel_type, status, arrival_date, eviction_date, is_hot)
VALUES
        (RANDOM_UUID(), 'Beach Holiday', 'A relaxing vacation by the beach', 500.00, 'ECO', 'BUS', 'FIVE_STARS', 'CREATED', '2025-06-15', '2025-06-22', true),
        (RANDOM_UUID(), 'Mountain Trekking', 'An adventurous mountain trekking tour', 400.00, 'ADVENTURE', 'PRIVATE_CAR', 'THREE_STARS', 'CREATED', '2025-07-01', '2025-07-10', false),
        (RANDOM_UUID(), 'City Sightseeing', 'Explore the beauty of the city landmarks', 300.00, 'CULTURAL', 'TRAIN', 'FOUR_STARS', 'CREATED', '2025-05-01', '2025-05-07', true),
        (RANDOM_UUID(), 'Wine Tasting', 'Discover the finest wines in the region', 600.00, 'WINE', 'SHIP', 'FOUR_STARS', 'CREATED', '2025-08-10', '2025-08-17', true),
        (RANDOM_UUID(), 'Health Retreat', 'Relax and rejuvenate at a spa retreat', 750.00, 'HEALTH', 'PRIVATE_CAR', 'FIVE_STARS', 'CREATED', '2025-09-05', '2025-09-12', true),
        (RANDOM_UUID(), 'Safari Adventure', 'Experience the thrill of a safari in Africa', 1200.00, 'SAFARI', 'PLANE', 'FIVE_STARS', 'CREATED', '2025-10-01', '2025-10-14', false),
        (RANDOM_UUID(), 'Sports Tour', 'Watch live sports events and explore new places', 350.00, 'SPORTS', 'MINIBUS', 'TWO_STARS', 'CREATED', '2025-07-15', '2025-07-20', true),
        (RANDOM_UUID(), 'Luxury Beach Resort', 'Stay at an exclusive beach resort', 1500.00, 'LEISURE', 'PLANE', 'FIVE_STARS', 'CREATED', '2025-12-01', '2025-12-10', true),
        (RANDOM_UUID(), 'Cultural Exploration', 'Discover the art and history of ancient cities', 400.00, 'CULTURAL', 'TRAIN', 'THREE_STARS', 'CREATED', '2025-04-10', '2025-04-17', false),
        (RANDOM_UUID(), 'Jeep Safari', 'Ride through the wilderness in a jeep safari', 800.00, 'SAFARI', 'JEEPS', 'FOUR_STARS', 'CREATED', '2025-08-20', '2025-08-25', true);
