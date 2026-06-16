import { useState, useEffect } from "react";

function OrganizationDetail({ org, currentAdmin, token, onBack, onSaved, onDeleted }) {
    const canEdit = currentAdmin.permissions.includes("EDIT_ORG") || currentAdmin.permissions.includes("CREATE_ORG");
    const canDelete = currentAdmin.permissions.includes("DELETE_ORG");
    const [name, setName] = useState(org.name || "");
    const [locale, setLocale] = useState(org.defaultLocale || "");
    const [error, setError] = useState(null);

    async function handleSave() {
        setError(null);
        try {
            const res = await fetch("/admin/organizations/" + org.id, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token,
                },
                body: JSON.stringify({ organization: { name, defaultLocale: locale } }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") {
                onSaved();
            } else {
                setError((body.errors && body.errors.join(", ")) || "Could not save organization");
            }
        } catch (err) {
            setError("Could not reach the server");
        }
    }

    async function handleDelete() {
        setError(null);
        try {
            const res = await fetch("/admin/organizations/" + org.id, {
                method: "DELETE",
                headers: { "Authorization": "Bearer " + token },
            });
            const body = await res.json();
            if (body.errorType === "NONE") {
                onDeleted();
            } else {
                setError((body.errors && body.errors.join(", ")) || "Could not delete organization");
            }
        } catch (err) {
            setError("Could not reach the server");
        }
    }

    async function handleUndelete() {
        setError(null);
        try {
            const res = await fetch("/admin/organizations/" + org.id, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token,
                },
                body: JSON.stringify({ organization: { isDeleted: false } }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") {
                onSaved();
            } else {
                setError((body.errors && body.errors.join(", ")) || "Could not undelete organization");
            }
        } catch (err) {
            setError("Could not reach the server");
        }
    }

    return (
        <div>
            <button className="back-link" onClick={onBack}>← Back to organizations</button>
            <div className="detail-header">
                <h1>Edit Organization</h1>
                {canEdit && <button onClick={handleSave}>Save</button>}
            </div>
            <div className="edit-form">
                <label>Name
                    <input type="text" value={name} onChange={(e) => setName(e.target.value)} />
                </label>
                <label>Default locale
                    <input type="text" value={locale} onChange={(e) => setLocale(e.target.value)} />
                </label>
            </div>
            {canDelete && (
                <div>
                    {org.isDeleted
                        ? <button onClick={handleUndelete}>Undelete</button>
                        : <button className="danger" onClick={handleDelete}>Delete</button>}
                </div>
            )}
            {error && <div className="error">{error}</div>}
        </div>
    );
}

function OrganizationCreate({ token, onBack, onCreated }) {
    const [name, setName] = useState("");
    const [locale, setLocale] = useState("");
    const [error, setError] = useState(null);

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        try {
            const res = await fetch("/admin/organizations", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token,
                },
                body: JSON.stringify({ organization: { name, defaultLocale: locale } }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") {
                onCreated();
            } else {
                setError((body.errors && body.errors.join(", ")) || "Could not create organization");
            }
        } catch (err) {
            setError("Could not reach the server");
        }
    }

    return (
        <div>
            <button className="back-link" onClick={onBack}>← Back to organizations</button>
            <h1>Add Organization</h1>
            <form onSubmit={handleSubmit}>
                <input type="text" placeholder="Name" value={name}
                       onChange={(e) => setName(e.target.value)} />
                <input type="text" placeholder="Default locale" value={locale}
                       onChange={(e) => setLocale(e.target.value)} />
                <button type="submit">Create</button>
                {error && <div className="error">{error}</div>}
            </form>
        </div>
    );
}

export default function OrganizationsPage({ token, currentAdmin }) {
    const [orgs, setOrgs] = useState(null);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);

    async function load() {
        setError(null);
        try {
            const res = await fetch("/admin/organizations", {
                headers: { "Authorization": "Bearer " + token },
            });
            const body = await res.json();
            if (body.errorType === "NONE") {
                setOrgs(body.data);
            } else {
                setError((body.errors && body.errors[0]) || "Could not load organizations");
            }
        } catch (err) {
            setError("Could not reach the server");
        }
    }

    useEffect(() => { load(); }, [token]);

    if (creating) {
        return (
            <OrganizationCreate
                token={token}
                onBack={() => setCreating(false)}
                onCreated={() => { setCreating(false); load(); }}
            />
        );
    }

    if (selected) {
        return (
            <OrganizationDetail
                org={selected}
                currentAdmin={currentAdmin}
                token={token}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); load(); }}
                onDeleted={() => { setSelected(null); load(); }}
            />
        );
    }

    return (
        <div>
            <h1>Organizations</h1>
            {currentAdmin.permissions.includes("CREATE_ORG") &&
                <div style={{ marginBottom: "12px" }}><button onClick={() => setCreating(true)}>Add New</button></div>}
            {error && <div className="error">{error}</div>}
            {!orgs && !error && <div>Loading…</div>}
            {orgs && orgs.map((o) => (
                <div className={"admin-row" + (o.isDeleted ? " deleted" : "")} key={o.id} onClick={() => setSelected(o)}>
                    <span className="name">{o.name}</span>
                    <span className="meta"> — {o.defaultLocale}</span>
                    <span className="deleted-tag">{o.isDeleted ? "Deleted" : ""}</span>
                </div>
            ))}
        </div>
    );
}
