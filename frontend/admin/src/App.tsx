import { useState } from "react";
import AuthContext from "./AuthContext";
import Navbar, { getInitialPage } from "./Navbar";
import LoginPage from "./pages/LoginPage";
import AdminsPage from "./pages/AdminsPage";
import OrganizationsPage from "./pages/OrganizationsPage";
import RolesPage from "./pages/RolesPage";
import ItemsPage from "./pages/ItemsPage";
import OrdersPage from "./pages/OrdersPage";
import UsersPage from "./pages/UsersPage";

function Shell({ token, currentAdmin }) {
    const perms = currentAdmin.permissions || [];
    const [page, setPage] = useState(() => getInitialPage(perms));

    return (
        <AuthContext.Provider value={{ token, currentAdmin }}>
            <div className="shell">
                <Navbar perms={perms} page={page} onNavigate={setPage} />
                <main className="content">
                    {page === "admins" && <AdminsPage />}
                    {page === "roles" && <RolesPage />}
                    {page === "organizations" && <OrganizationsPage />}
                    {page === "orders" && <OrdersPage />}
                    {page === "users" && <UsersPage />}
                    {page === "items" && <ItemsPage />}
                </main>
            </div>
        </AuthContext.Provider>
    );
}

export default function App() {
    const [token, setToken] = useState(null);
    const [currentAdmin, setCurrentAdmin] = useState(null);
    if (!token) {
        return <LoginPage onLogin={(session, admin) => { setToken(session); setCurrentAdmin(admin); }} />;
    }
    return <Shell token={token} currentAdmin={currentAdmin} />;
}
