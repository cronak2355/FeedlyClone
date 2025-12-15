-- 키워드 그룹
CREATE TABLE keyword_groups (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(50),   -- company / topic
  name VARCHAR(255)
);

-- 실제 검색에 쓰이는 alias
CREATE TABLE keyword_aliases (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_id BIGINT,
  keyword VARCHAR(255),
  FOREIGN KEY (group_id) REFERENCES keyword_groups(id)
);