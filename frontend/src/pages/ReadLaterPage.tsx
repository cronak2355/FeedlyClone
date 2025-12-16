import { useState, useEffect } from 'react';
import '/src/styles/discover.css';

interface SavedArticle {
    url: string;
    title?: string;
    description?: string;
    thumbnailUrl?: string;
    siteName?: string;
    savedAt?: string;
    memo?: string;
}

export default function ReadLaterPage() {
    const [articles, setArticles] = useState<SavedArticle[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchSavedArticles();
    }, []);

    const fetchSavedArticles = async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch('http://localhost:8080/api/saved', {
                credentials: 'include',
            });

            if (!response.ok) throw new Error('Failed to fetch');

            const data = await response.json();
            setArticles(data.articles || []);
        } catch (err) {
            console.error('Fetch error:', err);
            setError('저장된 기사를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleRemove = async (url: string) => {
        try {
            await fetch('http://localhost:8080/api/articles/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ url }),
                credentials: 'include',
            });

            // Remove from list
            setArticles(articles.filter(a => a.url !== url));
        } catch (err) {
            console.error('Remove error:', err);
        }
    };

    const formatDate = (dateString?: string) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
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

    return (
        <>
            {/* Header */}
            <header className="discover-header">
                <div className="header-top">
                    <h1>
                        <i className="bi bi-bookmark me-2"></i>
                        Read Later
                    </h1>
                    <span className="article-count">{articles.length}개의 기사</span>
                </div>
            </header>

            {/* Main Content */}
            <main className="discover-content">
                {error ? (
                    <div className="error-container">
                        <p>{error}</p>
                        <button onClick={fetchSavedArticles} className="btn btn-primary">다시 시도</button>
                    </div>
                ) : articles.length > 0 ? (
                    <div className="headlines-list">
                        {articles.map((article, index) => (
                            <article key={index} className="headline-item saved-article">
                                {article.thumbnailUrl && (
                                    <img
                                        src={article.thumbnailUrl}
                                        alt=""
                                        className="headline-thumbnail"
                                        onError={(e) => (e.currentTarget.style.display = 'none')}
                                    />
                                )}
                                <div className="headline-content">
                                    <a href={article.url} target="_blank" rel="noopener noreferrer" className="headline-title">
                                        {article.title || article.url}
                                    </a>
                                    {article.description && (
                                        <p className="headline-description">{article.description}</p>
                                    )}
                                    {article.memo && (
                                        <p className="article-memo">
                                            <i className="bi bi-chat-left-text me-1"></i>
                                            {article.memo}
                                        </p>
                                    )}
                                    <div className="headline-meta">
                                        {article.siteName && <span className="source">{article.siteName}</span>}
                                        {article.savedAt && <span className="date">저장: {formatDate(article.savedAt)}</span>}
                                        <button
                                            className="remove-btn"
                                            onClick={() => handleRemove(article.url)}
                                            title="북마크 해제"
                                        >
                                            <i className="bi bi-bookmark-x"></i>
                                        </button>
                                    </div>
                                </div>
                            </article>
                        ))}
                    </div>
                ) : (
                    <div className="empty-state">
                        <i className="bi bi-bookmark"></i>
                        <h3>저장된 기사가 없습니다</h3>
                        <p>나중에 읽고 싶은 기사를 북마크하세요!</p>
                    </div>
                )}
            </main>
        </>
    );
}
