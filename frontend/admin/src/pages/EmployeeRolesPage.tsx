import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { saveResource, loadResource, deleteResource } from "../apiUtils";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

const ALL_EMPLOYEE_PERMISSIONS = [
    "CREATE_ITEM", "DELETE_ITEM", "CREATE_LOCATION", "DELETE_LOCATION",
];

function PermissionChecklist({ selected, onToggle }) {
    return (
        <div className="edit-form">
            {ALL_EMPLOYEE_PERMISSIONS.map((p) => (
                <label key={p} style={{ flexDirection: "row", alignItems: "center", gap: "8px" }}>
                    <input type="checkbox" checked={selected.includes(p)} onChange={() => onToggle(p)} />
                    {p}
                </label>
            ))}
        </div>
    );
}

function EmployeeRoleDetail({ orgId, role, onBack, onSaved, onDeleted }) {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG");
    const [name, setName] = useState(role.name || "");
    const [perms, setPerms] = useState(role.permissions || []);
    const [error, setError] = useState(null);

    function toggle(p) {
        setPerms((prev) => prev.includes(p) ? prev.filter((x) => x !== p) : [...prev, p]);
    }

    const handleSave = () =>
        saveResource(
            api, "PATCH",
            `/admin/organizations/${orgId}/employee-roles/${role.id}`,
            { role: { name, permissions: perms } },
            setError, onSaved, "Could not save role"
        );

    const handleDelete = () =>
        deleteResource(
            api,
            `/admin/organizations/${orgId}/employee-roles/${role.id}`,
            setError, onDeleted, "Could not delete role"
        );

    return (
        <DetailView
            title="Edit Employee Role"
            backLabel="Back to employee roles"
            onBack={onBack}
            canSave={canEdit}
            onSave={handleSave}
            canDelete={canEdit}
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

function EmployeeRoleCreate({ orgId, onBack, onCreated }) {
    const api = useApi();
    const [name, setName] = useState("");
    const [perms, setPerms] = useState([]);
    const [error, setError] = useState(null);

    function toggle(p) {
        setPerms((prev) => prev.includes(p) ? prev.filter((x) => x !== p) : [...prev, p]);
    }

    async function handleSubmit(e) {
        e.preventDefault();
        await saveResource(
            api, "POST",
            `/admin/organizations/${orgId}/employee-roles`,
            { role: { name, permissions: perms } },
            setError, onCreated, "Could not create role"
        );
    }

    return (
        <div>
            <button className="back-link" onClick={onBack}>← Back to employee roles</button>
            <h1>Add Employee Role</h1>
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

export default function EmployeeRolesPage({ orgId, onOrgChange }) {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG");
    const [orgs, setOrgs] = useState(null);
    const [roles, setRoles] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadResource(api, "/admin/organizations", setError, setOrgs, "Could not load organizations");
    }, []);

    function loadRoles() {
        if (!orgId) { setRoles(null); return; }
        loadResource(
            api,
            `/admin/organizations/${orgId}/employee-roles`,
            setError, setRoles, "Could not load employee roles"
        );
    }

    useEffect(() => {
        setSelected(null);
        setCreating(false);
        setRoles(null);
        loadRoles();
    }, [orgId]);

    if (orgId && creating) {
        return (
            <EmployeeRoleCreate
                orgId={orgId}
                onBack={() => setCreating(false)}
                onCreated={() => { setCreating(false); loadRoles(); }}
            />
        );
    }

    if (orgId && selected) {
        return (
            <EmployeeRoleDetail
                orgId={orgId}
                role={selected}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); loadRoles(); }}
                onDeleted={() => { setSelected(null); loadRoles(); }}
            />
        );
    }

    return (
        <PageList
            title="Employee Roles"
            orgs={orgs ?? []}
            orgId={orgId}
            onOrgChange={onOrgChange}
            canAdd={!!orgId && canEdit}
            onAdd={() => setCreating(true)}
            loading={!!orgId && !roles && !error}
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
