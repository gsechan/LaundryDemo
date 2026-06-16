import { useState } from "react";
import AuthContext, { makeAuthValue } from "./AuthContext";
import Navbar, { getInitialPage } from "./Navbar";
import LoginPage from "./pages/LoginPage";
import AdminsPage from "./pages/AdminsPage";
import OrganizationsPage from "./pages/OrganizationsPage";
import RolesPage from "./pages/RolesPage";
import ItemsPage from "./pages/ItemsPage";
import LocationsPage from "./pages/LocationsPage";
import OrdersPage from "./pages/OrdersPage";
import UsersPage from "./pages/UsersPage";
import EmployeesPage from "./pages/EmployeesPage";

function Shell({ token, currentAdmin }) {
    const perms = currentAdmin.permissions || [];
    const [page, setPage] = useState(() => getInitialPage(perms));
    const [orgId, setOrgId] = useState("");
    const [itemLocationId, setItemLocationId] = useState("");

    function handleOrgChange(id: string) {
        setOrgId(id);
        setItemLocationId("");
    }

    return (
        <AuthContext.Provider value={makeAuthValue(token, currentAdmin)}>
            <div className="shell">
                <Navbar perms={perms} page={page} onNavigate={setPage} />
                <main className="content">
                    {page === "admins" && <AdminsPage />}
                    {page === "roles" && <RolesPage />}
                    {page === "organizations" && <OrganizationsPage />}
                    {page === "locations" && <LocationsPage orgId={orgId} onOrgChange={handleOrgChange} />}
                    {page === "orders" && <OrdersPage orgId={orgId} onOrgChange={handleOrgChange} />}
                    {page === "users" && <UsersPage orgId={orgId} onOrgChange={handleOrgChange} />}
                    {page === "employees" && <EmployeesPage orgId={orgId} onOrgChange={handleOrgChange} />}
                    {page === "items" && (
                        <ItemsPage
                            orgId={orgId}
                            onOrgChange={handleOrgChange}
                            locationId={itemLocationId}
                            onLocationChange={setItemLocationId}
                        />
                    )}
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
