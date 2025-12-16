import { useState, useEffect, useCallback } from 'react';
import '../styles/discover.css';

interface RedditPost {
    title: string;
    link: string;
    author: string;
    subreddit: string;
    publishedDate?: string;
    selfText?: string;
    thumbnailUrl?: string;
}

interface SubredditFeed {
    subreddit: string;
    feedUrl: string;
    title: string;
    description?: string;
    iconUrl?: string;
    posts: RedditPost[];
    isFollowed?: boolean;
}

const POPULAR_SUBREDDITS = [
    'programming', 'kotlin', 'java', 'javascript', 'python',
    'webdev', 'android', 'technology', 'news', 'worldnews'
];

export default function RedditPage() {
    const [searchQuery, setSearchQuery] = useState('');
    const [subreddit, setSubreddit] = useState<SubredditFeed | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [selectedSub, setSelectedSub] = useState('programming');
    const [imageErrors, setImageErrors] = useState<Set<string>>(new Set());

    const fetchSubreddit = useCallback(async (sub: string) => {
        if (!sub) return;

        setLoading(true);
        setError(null);
        setSubreddit(null);

        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 15000);

            const response = await fetch(`http://localhost:8080/api/reddit?subreddit=${encodeURIComponent(sub)}`, {
                credentials: 'include',
                signal: controller.signal,
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const data = await response.json();

            if (data.error) {
                throw new Error(data.error);
            }

            // subreddit 필드가 없으면 루트 객체를 사용
            const feedData = data.subreddit || data;

            if (!feedData || !feedData.posts) {
                throw new Error('Invalid response format');
            }

            setSubreddit(feedData);
            setImageErrors(new Set());
        } catch (err) {
            console.error('Fetch error:', err);
            if (err instanceof Error && err.name === 'AbortError') {
                setError('요청 시간이 초과되었습니다. 다시 시도해주세요.');
            } else {
                setError('서브레딧을 불러오는데 실패했습니다.');
            }
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (selectedSub) {
            fetchSubreddit(selectedSub);
        }
    }, [selectedSub, fetchSubreddit]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            const cleanQuery = searchQuery.replace(/^r\//, '').trim();
            setSelectedSub(cleanQuery);
            setSearchQuery('');
        }
    };

    const handleImageError = (postLink: string) => {
        setImageErrors(prev => new Set(prev).add(postLink));
    };

    const formatDate = (dateString?: string) => {
        if (!dateString) return '';
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return '';
            return date.toLocaleDateString('ko-KR', {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return '';
        }
    };

    const isValidThumbnail = (url?: string): boolean => {
        if (!url) return false;
        // 유효한 이미지 URL인지 확인
        return url.startsWith('http') &&
            !url.includes('self') &&
            !url.includes('default') &&
            !url.includes('nsfw');
    };

    return (
        <>
            {/* Header */}
            <header className="discover-header">
                <div className="header-top">
                    <h1>
                        <i className="bi bi-reddit me-2" style={{ color: '#FF4500' }}></i>
                        Reddit
                    </h1>

                    <form onSubmit={handleSearch} className="search-form">
                        <input
                            type="text"
                            placeholder="서브레딧 검색 (예: programming)"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="search-input"
                        />
                        <button type="submit" className="search-btn">
                            <i className="bi bi-search"></i>
                        </button>
                    </form>
                </div>

                {/* Popular Subreddits */}
                <div className="category-filter">
                    {POPULAR_SUBREDDITS.map((sub) => (
                        <button
                            key={sub}
                            className={`category-btn ${selectedSub === sub ? 'active' : ''}`}
                            onClick={() => setSelectedSub(sub)}
                            disabled={loading}
                        >
                            r/{sub}
                        </button>
                    ))}
                </div>
            </header>

            {/* Main Content */}
            <main className="discover-content">
                {loading ? (
                    <div className="loading-container">
                        <div className="spinner"></div>
                        <p>Loading r/{selectedSub}...</p>
                    </div>
                ) : error ? (
                    <div className="error-container">
                        <i className="bi bi-exclamation-triangle" style={{ fontSize: '2rem', color: '#dc3545' }}></i>
                        <p>{error}</p>
                        <button onClick={() => fetchSubreddit(selectedSub)} className="btn btn-primary">
                            다시 시도
                        </button>
                    </div>
                ) : subreddit ? (
                    <>
                        <div className="subreddit-header">
                            <div className="subreddit-info">
                                <img
                                    src={subreddit.iconUrl || 'https://www.redditstatic.com/desktop2x/img/favicon/android-icon-192x192.png'}
                                    alt=""
                                    className="subreddit-icon"
                                    onError={(e) => {
                                        (e.target as HTMLImageElement).src = 'https://www.redditstatic.com/desktop2x/img/favicon/android-icon-192x192.png';
                                    }}
                                />
                                <div>
                                    <h2 className="subreddit-title">{subreddit.title || `r/${subreddit.subreddit}`}</h2>
                                    {subreddit.description && (
                                        <p className="subreddit-description">{subreddit.description}</p>
                                    )}
                                </div>
                            </div>
                            <button className={`follow-btn ${subreddit.isFollowed ? 'following' : ''}`}>
                                {subreddit.isFollowed ? 'Following' : 'Follow'}
                            </button>
                        </div>

                        {subreddit.posts && subreddit.posts.length > 0 ? (
                            <div className="headlines-list">
                                {subreddit.posts.map((post, index) => (
                                    <article key={`${post.link}-${index}`} className="headline-item reddit-post">
                                        {/* 썸네일 이미지 */}
                                        {isValidThumbnail(post.thumbnailUrl) && !imageErrors.has(post.link) && (
                                            <div className="headline-thumbnail">
                                                <img
                                                    src={post.thumbnailUrl}
                                                    alt=""
                                                    loading="lazy"
                                                    onError={() => handleImageError(post.link)}
                                                />
                                            </div>
                                        )}
                                        <div className="headline-content">
                                            <a
                                                href={post.link}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="headline-title"
                                            >
                                                {post.title}
                                            </a>
                                            {post.selfText && (
                                                <p className="headline-description">{post.selfText}</p>
                                            )}
                                            <div className="headline-meta">
                                                <span className="author">u/{post.author}</span>
                                                {post.publishedDate && (
                                                    <span className="date">{formatDate(post.publishedDate)}</span>
                                                )}
                                            </div>
                                        </div>
                                    </article>
                                ))}
                            </div>
                        ) : (
                            <div className="empty-state">
                                <i className="bi bi-inbox"></i>
                                <p>이 서브레딧에 게시물이 없습니다</p>
                            </div>
                        )}
                    </>
                ) : (
                    <div className="empty-state">
                        <i className="bi bi-reddit" style={{ color: '#FF4500' }}></i>
                        <p>서브레딧을 선택하세요</p>
                    </div>
                )}
            </main>
        </>
    );
}

