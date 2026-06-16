import { useState } from "react";
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
        <div className="shell">
            <Navbar perms={perms} page={page} onNavigate={setPage} />
            <main className="content">
                {page === "admins" && <AdminsPage token={token} currentAdmin={currentAdmin} />}
                {page === "roles" && <RolesPage token={token} currentAdmin={currentAdmin} />}
                {page === "organizations" && <OrganizationsPage token={token} currentAdmin={currentAdmin} />}
                {page === "orders" && <OrdersPage token={token} currentAdmin={currentAdmin} />}
                {page === "users" && <UsersPage token={token} />}
                {page === "items" && <ItemsPage token={token} currentAdmin={currentAdmin} />}
            </main>
        </div>
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
