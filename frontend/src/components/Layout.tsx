import { ReactNode } from 'react';
import { useLocation, Link } from 'react-router-dom';

interface LayoutProps {
    children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
    const location = useLocation();

    const isActive = (path: string) => location.pathname === path;

    return (
        <div className="app-container">
            {/* Sidebar */}
            <aside className="app-sidebar">
                <div className="sidebar-logo">
                    <i className="bi bi-rss-fill text-feedly me-2"></i>
                    Feedly Clone
                </div>

                <nav className="sidebar-nav">
                    <Link to="/discover" className={`sidebar-item ${isActive('/discover') ? 'active' : ''}`}>
                        <i className="bi bi-compass"></i>
                        <span>Explore</span>
                    </Link>

                    <Link to="/discover/news" className={`sidebar-item ${isActive('/discover/news') ? 'active' : ''}`}>
                        <i className="bi bi-newspaper"></i>
                        <span>News</span>
                    </Link>

                    <Link to="/discover/reddit" className={`sidebar-item ${isActive('/discover/reddit') ? 'active' : ''}`}>
                        <i className="bi bi-reddit"></i>
                        <span>Reddit</span>
                    </Link>
                </nav>

                <div className="nav-section-title">Library</div>

                <nav className="sidebar-nav">
                    <Link to="/saved" className={`sidebar-item ${isActive('/saved') ? 'active' : ''}`}>
                        <i className="bi bi-bookmark"></i>
                        <span>Read Later</span>
                    </Link>
                </nav>
            </aside>

            {/* Main Content */}
            <main className="app-main">
                <div>
                    {children}
                </div>
            </main>
        </div>
    );
}
