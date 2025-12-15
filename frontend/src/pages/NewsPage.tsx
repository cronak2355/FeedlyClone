import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import '../styles/discover.css';

interface NewsArticle {
    title: string;
    link: string;
    description?: string;
    author?: string;
    publishedDate?: string;
    thumbnailUrl?: string;
    sourceName?: string;
}

const CATEGORIES = ['Business', 'Entertainment', 'General', 'Health', 'Science', 'Sports', 'Technology'];

export default function NewsPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [articles, setArticles] = useState<NewsArticle[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [savedUrls, setSavedUrls] = useState<Set<string>>(new Set());

    const category = searchParams.get('category') || '';
    const query = searchParams.get('query') || '';

    useEffect(() => {
        fetchNews();
        fetchSavedUrls();
    }, [category, query]);

    const fetchSavedUrls = async () => {
        try {
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

    const fetchNews = async () => {
        setLoading(true);
        setError(null);

        try {
            const params = new URLSearchParams();
            params.set('view', 'headlines');
            if (category) params.set('category', category.toLowerCase());
            if (query) params.set('query', query);

            const response = await fetch(`http://localhost:8080/api/discover?${params}`, {
                credentials: 'include',
            });

            if (!response.ok) {
                throw new Error('Failed to fetch news');
            }

            const data = await response.json();
            setArticles(data.headlines || []);
        } catch (err) {
            console.error('Fetch error:', err);
            setError('뉴스를 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleCategoryChange = (newCategory: string) => {
        const params: Record<string, string> = {};
        if (newCategory) params.category = newCategory.toLowerCase();
        if (query) params.query = query;
        setSearchParams(params);
    };

    const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const params: Record<string, string> = {};
        if (category) params.category = category;
        if (searchQuery) params.query = searchQuery;
        setSearchParams(params);
    };

    const handleSaveArticle = async (article: NewsArticle) => {
        try {
            const response = await fetch('http://localhost:8080/api/articles/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    url: article.link,
                    title: article.title,
                    description: article.description,
                    thumbnailUrl: article.thumbnailUrl,
                    siteName: article.sourceName
                }),
                credentials: 'include',
            });

            if (response.ok) {
                const data = await response.json();
                if (data.isSaved) {
                    setSavedUrls(new Set([...savedUrls, article.link]));
                } else {
                    const newSaved = new Set(savedUrls);
                    newSaved.delete(article.link);
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
                    <p>Loading news...</p>
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
                        <i className="bi bi-newspaper me-2"></i>
                        News
                    </h1>

                    {/* Search */}
                    <form onSubmit={handleSearch} className="search-form">
                        <input
                            type="text"
                            placeholder="뉴스 검색..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="search-input"
                        />
                        <button type="submit" className="search-btn">
                            <i className="bi bi-search"></i>
                        </button>
                    </form>
                </div>

                {/* Categories */}
                <div className="category-filter">
                    <button
                        className={`category-btn ${!category ? 'active' : ''}`}
                        onClick={() => handleCategoryChange('')}
                    >
                        All
                    </button>
                    {CATEGORIES.map((cat) => (
                        <button
                            key={cat}
                            className={`category-btn ${category === cat.toLowerCase() ? 'active' : ''}`}
                            onClick={() => handleCategoryChange(cat)}
                        >
                            {cat}
                        </button>
                    ))}
                </div>
            </header>

            {/* Main Content */}
            <main className="discover-content">
                {error ? (
                    <div className="error-container">
                        <p>{error}</p>
                        <button onClick={fetchNews} className="btn btn-primary">다시 시도</button>
                    </div>
                ) : (
                    <div className="headlines-list">
                        {query && (
                            <p className="search-result-info">
                                <i className="bi bi-search me-1"></i>
                                "{query}" 검색 결과
                            </p>
                        )}
                        {articles.length ? (
                            articles.map((article, index) => (
                                <article key={index} className="headline-item">
                                    {article.thumbnailUrl && (
                                        <img
                                            src={article.thumbnailUrl}
                                            alt=""
                                            className="headline-thumbnail"
                                            onError={(e) => (e.currentTarget.style.display = 'none')}
                                        />
                                    )}
                                    <div className="headline-content">
                                        <a href={article.link} target="_blank" rel="noopener noreferrer" className="headline-title">
                                            {article.title}
                                        </a>
                                        {article.description && (
                                            <p className="headline-description">{article.description}</p>
                                        )}
                                        <div className="headline-meta">
                                            {article.sourceName && <span className="source">{article.sourceName}</span>}
                                            {article.publishedDate && <span className="date">{formatDate(article.publishedDate)}</span>}
                                            <button
                                                className={`bookmark-btn ${savedUrls.has(article.link) ? 'saved' : ''}`}
                                                onClick={() => handleSaveArticle(article)}
                                                title="나중에 보기"
                                            >
                                                <i className={`bi ${savedUrls.has(article.link) ? 'bi-bookmark-fill' : 'bi-bookmark'}`}></i>
                                            </button>
                                        </div>
                                    </div>
                                </article>
                            ))
                        ) : (
                            <div className="empty-state">
                                <i className="bi bi-newspaper"></i>
                                <p>검색 결과가 없습니다</p>
                            </div>
                        )}
                    </div>
                )}
            </main>
        </>
    );
}
