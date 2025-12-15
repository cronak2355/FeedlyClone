import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import '../styles/discover.css';

interface FeedItem {
    title: string;
    link: string;
    description?: string;
    author?: string;
    publishedDate?: string;
    thumbnailUrl?: string;
    sourceName?: string;
}

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
    title: string;
    description?: string;
    iconUrl?: string;
    posts: RedditPost[];
}

const POPULAR_SUBREDDITS = ['programming', 'kotlin', 'java', 'javascript', 'python', 'webdev', 'android', 'technology'];

export default function FollowSourcesPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [activeTab, setActiveTab] = useState<'feeds' | 'reddit'>(
        (searchParams.get('tab') as 'feeds' | 'reddit') || 'feeds'
    );
    const [feeds, setFeeds] = useState<DiscoveredFeed[]>([]);
    const [redditFeed, setRedditFeed] = useState<SubredditFeed | null>(null);
    const [selectedSubreddit, setSelectedSubreddit] = useState('programming');
    const [loading, setLoading] = useState(true);

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

    const fetchFeeds = async () => {
        setLoading(true);
        try {
            const response = await fetch('http://localhost:8080/api/discover?view=feeds', {
                credentials: 'include',
            });
            if (response.ok) {
                const data = await response.json();
                setFeeds(data.feeds || []);
            }
        } catch (err) {
            console.error('Fetch feeds error:', err);
        } finally {
            setLoading(false);
        }
    };

    const fetchReddit = async (subreddit: string) => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/reddit?subreddit=${subreddit}`, {
                credentials: 'include',
            });
            if (response.ok) {
                const data = await response.json();
                setRedditFeed(data);
            }
        } catch (err) {
            console.error('Fetch reddit error:', err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <header className="discover-header">
                <div className="header-top">
                    <h1>
                        <i className="bi bi-compass me-2"></i>
                        Follow Sources
                    </h1>
                </div>

                <div className="view-toggle">
                    <button
                        className={`toggle-btn ${activeTab === 'feeds' ? 'active' : ''}`}
                        onClick={() => handleTabChange('feeds')}
                    >
                        <i className="bi bi-rss me-1"></i>
                        Feeds
                    </button>
                    <button
                        className={`toggle-btn ${activeTab === 'reddit' ? 'active' : ''}`}
                        onClick={() => handleTabChange('reddit')}
                    >
                        <i className="bi bi-reddit me-1"></i>
                        Reddit
                    </button>
                </div>
            </header>

            <main className="discover-content">
                {loading ? (
                    <div className="loading-container">
                        <div className="spinner"></div>
                        <p>Loading...</p>
                    </div>
                ) : activeTab === 'feeds' ? (
                    <div className="feeds-section">
                        <p className="section-subtitle">
                            <i className="bi bi-collection me-1"></i>
                            인기 피드 소스
                        </p>
                        <div className="feeds-grid">
                            {feeds.length ? (
                                feeds.map((feed, index) => (
                                    <div key={index} className="feed-card">
                                        <div className="feed-header">
                                            {feed.faviconUrl && (
                                                <img
                                                    src={feed.faviconUrl}
                                                    alt=""
                                                    className="feed-icon"
                                                    onError={(e) => (e.currentTarget.style.display = 'none')}
                                                />
                                            )}
                                            <div className="feed-info">
                                                <h3 className="feed-title">{feed.title}</h3>
                                                {feed.category && <span className="feed-category">{feed.category}</span>}
                                            </div>
                                        </div>
                                        {feed.description && (
                                            <p className="feed-description">{feed.description}</p>
                                        )}
                                        <div className="feed-footer">
                                            {feed.subscriberCount && (
                                                <span className="subscriber-count">
                                                    <i className="bi bi-people"></i> {feed.subscriberCount.toLocaleString()}
                                                </span>
                                            )}
                                            <button className={`follow-btn ${feed.isFollowed ? 'following' : ''}`}>
                                                {feed.isFollowed ? 'Following' : 'Follow'}
                                            </button>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <div className="empty-state">
                                    <i className="bi bi-rss"></i>
                                    <p>No feeds available</p>
                                </div>
                            )}
                        </div>
                    </div>
                ) : (
                    <div className="reddit-section">
                        <div className="subreddit-filter">
                            {POPULAR_SUBREDDITS.map((sub) => (
                                <button
                                    key={sub}
                                    className={`category-btn ${selectedSubreddit === sub ? 'active' : ''}`}
                                    onClick={() => setSelectedSubreddit(sub)}
                                >
                                    r/{sub}
                                </button>
                            ))}
                        </div>

                        {redditFeed && (
                            <>
                                <div className="subreddit-header">
                                    <h2>r/{selectedSubreddit}</h2>
                                    {typeof redditFeed.description === 'string' && redditFeed.description && (
                                        <p className="subreddit-description">{redditFeed.description}</p>
                                    )}
                                </div>

                                <div className="reddit-posts">
                                    {Array.isArray(redditFeed.posts) && redditFeed.posts.map((post, index) => (
                                        <article key={index} className="headline-item">
                                            <div className="headline-content">
                                                <a href={post.link} target="_blank" rel="noopener noreferrer" className="headline-title">
                                                    {post.title}
                                                </a>
                                                <div className="headline-meta">
                                                    <span className="source">u/{post.author}</span>
                                                    {post.publishedDate && <span className="date">{String(post.publishedDate)}</span>}
                                                </div>
                                            </div>
                                        </article>
                                    ))}
                                </div>
                            </>
                        )}
                    </div>
                )}
            </main>
        </>
    );
}
