-- 테스트 계정 생성
-- 비밀번호: password123
INSERT INTO account (email, password, created_at) 
VALUES ('test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkK', NOW());
