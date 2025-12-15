import { useState, useEffect } from 'react';
import '../styles/discover.css';

interface RedditPost {
    title: string;
    link: string;
    author: string;
    subreddit: string;
    publishedDate?: string;
    selfText?: string;
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

    useEffect(() => {
        if (selectedSub) {
            fetchSubreddit(selectedSub);
        }
    }, [selectedSub]);
    async function followFeed(feed: SubredditFeed) {
        const url = feed.isFollowed
            ? "http://localhost:8080/discover/unfollow"
            : "http://localhost:8080/discover/follow";

        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({
            feedUrl: feed.feedUrl,      // 여기도 siteUrl 말고 feedUrl이 맞을 확률 높음
            title: feed.title,
            description: feed.description,
            faviconUrl: feed.iconUrl,
            category: feed.subreddit,
            feedType: "RSS",
            }),
        });

        if (!response.ok) {
            alert("follow/unfollow에 실패했습니다.");
            return;
        }

        // UI 즉시 반영 (낙관적 업데이트)
        setSubreddit(prev =>
        prev
            ? { ...prev, isFollowed: !prev.isFollowed }
            : prev
        );
    }
    const fetchSubreddit = async (sub: string) => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`http://localhost:8080/api/reddit?subreddit=${sub}`, {
                credentials: 'include',
            });

            if (!response.ok) throw new Error('Failed to fetch');

            const data = await response.json();
            setSubreddit(data.subreddit);
        } catch (err) {
            console.error('Fetch error:', err);
            setError('서브레딧을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            const cleanQuery = searchQuery.replace(/^r\//, '').trim();
            setSelectedSub(cleanQuery);
            setSearchQuery('');
        }
    };

    const formatDate = (dateString?: string) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
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
                        <p>{error}</p>
                        <button onClick={() => fetchSubreddit(selectedSub)} className="btn btn-primary">다시 시도</button>
                    </div>
                ) : subreddit ? (
                    <>
                        <div className="subreddit-header">
                            <div className="subreddit-info">
                                <img
                                    src={subreddit.iconUrl || 'https://www.redditstatic.com/desktop2x/img/favicon/android-icon-192x192.png'}
                                    alt=""
                                    className="subreddit-icon"
                                />
                                <div>
                                    <h2 className="subreddit-title">{subreddit.title}</h2>
                                    {subreddit.description && (
                                        <p className="subreddit-description">{subreddit.description}</p>
                                    )}
                                </div>
                            </div>
                            <button  onClick={() => followFeed(subreddit)}
                                className={`follow-btn ${subreddit.isFollowed ? 'following' : ''}`}>
                                {subreddit.isFollowed ? 'Following' : 'Follow'}
                            </button>
                        </div>

                        <div className="headlines-list">
                            {subreddit.posts?.map((post, index) => (
                                <article key={index} className="headline-item reddit-post">
                                    <div className="headline-content">
                                        <a href={post.link} target="_blank" rel="noopener noreferrer" className="headline-title">
                                            {post.title}
                                        </a>
                                        {post.selfText && (
                                            <p className="headline-description">{post.selfText}</p>
                                        )}
                                        <div className="headline-meta">
                                            <span className="author">u/{post.author}</span>
                                            {post.publishedDate && <span className="date">{formatDate(post.publishedDate)}</span>}
                                        </div>
                                    </div>
                                </article>
                            ))}
                        </div>
                    </>
                ) : (
                    <div className="empty-state">
                        <i className="bi bi-reddit"></i>
                        <p>서브레딧을 선택하세요</p>
                    </div>
                )}
            </main>
        </>
    );
}
