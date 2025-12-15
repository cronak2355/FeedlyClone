import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout.tsx';
import LoginPage from './pages/LoginPage.tsx';
import SignupPage from './pages/SignupPage.tsx';
import DiscoverPage from './pages/DiscoverPage.tsx';
import NewsPage from './pages/NewsPage.tsx';
import RedditPage from './pages/RedditPage.tsx';
import ReadLaterPage from './pages/ReadLaterPage.tsx';
import './App.css';

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route path="/*" element={
                    <Layout>
                        <Routes>
                            <Route path="/discover" element={<DiscoverPage />} />
                            <Route path="/discover/news" element={<NewsPage />} />
                            <Route path="/discover/reddit" element={<RedditPage />} />
                            <Route path="/news" element={<NewsPage />} />
                            <Route path="/reddit" element={<RedditPage />} />
                            <Route path="/saved" element={<ReadLaterPage />} />
                            <Route path="/read-later" element={<ReadLaterPage />} />
                            <Route path="/" element={<Navigate to="/discover" replace />} />
                        </Routes>
                    </Layout>
                } />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
