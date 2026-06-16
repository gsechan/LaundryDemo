import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { saveResource, loadResource, deleteResource } from "../apiUtils";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

function RoleMembershipRow({ roles, locations, membership, onChange, onRemove }) {
    return (
        <div style={{ display: "flex", gap: "8px", alignItems: "center", marginBottom: "6px" }}>
            <select
                value={membership.roleId}
                onChange={(e) => onChange({ ...membership, roleId: e.target.value })}
            >
                <option value="">— select role —</option>
                {roles.map((r) => (
                    <option key={r.id} value={r.id}>{r.name}</option>
                ))}
            </select>
            <select
                value={membership.locationId ?? ""}
                onChange={(e) => onChange({ ...membership, locationId: e.target.value || null })}
            >
                <option value="">All</option>
                {locations.map((l) => (
                    <option key={l.id} value={l.id}>{l.name}</option>
                ))}
            </select>
            <button type="button" onClick={onRemove}>✕</button>
        </div>
    );
}

function EmployeeDetail({ orgId, employee, onBack, onSaved, onDeleted }) {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG");

    const [name, setName] = useState(employee.name || "");
    const [email, setEmail] = useState(employee.email || "");
    const [phone, setPhone] = useState(employee.phone || "");
    const [roles, setRoles] = useState(null);
    const [locations, setLocations] = useState(null);
    // Each row: { id: string|null, roleId: string, locationId: string|null }
    const [memberships, setMemberships] = useState(null);
    // Snapshot of server-side membership IDs at load time, for diffing on save
    const [originalMembershipIds, setOriginalMembershipIds] = useState<Set<string>>(new Set());
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!canEdit) return;
        loadResource(api, `/admin/organizations/${orgId}/employee-roles`, setError, setRoles, "Could not load roles");
        loadResource(api, `/admin/organizations/${orgId}/locations`, setError, setLocations, "Could not load locations");
        loadResource(
            api,
            `/admin/organizations/${orgId}/employee-role-memberships?employeeId=${employee.id}`,
            setError,
            (list) => {
                const rows = list.map((m) => ({ id: m.id, roleId: m.roleId, locationId: m.locationId ?? null }));
                setMemberships(rows);
                setOriginalMembershipIds(new Set(rows.map((r) => r.id).filter(Boolean)));
            },
            "Could not load role memberships"
        );
    }, [canEdit]);

    function addMembershipRow() {
        setMemberships((prev) => [...prev, { id: null, roleId: "", locationId: null }]);
    }

    function updateRow(i, updated) {
        setMemberships((prev) => prev.map((m, idx) => idx === i ? updated : m));
    }

    function removeRow(i) {
        setMemberships((prev) => prev.filter((_, idx) => idx !== i));
    }

    async function handleSave() {
        setError(null);
        try {
            const patchRes = await api(`/admin/organizations/${orgId}/employees/${employee.id}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ employee: { name, email, phone } }),
            });
            const patchBody = await patchRes.json();
            if (patchBody.errorType !== "NONE") {
                setError((patchBody.errors && patchBody.errors.join(", ")) || "Could not save employee");
                return;
            }

            // IDs still present in current state (rows that were loaded from the server and kept)
            const currentIds = new Set(memberships.filter((m) => m.id).map((m) => m.id));
            // Any original server row whose id is no longer in the list was removed by the user
            const toRemove = [...originalMembershipIds].filter((id) => !currentIds.has(id));
            const toAdd = memberships.filter((m) => !m.id && m.roleId);

            for (const membershipId of toRemove) {
                const res = await api(
                    `/admin/organizations/${orgId}/employee-role-memberships/${membershipId}`,
                    { method: "DELETE" }
                );
                const body = await res.json();
                if (body.errorType !== "NONE") {
                    setError((body.errors && body.errors.join(", ")) || "Could not remove role");
                    return;
                }
            }

            for (const m of toAdd) {
                const res = await api(`/admin/organizations/${orgId}/employee-role-memberships`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ employeeId: employee.id, roleId: m.roleId, locationId: m.locationId || null }),
                });
                const body = await res.json();
                if (body.errorType !== "NONE") {
                    setError((body.errors && body.errors.join(", ")) || "Could not assign role");
                    return;
                }
            }

            onSaved();
        } catch (err) {
            setError("Could not reach the server");
        }
    }

    const handleDelete = () =>
        deleteResource(
            api,
            `/admin/organizations/${orgId}/employees/${employee.id}`,
            setError,
            onDeleted,
            "Could not delete employee"
        );

    const rolesAndLocationsLoaded = roles !== null && locations !== null && memberships !== null;

    return (
        <DetailView
            title={employee.name || "(no name)"}
            backLabel="Back to employees"
            onBack={onBack}
            canSave={canEdit}
            onSave={handleSave}
            canDelete={canEdit}
            onDelete={handleDelete}
            error={error}
        >
            {canEdit ? (
                <div className="edit-form">
                    <label>Name
                        <input type="text" value={name} onChange={(e) => setName(e.target.value)} />
                    </label>
                    <label>Email
                        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                    </label>
                    <label>Phone
                        <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} />
                    </label>
                </div>
            ) : (
                <>
                    <div className="detail-field"><span className="label">Email:</span> {employee.email}</div>
                    <div className="detail-field"><span className="label">Phone:</span> {employee.phone}</div>
                </>
            )}

            {canEdit && (
                <div>
                    <h3>Roles</h3>
                    {!rolesAndLocationsLoaded && !error && <div>Loading…</div>}
                    {rolesAndLocationsLoaded && (
                        <>
                            {memberships.map((m, i) => (
                                <RoleMembershipRow
                                    key={i}
                                    roles={roles}
                                    locations={locations}
                                    membership={m}
                                    onChange={(updated) => updateRow(i, updated)}
                                    onRemove={() => removeRow(i)}
                                />
                            ))}
                            <button type="button" onClick={addMembershipRow}>+ Add role</button>
                        </>
                    )}
                </div>
            )}
        </DetailView>
    );
}

function EmployeeCreate({ orgId, onBack, onCreated }) {
    const api = useApi();
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState(null);

    async function handleSubmit(e) {
        e.preventDefault();
        if (password !== confirm) {
            setError("Passwords do not match");
            return;
        }
        await saveResource(
            api,
            "POST",
            `/admin/organizations/${orgId}/employees`,
            { employee: { name, email, phone }, password },
            setError,
            onCreated,
            "Could not create employee"
        );
    }

    return (
        <div>
            <button className="back-link" onClick={onBack}>← Back to employees</button>
            <h1>Add Employee</h1>
            <form onSubmit={handleSubmit}>
                <input type="text" placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} />
                <input type="tel" placeholder="Phone" value={phone} onChange={(e) => setPhone(e.target.value)} />
                <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} />
                <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
                <input type="password" placeholder="Confirm password" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
                <button type="submit">Create</button>
                {error && <div className="error">{error}</div>}
            </form>
        </div>
    );
}

export default function EmployeesPage({ orgId, onOrgChange }) {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG");
    const [orgs, setOrgs] = useState(null);
    const [employees, setEmployees] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadResource(api, "/admin/organizations", setError, setOrgs, "Could not load organizations");
    }, []);

    function loadEmployees() {
        if (!orgId) { setEmployees(null); return; }
        loadResource(
            api,
            `/admin/organizations/${orgId}/employees`,
            setError,
            setEmployees,
            "Could not load employees"
        );
    }

    useEffect(() => {
        setSelected(null);
        setCreating(false);
        setEmployees(null);
        loadEmployees();
    }, [orgId]);

    if (orgId && creating) {
        return (
            <EmployeeCreate
                orgId={orgId}
                onBack={() => setCreating(false)}
                onCreated={() => { setCreating(false); loadEmployees(); }}
            />
        );
    }

    if (orgId && selected) {
        return (
            <EmployeeDetail
                orgId={orgId}
                employee={selected}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); loadEmployees(); }}
                onDeleted={() => { setSelected(null); loadEmployees(); }}
            />
        );
    }

    return (
        <PageList
            title="Employees"
            orgs={orgs ?? []}
            orgId={orgId}
            onOrgChange={onOrgChange}
            canAdd={!!orgId && canEdit}
            onAdd={() => setCreating(true)}
            loading={!!orgId && !employees && !error}
            error={error}
        >
            {employees && employees.map((emp) => (
                <div className="admin-row" key={emp.id} onClick={() => setSelected(emp)}>
                    <span className="name">{emp.name}</span>
                    <span className="meta"> — {emp.email} — {emp.phone}</span>
                </div>
            ))}
        </PageList>
    );
}
