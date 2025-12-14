-- V1__Create_User_Feeds_Table.sql
-- 사용자 피드 구독 정보 저장 테이블

CREATE TABLE IF NOT EXISTS user_feeds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL DEFAULT 1,
    feed_url VARCHAR(500) NOT NULL,
    feed_title VARCHAR(500),
    feed_description TEXT,
    feed_type VARCHAR(50) DEFAULT 'RSS',
    favicon_url VARCHAR(500),
    category VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_fetched_at DATETIME,
    
    UNIQUE KEY uk_user_feed (user_id, feed_url),
    INDEX idx_user_id (user_id),
    INDEX idx_feed_type (feed_type),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 샘플 RSS 소스 (인기 기술 블로그)
CREATE TABLE IF NOT EXISTS popular_feeds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feed_url VARCHAR(500) NOT NULL UNIQUE,
    site_url VARCHAR(500),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    favicon_url VARCHAR(500),
    subscriber_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table account(
    id bigint auto_increment primary key
    ,email varchar(128) not null unique
    ,password varchar(256) not null
);


-- 인기 피드 초기 데이터
INSERT INTO popular_feeds (feed_url, site_url, title, description, category, subscriber_count) VALUES
('https://techcrunch.com/feed/', 'https://techcrunch.com', 'TechCrunch', 'Startup and technology news', 'Technology', 15000),
('https://www.theverge.com/rss/index.xml', 'https://www.theverge.com', 'The Verge', 'Technology, science, art, and culture', 'Technology', 12000),
('https://feeds.arstechnica.com/arstechnica/index', 'https://arstechnica.com', 'Ars Technica', 'Serving the Technologist', 'Technology', 10000),
('https://www.wired.com/feed/rss', 'https://www.wired.com', 'WIRED', 'The latest in technology, science, culture', 'Technology', 11000),
('https://github.blog/feed/', 'https://github.blog', 'GitHub Blog', 'Updates, ideas, and inspiration from GitHub', 'Development', 8000),
('https://blog.jetbrains.com/feed/', 'https://blog.jetbrains.com', 'JetBrains Blog', 'Developer tools company blog', 'Development', 5000),
('https://spring.io/blog.atom', 'https://spring.io/blog', 'Spring Blog', 'Spring Framework official blog', 'Development', 6000),
('https://kotlinlang.org/feed.xml', 'https://kotlinlang.org', 'Kotlin Blog', 'Kotlin programming language news', 'Development', 4000),
('https://news.ycombinator.com/rss', 'https://news.ycombinator.com', 'Hacker News', 'Social news for hackers', 'News', 20000),
('https://www.reddit.com/r/programming/.rss', 'https://reddit.com/r/programming', 'r/programming', 'Computer programming subreddit', 'Reddit', 9000),
('https://www.reddit.com/r/kotlin/.rss', 'https://reddit.com/r/kotlin', 'r/kotlin', 'Kotlin subreddit', 'Reddit', 3000),
('https://www.reddit.com/r/java/.rss', 'https://reddit.com/r/java', 'r/java', 'Java subreddit', 'Reddit', 5000);
