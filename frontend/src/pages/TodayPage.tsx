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
    const [headlines, setHeadlines] = useState<FeedItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [savedUrls, setSavedUrls] = useState<Set<string>>(new Set());

    useEffect(() => {
        if (activeTab === 'explore') {
            fetchHeadlines();
        }
        fetchSavedUrls();
    }, [activeTab]);

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
                const urls = new Set<string>(data.articles?.map((a: { url: string }) => a.url) || []);
                setSavedUrls(urls);
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

    return (
        <>
            <header className="discover-header">
                <div className="header-top">
                    <h1>Today</h1>
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
                {activeTab === 'me' && (
                    <div className="today-empty-state">
                        <div className="today-illustration">
                            <div className="today-card">
                                <span className="today-card-label">Today</span>
                                <div className="today-card-content">
                                    <div className="today-card-line"></div>
                                    <div className="today-card-line short"></div>
                                    <div className="today-card-line"></div>
                                </div>
                            </div>
                            <div className="today-avatar">
                                <i className="bi bi-person-circle"></i>
                            </div>
                        </div>

                        <h2>Personalize your Feedly</h2>
                        <p className="text-secondary">
                            The most interesting articles published by the<br />
                            feeds you personally follow will be here.
                        </p>
                        <a href="/discover" className="btn btn-primary">Add articles</a>
                    </div>
                )}

                {activeTab === 'explore' && (
                    <div className="headlines-list">
                        <p className="section-subtitle">
                            <i className="bi bi-lightning-fill text-warning me-1"></i>
                            Top Headlines
                        </p>
                        {loading ? (
                            <div className="loading-container">
                                <div className="spinner"></div>
                                <p>Loading...</p>
                            </div>
                        ) : headlines.length ? (
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
            </main>
        </>
    );
}
