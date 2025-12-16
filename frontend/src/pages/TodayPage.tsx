import { useState, useEffect } from 'react';
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

export default function TodayPage() {
    const [activeTab, setActiveTab] = useState<'me' | 'explore'>('me');
    const [myItems, setMyItems] = useState<FeedItem[]>([]);
    const [headlines, setHeadlines] = useState<FeedItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [savedUrls, setSavedUrls] = useState<Set<string>>(new Set());

    useEffect(() => {
        if (activeTab === 'me') {
            fetchMyFeedItems();
        } else {
            fetchHeadlines();
        }
        fetchSavedUrls();
    }, [activeTab]);

    // 팔로우한 피드의 최신 글
    const fetchMyFeedItems = async () => {
        setLoading(true);
        try {
            const response = await fetch('http://localhost:8080/api/discover?view=myfeed', {
                credentials: 'include',
            });
            if (response.ok) {
                const data = await response.json();
                setMyItems(data.items || []);
            }
        } catch (err) {
            console.error('Fetch error:', err);
        } finally {
            setLoading(false);
        }
    };

    // Explore 탭
    const fetchHeadlines = async () => {
        setLoading(true);
        try {
            const response = await fetch('http://localhost:8080/api/discover?view=headlines', {
                credentials: 'include',
            });
            if (response.ok) {
                const data = await response.json();
                setHeadlines(data.headlines || []);
            }
        } catch (err) {
            console.error('Fetch error:', err);
        } finally {
            setLoading(false);
        }
    };

    const fetchSavedUrls = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/saved', { credentials: 'include' });
            if (response.ok) {
                const data = await response.json();
                setSavedUrls(new Set<string>(data.articles?.map((a: { url: string }) => a.url) || []));
            }
        } catch (err) {
            console.error('Fetch saved error:', err);
        }
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

    const currentItems = activeTab === 'me' ? myItems : headlines;

    return (
        <div className="today-page">
            <header className="discover-header">
                <div className="header-top">
                    <h1><i className="bi bi-sun me-2"></i>Today</h1>
                    <span className="header-subtitle">The insights you need to keep ahead</span>
                </div>

                <div className="view-toggle">
                    <button
                        className={`toggle-btn ${activeTab === 'me' ? 'active' : ''}`}
                        onClick={() => setActiveTab('me')}
                    >
                        Me
                    </button>
                    <button
                        className={`toggle-btn ${activeTab === 'explore' ? 'active' : ''}`}
                        onClick={() => setActiveTab('explore')}
                    >
                        Explore
                    </button>
                </div>
            </header>

            <main className="discover-content">
                {loading ? (
                    <div className="loading-container">
                        <div className="spinner"></div>
                        <p>Loading...</p>
                    </div>
                ) : currentItems.length === 0 ? (
                    <div className="today-empty-state">
                        {activeTab === 'me' ? (
                            <>
                                <i className="bi bi-rss" style={{ fontSize: '3rem', color: 'var(--primary-color)' }}></i>
                                <h2>Personalize your Feedly</h2>
                                <p className="text-secondary">
                                    팔로우한 피드의 최신 글이 여기에 표시됩니다.
                                </p>
                                <a href="/follow-sources" className="btn btn-primary">피드 추가하기</a>
                            </>
                        ) : (
                            <>
                                <i className="bi bi-newspaper" style={{ fontSize: '3rem' }}></i>
                                <p>뉴스가 없습니다</p>
                            </>
                        )}
                    </div>
                ) : (
                    <div className="headlines-list">
                        <p className="section-subtitle">
                            {activeTab === 'me' ? (
                                <><i className="bi bi-rss text-success me-1"></i>팔로우한 피드 ({myItems.length})</>
                            ) : (
                                <><i className="bi bi-lightning-fill text-warning me-1"></i>Top Headlines</>
                            )}
                        </p>
                        {currentItems.map((item, index) => (
                            <article key={index} className="headline-item">
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
                                        {item.sourceName && (
                                            <span className={`source ${item.sourceName.startsWith('r/') ? 'reddit-source' : ''}`}>
                                                {item.sourceName}
                                            </span>
                                        )}
                                        {item.publishedDate && (
                                            <span className="date">{formatDate(item.publishedDate)}</span>
                                        )}
                                        <button
                                            className={`bookmark-btn ${savedUrls.has(item.link) ? 'saved' : ''}`}
                                            onClick={() => handleSaveArticle(item)}
                                        >
                                            <i className={`bi ${savedUrls.has(item.link) ? 'bi-bookmark-fill' : 'bi-bookmark'}`}></i>
                                        </button>
                                    </div>
                                </div>
                            </article>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
}