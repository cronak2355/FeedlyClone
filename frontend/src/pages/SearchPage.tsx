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

export default function SearchPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [articles, setArticles] = useState<NewsArticle[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchInput, setSearchInput] = useState('');
    const [savedUrls, setSavedUrls] = useState<Set<string>>(new Set());

    const query = searchParams.get('query') || '';
    const category = searchParams.get('category') || '';

    useEffect(() => {
        if (query) {
            fetchNews();
        }
        fetchSavedUrls();
    }, [query, category]);

    useEffect(() => {
        setSearchInput(query);
    }, [query]);

    const fetchNews = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams();
            params.set('view', 'headlines');
            if (query) params.set('query', query);
            if (category) params.set('category', category.toLowerCase());

            const response = await fetch(`http://localhost:8080/api/discover?${params}`, {
                credentials: 'include',
            });

            if (response.ok) {
                const data = await response.json();
                setArticles(data.headlines || []);
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

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (searchInput.trim()) {
            const params: Record<string, string> = { query: searchInput.trim() };
            if (category) params.category = category;
            setSearchParams(params);
        }
    };

    const handleCategoryChange = (cat: string) => {
        const params: Record<string, string> = {};
        if (query) params.query = query;
        if (cat) params.category = cat.toLowerCase();
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

    return (
        <>
            <header className="discover-header">
                <div className="header-top">
                    <h1>
                        <i className="bi bi-search me-2"></i>
                        Search
                    </h1>
                </div>

                {/* 검색 폼 */}
                <form onSubmit={handleSearch} className="search-form-large">
                    <input
                        type="text"
                        placeholder="뉴스 검색..."
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value)}
                        className="search-input-large"
                    />
                    <button type="submit" className="search-btn-large">
                        <i className="bi bi-search"></i> 검색
                    </button>
                </form>

                {/* 카테고리 필터 */}
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

            <main className="discover-content">
                {!query ? (
                    <div className="search-empty-state">
                        <i className="bi bi-search"></i>
                        <h3>검색어를 입력하세요</h3>
                        <p>기사, 뉴스를 검색할 수 있습니다.</p>
                    </div>
                ) : loading ? (
                    <div className="loading-container">
                        <div className="spinner"></div>
                        <p>Searching...</p>
                    </div>
                ) : (
                    <div className="headlines-list">
                        <p className="search-result-info">
                            <i className="bi bi-search me-1"></i>
                            "{query}" 검색 결과 ({articles.length}건)
                        </p>
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
                                <i className="bi bi-search"></i>
                                <p>검색 결과가 없습니다</p>
                            </div>
                        )}
                    </div>
                )}
            </main>
        </>
    );
}
