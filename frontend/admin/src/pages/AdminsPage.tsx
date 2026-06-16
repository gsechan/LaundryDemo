import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { loadResource, deleteResource } from "../apiUtils";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

function AdminDetail({ admin, onBack, onSaved, onDeleted }) {
    const { currentAdmin } = useAuth();
    const api = useApi();
    const canAssign = currentAdmin.permissions.includes("ASSIGN_ADMIN_ROLES");
    const currentRoleIds = admin.roleMemberships.map((m) => m.roleId);
    const [roles, setRoles] = useState(null);
    const [checked, setChecked] = useState(currentRoleIds);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!canAssign) return;
        loadResource(api, "/admin/roles", setError,
            (data) => setRoles(data.filter((r) => r.name !== "Root")),
            "Could not load roles");
    }, [canAssign]);

    function toggle(roleId) {
        setChecked((prev) => prev.includes(roleId) ? prev.filter((x) => x !== roleId) : [...prev, roleId]);
    }

    async function handleSave() {
        setError(null);
        try {
            const toAdd = checked.filter((rid) => !currentRoleIds.includes(rid));
            const toRemove = admin.roleMemberships.filter((m) => !checked.includes(m.roleId));
            for (const roleId of toAdd) {
                const res = await api("/admin/role-memberships", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ adminId: admin.id, roleId }),
                });
                const body = await res.json();
                if (body.errorType !== "NONE") {
                    setError((body.errors && body.errors.join(", ")) || "Could not assign role");
                    return;
                }
            }
            for (const m of toRemove) {
                const res = await api("/admin/role-memberships/" + m.membershipId, { method: "DELETE" });
                const body = await res.json();
                if (body.errorType !== "NONE") {
                    setError((body.errors && body.errors.join(", ")) || "Could not remove role");
                    return;
                }
            }
            onSaved();
        } catch (err) { setError("Could not reach the server"); }
    }

    const handleDelete = () => deleteResource(api, "/admin/admins/" + admin.id, setError, onDeleted, "Could not delete admin");

    return (
        <DetailView
            title={admin.name}
            backLabel="Back to admins"
            onBack={onBack}
            canSave={canAssign}
            onSave={handleSave}
            canDelete={currentAdmin.permissions.includes("DELETE_ADMIN")}
            onDelete={handleDelete}
            error={error}
        >
            <div className="detail-field"><span className="label">Email:</span> {admin.email}</div>
            <div className="detail-field"><span className="label">Phone:</span> {admin.phone}</div>
            {canAssign && (
                <div>
                    <h3>Roles</h3>
                    {!roles && !error && <div>Loading…</div>}
                    {roles && (
                        <div className="edit-form">
                            {roles.map((r) => (
                                <label key={r.id} style={{ flexDirection: "row", alignItems: "center", gap: "8px" }}>
                                    <input type="checkbox" checked={checked.includes(r.id)} onChange={() => toggle(r.id)} />
                                    {r.name}
                                </label>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </DetailView>
    );
}

function AdminCreate({ onBack, onCreated }) {
    const api = useApi();
    const [name, setName] = useState("");
    const [phone, setPhone] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState(null);

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        if (password !== confirm) {
            setError("Passwords do not match");
            return;
        }
        try {
            const res = await api("/admin/admins", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ admin: { name, email, phone }, password }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") {
                onCreated();
            } else {
                setError((body.errors && body.errors.join(", ")) || "Could not create admin");
            }
        } catch (err) {
            setError("Could not reach the server");
        }
    }

    return (
        <div>
            <button className="back-link" onClick={onBack}>← Back to admins</button>
            <h1>Add Admin</h1>
            <form onSubmit={handleSubmit}>
                <input type="text" placeholder="Name" value={name}
                       onChange={(e) => setName(e.target.value)} />
                <input type="tel" placeholder="Phone" value={phone}
                       onChange={(e) => setPhone(e.target.value)} />
                <input type="email" placeholder="Email" value={email}
                       onChange={(e) => setEmail(e.target.value)} />
                <input type="password" placeholder="Password" value={password}
                       onChange={(e) => setPassword(e.target.value)} />
                <input type="password" placeholder="Confirm password" value={confirm}
                       onChange={(e) => setConfirm(e.target.value)} />
                <button type="submit">Create</button>
                {error && <div className="error">{error}</div>}
            </form>
        </div>
    );
}

export default function AdminsPage() {
    const { currentAdmin } = useAuth();
    const api = useApi();
    const [admins, setAdmins] = useState(null);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);

    async function load() {
        await loadResource(api, "/admin/admins", setError, setAdmins, "Could not load admins");
    }

    useEffect(() => { load(); }, []);

    if (creating) {
        return (
            <AdminCreate
                onBack={() => setCreating(false)}
                onCreated={() => { setCreating(false); load(); }}
            />
        );
    }

    if (selected) {
        return (
            <AdminDetail
                admin={selected}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); load(); }}
                onDeleted={() => { setSelected(null); load(); }}
            />
        );
    }

    return (
        <PageList
            title="Admins"
            canAdd={currentAdmin.permissions.includes("CREATE_ADMIN")}
            onAdd={() => setCreating(true)}
            loading={!admins && !error}
            error={error}
        >
            {admins && admins.map((a) => (
                <div className="admin-row" key={a.id} onClick={() => setSelected(a)}>
                    <span className="name">{a.name}</span>
                    <span className="meta"> — {a.email} — {a.phone}</span>
                </div>
            ))}
        </PageList>
    );
}
