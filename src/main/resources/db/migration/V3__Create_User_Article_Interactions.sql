CREATE TABLE user_article_interactions (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           user_id BIGINT NOT NULL,


                                           article_url TEXT NOT NULL,


                                           article_url_hash CHAR(64) NOT NULL,

                                           title VARCHAR(1000),
                                           description TEXT,
                                           thumbnail_url TEXT,
                                           site_name VARCHAR(255),

                                           is_saved BOOLEAN DEFAULT FALSE,
                                           saved_at DATETIME(6),
                                           is_read BOOLEAN DEFAULT FALSE,
                                           read_at DATETIME(6),
                                           memo TEXT,

                                           created_at DATETIME(6) NOT NULL,
                                           updated_at DATETIME(6),

                                           CONSTRAINT uk_user_article UNIQUE (user_id, article_url_hash)
);
