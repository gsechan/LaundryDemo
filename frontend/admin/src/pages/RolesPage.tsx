import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { saveResource, loadResource, deleteResource } from "../apiUtils";
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

    const handleSave = () => saveResource(api, "PATCH", "/admin/roles/" + role.id,
        { role: { name, permissions: perms } }, setError, onSaved, "Could not save role");

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
        await saveResource(api, "POST", "/admin/roles",
            { role: { name, permissions: perms } }, setError, onCreated, "Could not create role");
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
        await loadResource(api, "/admin/roles", setError, setRoles, "Could not load roles");
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
