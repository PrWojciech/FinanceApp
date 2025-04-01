-- Przykladowy uzytkownik
INSERT INTO users (id, email, password) VALUES
(1, 'test@example.com', '$2a$10$KbQiJqYj1w6q1K1VKsUHeekZkhrIYTSpRgykW8s7aT3lXc3ePP6Zy'); -- hasło: test123

-- Rola
INSERT INTO user_roles (user_id, roles) VALUES
(1, 'USER');
