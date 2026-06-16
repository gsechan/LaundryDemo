import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { deleteResource } from "../apiUtils";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

const ALL_PERMISSIONS = [
    "CREATE_ORG", "DELETE_ORG", "EDIT_ORG",
    "CREATE_ADMIN", "DELETE_ADMIN", "ASSIGN_ADMIN_ROLES",
];

function PermissionChecklist({ selected, onToggle }) {
    return (
        <div className="edit-form">
            {ALL_PERMISSIONS.map((p) => (
                <label key={p} style={{ flexDirection: "row", alignItems: "center", gap: "8px" }}>
                    <input type="checkbox" checked={selected.includes(p)} onChange={() => onToggle(p)} />
                    {p}
                </label>
            ))}
        </div>
    );
}

function RoleDetail({ role, onBack, onSaved, onDeleted }) {
    const { currentAdmin } = useAuth();
    const api = useApi();
    const canAssign = currentAdmin.permissions.includes("ASSIGN_ADMIN_ROLES");
    const [name, setName] = useState(role.name || "");
    const [perms, setPerms] = useState(role.permissions || []);
    const [error, setError] = useState(null);

    function toggle(p) {
        setPerms((prev) => prev.includes(p) ? prev.filter((x) => x !== p) : [...prev, p]);
    }

    async function handleSave() {
        setError(null);
        try {
            const res = await api("/admin/roles/" + role.id, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ role: { name, permissions: perms } }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") { onSaved(); }
            else { setError((body.errors && body.errors.join(", ")) || "Could not save role"); }
        } catch (err) { setError("Could not reach the server"); }
    }

    const handleDelete = () => deleteResource(api, "/admin/roles/" + role.id, setError, onDeleted, "Could not delete role");

    return (
        <DetailView
            title="Edit Role"
            backLabel="Back to roles"
            onBack={onBack}
            canSave={canAssign}
            onSave={handleSave}
            canDelete={canAssign}
            onDelete={handleDelete}
            error={error}
        >
            <div className="edit-form">
                <label>Name
                    <input type="text" value={name} onChange={(e) => setName(e.target.value)} />
                </label>
            </div>
            <h3>Permissions</h3>
            <PermissionChecklist selected={perms} onToggle={toggle} />
        </DetailView>
    );
}

function RoleCreate({ onBack, onCreated }) {
    const api = useApi();
    const [name, setName] = useState("");
    const [perms, setPerms] = useState([]);
    const [error, setError] = useState(null);

    function toggle(p) {
        setPerms((prev) => prev.includes(p) ? prev.filter((x) => x !== p) : [...prev, p]);
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        try {
            const res = await api("/admin/roles", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ role: { name, permissions: perms } }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") { onCreated(); }
            else { setError((body.errors && body.errors.join(", ")) || "Could not create role"); }
        } catch (err) { setError("Could not reach the server"); }
    }

    return (
        <div>
            <button className="back-link" onClick={onBack}>← Back to roles</button>
            <h1>Add Role</h1>
            <form onSubmit={handleSubmit}>
                <input type="text" placeholder="Name" value={name}
                       onChange={(e) => setName(e.target.value)} />
                <h3>Permissions</h3>
                <PermissionChecklist selected={perms} onToggle={toggle} />
                <button type="submit">Create</button>
                {error && <div className="error">{error}</div>}
            </form>
        </div>
    );
}

export default function RolesPage() {
    const { currentAdmin } = useAuth();
    const api = useApi();
    const [roles, setRoles] = useState(null);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);

    async function load() {
        setError(null);
        try {
            const res = await api("/admin/roles");
            const body = await res.json();
            if (body.errorType === "NONE") { setRoles(body.data); }
            else { setError((body.errors && body.errors[0]) || "Could not load roles"); }
        } catch (err) { setError("Could not reach the server"); }
    }

    useEffect(() => { load(); }, []);

    if (creating) {
        return (
            <RoleCreate
                onBack={() => setCreating(false)}
                onCreated={() => { setCreating(false); load(); }}
            />
        );
    }

    if (selected) {
        return (
            <RoleDetail
                role={selected}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); load(); }}
                onDeleted={() => { setSelected(null); load(); }}
            />
        );
    }

    return (
        <PageList
            title="Roles"
            canAdd={currentAdmin.permissions.includes("ASSIGN_ADMIN_ROLES")}
            onAdd={() => setCreating(true)}
            loading={!roles && !error}
            error={error}
        >
            {roles && roles.map((r) => (
                <div className="admin-row" key={r.id} onClick={() => setSelected(r)}>
                    <span className="name">{r.name}</span>
                    <span className="meta"> — {r.permissions.join(", ")}</span>
                </div>
            ))}
        </PageList>
    );
}
