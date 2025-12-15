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
}

interface SubredditFeed {
    subreddit: string;
    title: string;
    description?: string;
    posts: RedditPost[];
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

    // Feeds
    const [feeds, setFeeds] = useState<DiscoveredFeed[]>([]);
    const [feedsLoading, setFeedsLoading] = useState(true);

    // Keyword Search
    const [keyword, setKeyword] = useState('');
    const [searchResults, setSearchResults] = useState<RecommendedSite[]>([]);
    const [searchLoading, setSearchLoading] = useState(false);
    const [followedUrls, setFollowedUrls] = useState<Set<string>>(new Set());

    // Reddit
    const [redditFeed, setRedditFeed] = useState<SubredditFeed | null>(null);
    const [selectedSubreddit, setSelectedSubreddit] = useState('programming');
    const [redditLoading, setRedditLoading] = useState(false);

    useEffect(() => {
        if (activeTab === 'feeds') fetchFeeds();
        else fetchReddit(selectedSubreddit);
    }, [activeTab, selectedSubreddit]);

    const handleTabChange = (tab: 'feeds' | 'reddit') => {
        setActiveTab(tab);
        setSearchParams({ tab });
    };

    const fetchFeeds = async () => {
        setFeedsLoading(true);
        try {
            const res = await fetch('http://localhost:8080/api/discover?view=feeds', { credentials: 'include' });
            if (res.ok) setFeeds((await res.json()).feeds || []);
        } catch (e) { console.error(e); }
        setFeedsLoading(false);
    };

    const fetchReddit = async (sub: string) => {
        setRedditLoading(true);
        try {
            const res = await fetch(`http://localhost:8080/api/reddit?subreddit=${sub}`, { credentials: 'include' });
            if (res.ok) setRedditFeed(await res.json());
        } catch (e) { console.error(e); }
        setRedditLoading(false);
    };

    // 키워드 검색
    const handleSearch = async (e: React.FormEvent) => {
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
                body: JSON.stringify({ websiteUrl: site.url, feedUrl: site.feedUrl, title: site.title, description: site.description, faviconUrl: site.faviconUrl }),
                credentials: 'include'
            });
            const data = await res.json();
            if (data.success) {
                setFollowedUrls(new Set([...followedUrls, site.feedUrl!]));
                alert(data.message);
            } else alert(data.message);
        } catch (e) { alert('팔로우 실패'); }
    };

    return (
        <>
            <header className="discover-header">
                <div className="header-top">
                    <h1><i className="bi bi-compass me-2"></i>Follow Sources</h1>
                </div>
                <div className="view-toggle">
                    <button className={`toggle-btn ${activeTab === 'feeds' ? 'active' : ''}`} onClick={() => handleTabChange('feeds')}>
                        <i className="bi bi-rss me-1"></i>Feeds
                    </button>
                    <button className={`toggle-btn ${activeTab === 'reddit' ? 'active' : ''}`} onClick={() => handleTabChange('reddit')}>
                        <i className="bi bi-reddit me-1"></i>Reddit
                    </button>
                </div>
            </header>

            <main className="discover-content">
                {/* FEEDS 탭 */}
                {activeTab === 'feeds' && (
                    <>
                        {/* 키워드 검색 */}
                        <div className="keyword-search-box">
                            <form onSubmit={handleSearch} className="keyword-form">
                                <input
                                    type="text"
                                    value={keyword}
                                    onChange={(e) => setKeyword(e.target.value)}
                                    placeholder="키워드로 새 소스 찾기 (예: tech news, AI, startup...)"
                                    className="keyword-input"
                                />
                                <button type="submit" disabled={searchLoading} className="keyword-btn">
                                    {searchLoading ? <span className="spinner-border spinner-border-sm"></span> : <i className="bi bi-search"></i>}
                                </button>
                            </form>
                        </div>

                        {/* 검색 결과 */}
                        {searchResults.length > 0 && (
                            <div className="search-results">
                                <h5>"{keyword}" 검색 결과</h5>
                                {searchResults.map((site, i) => (
                                    <div key={i} className={`search-result-item ${site.hasFeed ? 'has-feed' : ''}`}>
                                        <img src={site.faviconUrl || ''} alt="" className="result-favicon" onError={(e) => e.currentTarget.style.display = 'none'} />
                                        <div className="result-info">
                                            <a href={site.url} target="_blank" rel="noopener noreferrer" className="result-title">{site.title}</a>
                                            <span className="result-desc">{site.description}</span>
                                            {site.previewArticles.length > 0 && (
                                                <ul className="result-articles">
                                                    {site.previewArticles.map((a, j) => <li key={j}><a href={a.link} target="_blank" rel="noopener noreferrer">{a.title}</a></li>)}
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
                                            <img src={feed.faviconUrl || ''} alt="" className="feed-icon" onError={(e) => e.currentTarget.style.display = 'none'} />
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

                {/* REDDIT 탭 */}
                {activeTab === 'reddit' && (
                    <>
                        <div className="subreddit-selector">
                            {POPULAR_SUBREDDITS.map((sub) => (
                                <button key={sub} className={`subreddit-btn ${selectedSubreddit === sub ? 'active' : ''}`} onClick={() => setSelectedSubreddit(sub)}>
                                    r/{sub}
                                </button>
                            ))}
                        </div>
                        {redditLoading ? (
                            <div className="loading-container"><div className="spinner"></div></div>
                        ) : redditFeed && (
                            <div className="reddit-posts">
                                {redditFeed.posts.map((post, i) => (
                                    <div key={i} className="reddit-post">
                                        <a href={post.link} target="_blank" rel="noopener noreferrer">{post.title}</a>
                                        <span className="post-author">u/{post.author}</span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </>
                )}
            </main>
        </>
    );
}