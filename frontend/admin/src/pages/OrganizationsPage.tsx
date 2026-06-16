import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { loadResource, deleteResource } from "../apiUtils";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

function OrganizationDetail({ org, onBack, onSaved, onDeleted }) {
    const { currentAdmin } = useAuth();
    const api = useApi();
    const canEdit = currentAdmin.permissions.includes("EDIT_ORG") || currentAdmin.permissions.includes("CREATE_ORG");
    const canDelete = currentAdmin.permissions.includes("DELETE_ORG");
    const [name, setName] = useState(org.name || "");
    const [locale, setLocale] = useState(org.defaultLocale || "");
    const [error, setError] = useState(null);

    async function handleSave() {
        setError(null);
        try {
            const res = await api("/admin/organizations/" + org.id, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
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

    const handleDelete = () => deleteResource(api, "/admin/organizations/" + org.id, setError, onDeleted, "Could not delete organization");

    async function handleUndelete() {
        setError(null);
        try {
            const res = await api("/admin/organizations/" + org.id, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
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
        <DetailView
            title="Edit Organization"
            backLabel="Back to organizations"
            onBack={onBack}
            canSave={canEdit}
            onSave={handleSave}
            canDelete={canDelete}
            onDelete={org.isDeleted ? handleUndelete : handleDelete}
            deleteLabel={org.isDeleted ? "Undelete" : "Delete"}
            deleteDanger={!org.isDeleted}
            error={error}
        >
            <div className="edit-form">
                <label>Name
                    <input type="text" value={name} onChange={(e) => setName(e.target.value)} />
                </label>
                <label>Default locale
                    <input type="text" value={locale} onChange={(e) => setLocale(e.target.value)} />
                </label>
            </div>
        </DetailView>
    );
}

function OrganizationCreate({ onBack, onCreated }) {
    const api = useApi();
    const [name, setName] = useState("");
    const [locale, setLocale] = useState("");
    const [error, setError] = useState(null);

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        try {
            const res = await api("/admin/organizations", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
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

export default function OrganizationsPage() {
    const { currentAdmin } = useAuth();
    const api = useApi();
    const [orgs, setOrgs] = useState(null);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);

    async function load() {
        await loadResource(api, "/admin/organizations", setError, setOrgs, "Could not load organizations");
    }

    useEffect(() => { load(); }, []);

    if (creating) {
        return (
            <OrganizationCreate
                onBack={() => setCreating(false)}
                onCreated={() => { setCreating(false); load(); }}
            />
        );
    }

    if (selected) {
        return (
            <OrganizationDetail
                org={selected}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); load(); }}
                onDeleted={() => { setSelected(null); load(); }}
            />
        );
    }

    return (
        <PageList
            title="Organizations"
            canAdd={currentAdmin.permissions.includes("CREATE_ORG")}
            onAdd={() => setCreating(true)}
            loading={!orgs && !error}
            error={error}
        >
            {orgs && orgs.map((o) => (
                <div className={"admin-row" + (o.isDeleted ? " deleted" : "")} key={o.id} onClick={() => setSelected(o)}>
                    <span className="name">{o.name}</span>
                    <span className="meta"> — {o.defaultLocale}</span>
                    <span className="deleted-tag">{o.isDeleted ? "Deleted" : ""}</span>
                </div>
            ))}
        </PageList>
    );
}
