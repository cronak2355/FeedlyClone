import { ReactNode } from 'react';
import { useLocation, Link } from 'react-router-dom';

interface LayoutProps {
    children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
    const location = useLocation();

    const isActive = (path: string) => location.pathname === path;
    const isActiveStartsWith = (path: string) => location.pathname.startsWith(path);

    return (
        <div className="app-container">
            {/* Sidebar */}
            <aside className="app-sidebar">
                <div className="sidebar-logo">
                    <i className="bi bi-rss-fill text-feedly me-2"></i>
                    Feedly Clone
                </div>

                <nav className="sidebar-nav">
                    <Link to="/today" className={`sidebar-item ${isActiveStartsWith('/today') ? 'active' : ''}`}>
                        <i className="bi bi-sun"></i>
                        <span>Today</span>
                    </Link>

                    {/* Follow Sources - 클릭하면 페이지로 이동 */}
                    <Link to="/follow-sources" className={`sidebar-item ${isActiveStartsWith('/follow-sources') ? 'active' : ''}`}>
                        <i className="bi bi-compass"></i>
                        <span>Follow Sources</span>
                    </Link>

                    {/* Search */}
                    <Link to="/search" className={`sidebar-item ${isActiveStartsWith('/search') ? 'active' : ''}`}>
                        <i className="bi bi-search"></i>
                        <span>Search</span>
                    </Link>

                    <Link to="/discover/createAIfeed" className={`sidebar-item ${isActive('/discover/createAIfeed') ? 'active' : ''}`}>
                        <i className="bi bi-CreateAIFeedPage"></i>
                        <span>CreateAIFeed</span>
                    </Link>
                </nav>

                <div className="nav-section-title">Library</div>

                    <Link to="/saved" className={`sidebar-item ${isActive('/saved') ? 'active' : ''}`}>
                        <i className="bi bi-bookmark"></i>
                        <span>Read Later</span>
                    </Link>
                </nav>

                <div className="nav-section-title">Feeds</div>
                <nav className="sidebar-nav">
                    <div className="sidebar-item text-muted small">
                        <i className="bi bi-plus"></i>
                        <span>Add Content</span>
                    </div>
                </nav>

                <div className="nav-section-title">Boards</div>
                <nav className="sidebar-nav">
                    <div className="sidebar-item text-muted small">
                        <i className="bi bi-plus"></i>
                        <span>Create Board</span>
                    </div>
                </nav>

                {/* Footer Links */}
                <div className="sidebar-footer">
                    <a href="#" className="sidebar-item text-muted small">
                        <i className="bi bi-gear"></i>
                        <span>Integrations & API</span>
                    </a>
                    <a href="#" className="sidebar-item text-muted small">
                        <i className="bi bi-pencil"></i>
                        <span>Blog</span>
                    </a>
                    <a href="#" className="sidebar-item text-muted small">
                        <i className="bi bi-question-circle"></i>
                        <span>Learn & Get Support</span>
                    </a>
                </div>
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
