CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    balance DECIMAL(19, 2) DEFAULT NULL,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE vouchers (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(19, 2) NOT NULL,
    tour_type VARCHAR(50) NOT NULL,
    transfer_type VARCHAR(50) NOT NULL,
    hotel_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    arrival_date DATE NOT NULL,
    eviction_date DATE NOT NULL,
    user_id UUID,
    is_hot BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);
