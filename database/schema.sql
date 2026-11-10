-- EstatePro relational schema reference for MySQL 8.
-- Spring Data JPA creates/updates the actual mapped schema at startup.
CREATE DATABASE IF NOT EXISTS estatepro
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE estatepro;

CREATE TABLE IF NOT EXISTS users (
  id CHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  phone VARCHAR(30),
  role VARCHAR(20) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  INDEX idx_user_email (email)
);

CREATE TABLE IF NOT EXISTS properties (
  id CHAR(36) NOT NULL PRIMARY KEY,
  title VARCHAR(150) NOT NULL,
  description VARCHAR(5000) NOT NULL,
  property_type VARCHAR(40) NOT NULL,
  listing_type VARCHAR(20) NOT NULL,
  city VARCHAR(80) NOT NULL,
  locality VARCHAR(120) NOT NULL,
  address VARCHAR(250),
  price DECIMAL(14,2) NOT NULL,
  bedrooms INT,
  bathrooms INT,
  area_sqft INT,
  image_url VARCHAR(400) NOT NULL,
  status VARCHAR(20) NOT NULL,
  owner_id CHAR(36),
  agent_id CHAR(36),
  featured BOOLEAN NOT NULL DEFAULT FALSE,
  moderation_note VARCHAR(500),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_property_owner FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT fk_property_agent FOREIGN KEY (agent_id) REFERENCES users(id),
  INDEX idx_property_status_city (status, city),
  INDEX idx_property_price (price),
  INDEX idx_property_created (created_at)
);

CREATE TABLE IF NOT EXISTS favorites (
  id CHAR(36) NOT NULL PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  property_id CHAR(36) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uk_favorite_user_property UNIQUE (user_id, property_id),
  CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_favorite_property FOREIGN KEY (property_id) REFERENCES properties(id)
);

CREATE TABLE IF NOT EXISTS enquiries (
  id CHAR(36) NOT NULL PRIMARY KEY,
  property_id CHAR(36) NOT NULL,
  buyer_id CHAR(36) NOT NULL,
  agent_id CHAR(36),
  message VARCHAR(2000) NOT NULL,
  status VARCHAR(30) NOT NULL,
  internal_note VARCHAR(2000),
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_enquiry_property FOREIGN KEY (property_id) REFERENCES properties(id),
  CONSTRAINT fk_enquiry_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
  CONSTRAINT fk_enquiry_agent FOREIGN KEY (agent_id) REFERENCES users(id),
  INDEX idx_enquiry_agent_status (agent_id, status)
);

CREATE TABLE IF NOT EXISTS appointments (
  id CHAR(36) NOT NULL PRIMARY KEY,
  property_id CHAR(36) NOT NULL,
  buyer_id CHAR(36) NOT NULL,
  agent_id CHAR(36),
  visit_date DATE NOT NULL,
  visit_time TIME NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uk_agent_visit_slot UNIQUE (agent_id, visit_date, visit_time),
  CONSTRAINT fk_appointment_property FOREIGN KEY (property_id) REFERENCES properties(id),
  CONSTRAINT fk_appointment_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
  CONSTRAINT fk_appointment_agent FOREIGN KEY (agent_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bookings (
  id CHAR(36) NOT NULL PRIMARY KEY,
  reference VARCHAR(24) NOT NULL UNIQUE,
  property_id CHAR(36) NOT NULL,
  buyer_id CHAR(36) NOT NULL,
  agent_id CHAR(36),
  amount DECIMAL(14,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_booking_property FOREIGN KEY (property_id) REFERENCES properties(id),
  CONSTRAINT fk_booking_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
  CONSTRAINT fk_booking_agent FOREIGN KEY (agent_id) REFERENCES users(id),
  INDEX idx_booking_buyer_date (buyer_id, created_at)
);

CREATE TABLE IF NOT EXISTS payments (
  id CHAR(36) NOT NULL PRIMARY KEY,
  booking_id CHAR(36) NOT NULL,
  transaction_reference VARCHAR(32) NOT NULL UNIQUE,
  amount DECIMAL(14,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE IF NOT EXISTS contact_messages (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL,
  subject VARCHAR(150) NOT NULL,
  message VARCHAR(3000) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
