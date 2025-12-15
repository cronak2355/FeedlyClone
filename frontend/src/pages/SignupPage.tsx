import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../styles/signup.css';

export default function SignupPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await fetch('http://localhost:8080/api/account/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password }),
                credentials: 'include',
            });

            const data = await response.json();

            if (data.success) {
                alert('회원가입 성공! 로그인 페이지로 이동합니다.');
                navigate('/login');
            } else {
                setError(data.message || '회원가입에 실패했습니다.');
            }
        } catch (err) {
            console.error('Signup error:', err);
            setError('서버 연결에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="signup-container">
            <div className="signup-card">
                <h2 className="signup-title">Create Account</h2>

                {error && <div className="alert alert-danger mb-3">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label htmlFor="email" className="form-label">Email</label>
                        <input
                            type="email"
                            className="form-control"
                            id="email"
                            name="email"
                            placeholder="name@example.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">Password</label>
                        <input
                            type="password"
                            className="form-control"
                            id="password"
                            name="password"
                            placeholder="Enter your password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading}>
                        {loading ? 'Processing...' : 'Sign Up'}
                    </button>
                </form>

                <div className="signup-footer">
                    Already have an account? <Link to="/login">Log in</Link>
                </div>
            </div>
        </div>
    );
}
