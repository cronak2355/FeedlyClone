import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import '../styles/discover.css';

interface DiscoveredFeed {
    feedUrl: string;
    siteUrl?: string;
    title: string;
    description?: string;
    faviconUrl?: string;
    category?: string;
    subscriberCount?: number;
    isFollowed?: boolean;
}

interface RedditPost {
    title: string;
    link: string;
    author?: string;
    publishedDate?: string;
    selfText?: string;
}

interface SubredditFeed {
    subreddit: string;
    feedUrl?: string;
    title: string;
    description?: string;
    posts: RedditPost[];
    isFollowed?: boolean;
}

interface RecommendedSite {
    title: string;
    url: string;
    feedUrl: string | null;
    description: string | null;
    faviconUrl: string | null;
    previewArticles: { title: string; link: string }[];
    hasFeed: boolean;
}

const POPULAR_SUBREDDITS = ['programming', 'kotlin', 'java', 'javascript', 'python', 'webdev', 'android', 'technology'];

export default function FollowSourcesPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [activeTab, setActiveTab] = useState<'feeds' | 'reddit'>(
        (searchParams.get('tab') as 'feeds' | 'reddit') || 'feeds'
    );

    // ===== Feeds 탭 상태 =====
    const [feeds, setFeeds] = useState<DiscoveredFeed[]>([]);
    const [feedsLoading, setFeedsLoading] = useState(true);
    const [keyword, setKeyword] = useState('');
    const [searchResults, setSearchResults] = useState<RecommendedSite[]>([]);
    const [searchLoading, setSearchLoading] = useState(false);
    const [followedUrls, setFollowedUrls] = useState<Set<string>>(new Set());

    // ===== Reddit 탭 상태 =====
    const [redditFeed, setRedditFeed] = useState<SubredditFeed | null>(null);
    const [selectedSubreddit, setSelectedSubreddit] = useState('programming');
    const [redditLoading, setRedditLoading] = useState(false);
    const [redditSearchQuery, setRedditSearchQuery] = useState('');  // Reddit 검색어

    // 탭 변경 시 데이터 로드
    useEffect(() => {
        if (activeTab === 'feeds') {
            fetchFeeds();
        } else {
            fetchReddit(selectedSubreddit);
        }
    }, [activeTab, selectedSubreddit]);

    const handleTabChange = (tab: 'feeds' | 'reddit') => {
        setActiveTab(tab);
        setSearchParams({ tab });
    };

    // ===== Feeds 관련 함수 =====
    const fetchFeeds = async () => {
        setFeedsLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/discover?view=feeds', { credentials: 'include' });
            if (res.ok) setFeeds((await res.json()).feeds || []);
        } catch (e) { console.error(e); }
        setFeedsLoading(false);
    };

    const handleFeedsSearch = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!keyword.trim()) return;

        setSearchLoading(true);
        setSearchResults([]);
        try {
            const res = await fetch(`http://localhost:8080/api/search/keyword?keyword=${encodeURIComponent(keyword)}`, { credentials: 'include' });
            if (res.ok) setSearchResults((await res.json()).sites || []);
        } catch (e) { console.error(e); }
        setSearchLoading(false);
    };

    const handleFollow = async (site: RecommendedSite) => {
        if (!site.feedUrl) return alert('RSS 피드가 없습니다.');
        try {
            const res = await fetch('http://localhost:8080/api/search/follow', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    websiteUrl: site.url, 
                    feedUrl: site.feedUrl, 
                    title: site.title, 
                    description: site.description, 
                    faviconUrl: site.faviconUrl 
                }),
                credentials: 'include'
            });
            const data = await res.json();
            if (data.success) {
                setFollowedUrls(new Set([...followedUrls, site.feedUrl!]));
                alert(data.message);
            } else {
                alert(data.message);
            }
        } catch (e) { 
            alert('팔로우 실패'); 
        }
    };

    // ===== Reddit 관련 함수 =====
    const fetchReddit = async (sub: string) => {
        if (!sub.trim()) return;
        
        setRedditLoading(true);
        setRedditFeed(null);
        try {
            const res = await fetch(`http://localhost:8080/api/reddit?subreddit=${encodeURIComponent(sub)}`, { 
                credentials: 'include' 
            });
            if (res.ok) {
                const data = await res.json();
                if (data.posts) {
                    setRedditFeed(data);
                }
            }
        } catch (e) { 
            console.error(e); 
        }
        setRedditLoading(false);
    };

    // Reddit 검색 핸들러
    const handleRedditSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (!redditSearchQuery.trim()) return;
        
        // r/ 접두사 제거
        const cleanQuery = redditSearchQuery.replace(/^r\//, '').trim();
        setSelectedSubreddit(cleanQuery);
        setRedditSearchQuery('');
    };

    // Reddit 팔로우
    const handleFollowSubreddit = async () => {
        if (!redditFeed?.feedUrl) return;
        
        try {
            const res = await fetch('http://localhost:8080/api/search/follow', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    websiteUrl: `https://reddit.com/r/${redditFeed.subreddit}`,
                    feedUrl: redditFeed.feedUrl,
                    title: `r/${redditFeed.subreddit}`,
                    description: redditFeed.description,
                    faviconUrl: 'https://www.redditstatic.com/desktop2x/img/favicon/android-icon-192x192.png'
                }),
                credentials: 'include'
            });
            const data = await res.json();
            alert(data.message);
        } catch (e) {
            alert('팔로우 실패');
        }
    };

    const formatDate = (dateString?: string) => {
        if (!dateString) return '';
        try {
            return new Date(dateString).toLocaleDateString('ko-KR', {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return '';
        }
    };

    return (
        <>
            <header className="discover-header">
                <div className="header-top">
                    <h1><i className="bi bi-compass me-2"></i>Follow Sources</h1>
                </div>
                <div className="view-toggle">
                    <button 
                        className={`toggle-btn ${activeTab === 'feeds' ? 'active' : ''}`} 
                        onClick={() => handleTabChange('feeds')}
                    >
                        <i className="bi bi-rss me-1"></i>Feeds
                    </button>
                    <button 
                        className={`toggle-btn ${activeTab === 'reddit' ? 'active' : ''}`} 
                        onClick={() => handleTabChange('reddit')}
                    >
                        <i className="bi bi-reddit me-1"></i>Reddit
                    </button>
                </div>
            </header>

            <main className="discover-content">
                {/* ==================== FEEDS 탭 ==================== */}
                {activeTab === 'feeds' && (
                    <>
                        {/* 키워드 검색 */}
                        <div className="keyword-search-box">
                            <form onSubmit={handleFeedsSearch} className="keyword-form">
                                <input
                                    type="text"
                                    value={keyword}
                                    onChange={(e) => setKeyword(e.target.value)}
                                    placeholder="키워드로 새 소스 찾기 (예: tech news, AI, startup...)"
                                    className="keyword-input"
                                />
                                <button type="submit" disabled={searchLoading} className="keyword-btn">
                                    {searchLoading ? (
                                        <span className="spinner-border spinner-border-sm"></span>
                                    ) : (
                                        <i className="bi bi-search"></i>
                                    )}
                                </button>
                            </form>
                        </div>

                        {/* 검색 결과 */}
                        {searchResults.length > 0 && (
                            <div className="search-results">
                                <h5>"{keyword}" 검색 결과</h5>
                                {searchResults.map((site, i) => (
                                    <div key={i} className={`search-result-item ${site.hasFeed ? 'has-feed' : ''}`}>
                                        <img 
                                            src={site.faviconUrl || ''} 
                                            alt="" 
                                            className="result-favicon" 
                                            onError={(e) => e.currentTarget.style.display = 'none'} 
                                        />
                                        <div className="result-info">
                                            <a href={site.url} target="_blank" rel="noopener noreferrer" className="result-title">
                                                {site.title}
                                            </a>
                                            <span className="result-desc">{site.description}</span>
                                            {site.previewArticles.length > 0 && (
                                                <ul className="result-articles">
                                                    {site.previewArticles.map((a, j) => (
                                                        <li key={j}>
                                                            <a href={a.link} target="_blank" rel="noopener noreferrer">
                                                                {a.title}
                                                            </a>
                                                        </li>
                                                    ))}
                                                </ul>
                                            )}
                                        </div>
                                        {site.hasFeed && site.feedUrl && (
                                            followedUrls.has(site.feedUrl) 
                                                ? <span className="followed-badge"><i className="bi bi-check"></i></span>
                                                : <button className="follow-btn" onClick={() => handleFollow(site)}>Follow</button>
                                        )}
                                        {!site.hasFeed && <span className="no-rss">No RSS</span>}
                                    </div>
                                ))}
                            </div>
                        )}

                        {/* 기존 피드 목록 */}
                        {feedsLoading ? (
                            <div className="loading-container"><div className="spinner"></div></div>
                        ) : (
                            <div className="feeds-grid">
                                {feeds.map((feed, i) => (
                                    <div key={i} className="feed-card">
                                        <div className="feed-header">
                                            <img 
                                                src={feed.faviconUrl || ''} 
                                                alt="" 
                                                className="feed-icon" 
                                                onError={(e) => e.currentTarget.style.display = 'none'} 
                                            />
                                            <div className="feed-info">
                                                <h5 className="feed-title">{feed.title}</h5>
                                                <span className="feed-url">{feed.siteUrl}</span>
                                            </div>
                                        </div>
                                        {feed.description && <p className="feed-description">{feed.description}</p>}
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}

                {/* ==================== REDDIT 탭 ==================== */}
                {activeTab === 'reddit' && (
                    <>
                        {/* Reddit 검색창 */}
                        <div className="keyword-search-box">
                            <form onSubmit={handleRedditSearch} className="keyword-form">
                                <span className="reddit-prefix">r/</span>
                                <input
                                    type="text"
                                    value={redditSearchQuery}
                                    onChange={(e) => setRedditSearchQuery(e.target.value)}
                                    placeholder="서브레딧 이름 입력 (예: programming, kotlin, webdev...)"
                                    className="keyword-input reddit-input"
                                />
                                <button type="submit" disabled={redditLoading} className="keyword-btn reddit-btn">
                                    {redditLoading ? (
                                        <span className="spinner-border spinner-border-sm"></span>
                                    ) : (
                                        <i className="bi bi-search"></i>
                                    )}
                                </button>
                            </form>
                        </div>

                        {/* 인기 서브레딧 버튼 */}
                        <div className="subreddit-selector">
                            {POPULAR_SUBREDDITS.map((sub) => (
                                <button 
                                    key={sub} 
                                    className={`subreddit-btn ${selectedSubreddit === sub ? 'active' : ''}`} 
                                    onClick={() => setSelectedSubreddit(sub)}
                                >
                                    r/{sub}
                                </button>
                            ))}
                        </div>

                        {/* 로딩 */}
                        {redditLoading && (
                            <div className="loading-container">
                                <div className="spinner"></div>
                                <p>Loading r/{selectedSubreddit}...</p>
                            </div>
                        )}

                        {/* 서브레딧 컨텐츠 */}
                        {!redditLoading && redditFeed && redditFeed.posts && redditFeed.posts.length > 0 && (
                            <div className="reddit-content">
                                {/* 서브레딧 헤더 */}
                                <div className="subreddit-header-card">
                                    <div className="subreddit-info">
                                        <img 
                                            src="https://www.redditstatic.com/desktop2x/img/favicon/android-icon-192x192.png" 
                                            alt="Reddit" 
                                            className="subreddit-icon"
                                        />
                                        <div>
                                            <h3>r/{redditFeed.subreddit}</h3>
                                            {redditFeed.description && <p>{redditFeed.description}</p>}
                                        </div>
                                    </div>
                                    <button className="follow-btn" onClick={handleFollowSubreddit}>
                                        <i className="bi bi-plus-circle me-1"></i>Follow
                                    </button>
                                </div>

                                {/* 포스트 목록 */}
                                <div className="headlines-list">
                                    {redditFeed.posts.map((post, index) => (
                                        <article key={index} className="headline-item reddit-post">
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
                                                    <span className="reddit-author">u/{post.author}</span>
                                                    {post.publishedDate && (
                                                        <span className="date">{formatDate(post.publishedDate)}</span>
                                                    )}
                                                </div>
                                            </div>
                                        </article>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* 결과 없음 */}
                        {!redditLoading && (!redditFeed || !redditFeed.posts || redditFeed.posts.length === 0) && (
                            <div className="empty-state">
                                <i className="bi bi-reddit" style={{ color: '#FF4500', fontSize: '3rem' }}></i>
                                <h4>r/{selectedSubreddit}</h4>
                                <p>게시물을 불러올 수 없습니다</p>
                            </div>
                        )}
                    </>
                )}
            </main>
        </>
    );
}