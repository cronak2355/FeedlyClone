import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import '../styles/discover.css';
import { findAllInRenderedTree } from 'react-dom/test-utils';

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

export default function DiscoverPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [headlines, setHeadlines] = useState<FeedItem[]>([]);
    const [feeds, setFeeds] = useState<DiscoveredFeed[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [savedUrls, setSavedUrls] = useState<Set<string>>(new Set());

    const view = searchParams.get('view') || 'headlines';

    async function followFeed(feed: DiscoveredFeed) {
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
            faviconUrl: feed.faviconUrl,
            category: feed.category,
            feedType: "RSS",
            }),
        });

        if (!response.ok) {
            alert("follow/unfollow에 실패했습니다.");
            return;
        }

        // UI 즉시 반영 (낙관적 업데이트)
        setFeeds(prev =>
            prev.map(f =>
            f.feedUrl === feed.feedUrl ? { ...f, isFollowed: !feed.isFollowed } : f
            )
        );
    }

    useEffect(() => {
        fetchData();
        fetchSavedUrls();
    }, [view]);
    //뉴스에 저장된 내용들을 비동기로 받아옴.
    const fetchSavedUrls = async () => {
        try {
            //해당 url로 GET 요청을 보냄
            //credentials = include인 이유는 로그인 정보를 쿠키에서 저장하기 때문에,
            //쿠키를 포함해서 보내줌
            const response = await fetch('http://localhost:8080/api/saved', {
                credentials: 'include',
            });
            if (response.ok) {
                const data = await response.json();
                const urls = new Set<string>(data.articles?.map((a: { url: string }) => a.url) || []);
                setSavedUrls(urls);
            }
        } catch (err) {
            console.error('Fetch saved error:', err);
        }
    };

    const fetchData = async () => {
        setLoading(true);
        setError(null);

        try {
            // Top Headlines (인기 뉴스)
            const headlinesResponse = await fetch('http://localhost:8080/api/discover?view=headlines', {
                credentials: 'include',
            });

            if (headlinesResponse.ok) {
                const headlinesData = await headlinesResponse.json();
                setHeadlines(headlinesData.headlines || []);
            }

            // Feeds
            const feedsResponse = await fetch('http://localhost:8080/api/discover?view=feeds', {
                credentials: 'include',
            });

            if (feedsResponse.ok) {
                const feedsData = await feedsResponse.json();
                setFeeds(feedsData.feeds || []);
            }
        } catch (err) {
            console.error('Fetch error:', err);
            setError('데이터를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleViewChange = (newView: string) => {
        setSearchParams({ view: newView });
    };

    const handleSaveArticle = async (item: FeedItem) => {
        try {
            const response = await fetch('http://localhost:8080/api/articles/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    url: item.link,
                    title: item.title,
                    description: item.description,
                    thumbnailUrl: item.thumbnailUrl,
                    siteName: item.sourceName
                }),
                credentials: 'include',
            });

            if (response.ok) {
                const data = await response.json();
                if (data.isSaved) {
                    setSavedUrls(new Set([...savedUrls, item.link]));
                } else {
                    const newSaved = new Set(savedUrls);
                    newSaved.delete(item.link);
                    setSavedUrls(newSaved);
                }
            }
        } catch (err) {
            console.error('Save error:', err);
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

    if (loading) {
        return (
            <div className="discover-page">
                <div className="loading-container">
                    <div className="spinner"></div>
                    <p>Loading...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="discover-page">
                <div className="error-container">
                    <p>{error}</p>
                    <button onClick={fetchData} className="btn btn-primary">다시 시도</button>
                </div>
            </div>
        );
    }

    return (
        <>
            {/* Header */}
            <header className="discover-header">
                <div className="header-top">
                    <h1>
                        <i className="bi bi-compass me-2"></i>
                        Explore
                    </h1>
                </div>

                {/* View Toggle */}
                <div className="view-toggle">
                    <button
                        className={`toggle-btn ${view === 'headlines' ? 'active' : ''}`}
                        onClick={() => handleViewChange('headlines')}
                    >
                        <i className="bi bi-fire me-1"></i>
                        Top Headlines
                    </button>
                    <button
                        className={`toggle-btn ${view === 'feeds' ? 'active' : ''}`}
                        onClick={() => handleViewChange('feeds')}
                    >
                        <i className="bi bi-rss me-1"></i>
                        Feeds
                    </button>
                </div>
            </header>

            {/* Main Content */}
            <main className="discover-content">
                {/* Headlines View - Top N 인기 뉴스 */}
                {view === 'headlines' && (
                    <div className="headlines-list">
                        <p className="section-subtitle">
                            <i className="bi bi-lightning-fill text-warning me-1"></i>
                            Popularest News
                        </p>
                        {headlines.length ? (
                            headlines.slice(0, 20).map((item, index) => (
                                <article key={index} className="headline-item">
                                    <span className="headline-rank">{index + 1}</span>
                                    {item.thumbnailUrl && (
                                        <img
                                            src={item.thumbnailUrl}
                                            alt=""
                                            className="headline-thumbnail"
                                            onError={(e) => (e.currentTarget.style.display = 'none')}
                                        />
                                    )}
                                    <div className="headline-content">
                                        <a href={item.link} target="_blank" rel="noopener noreferrer" className="headline-title">
                                            {item.title}
                                        </a>
                                        {item.description && (
                                            <p className="headline-description">{item.description}</p>
                                        )}
                                        <div className="headline-meta">
                                            {item.sourceName && <span className="source">{item.sourceName}</span>}
                                            {item.publishedDate && <span className="date">{formatDate(item.publishedDate)}</span>}
                                            <button
                                                className={`bookmark-btn ${savedUrls.has(item.link) ? 'saved' : ''}`}
                                                onClick={() => handleSaveArticle(item)}
                                                title="나중에 보기"
                                            >
                                                <i className={`bi ${savedUrls.has(item.link) ? 'bi-bookmark-fill' : 'bi-bookmark'}`}></i>
                                            </button>
                                        </div>
                                    </div>
                                </article>
                            ))
                        ) : (
                            <div className="empty-state">
                                <i className="bi bi-newspaper"></i>
                                <p>No headlines available</p>
                            </div>
                        )}
                    </div>
                )}

                {/* Feeds View */}
                {view === 'feeds' && (
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
                                            <button  onClick={() => followFeed(feed)}
                                                     className={`follow-btn ${feed.isFollowed ? 'following' : ''}`}>
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
                )}
            </main>
        </>
    );
}
