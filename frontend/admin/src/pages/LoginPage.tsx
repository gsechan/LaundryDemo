import { useState } from "react";

export default function LoginPage({ onLogin }) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        setLoading(true);
        try {
            const res = await fetch("/admin/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") {
                onLogin(body.data.session, body.data.admin);
            } else {
                setError((body.errors && body.errors[0]) || "Login failed");
            }
        } catch (err) {
            setError("Could not reach the server");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-wrap">
            <form onSubmit={handleSubmit}>
                <h2>GabePOS Admin Login</h2>
                <input type="email" placeholder="Email" value={email}
                       onChange={(e) => setEmail(e.target.value)} />
                <input type="password" placeholder="Password" value={password}
                       onChange={(e) => setPassword(e.target.value)} />
                <button type="submit" disabled={loading}>{loading ? "Logging in…" : "Log in"}</button>
                {error && <div className="error">{error}</div>}
            </form>
        </div>
    );
}
