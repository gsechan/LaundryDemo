import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { saveResource, loadResource, deleteResource } from "../apiUtils";
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

    const handleSave = () => saveResource(api, "PATCH", "/admin/organizations/" + org.id,
        { organization: { name, defaultLocale: locale } }, setError, onSaved, "Could not save organization");

    const handleDelete = () => deleteResource(api, "/admin/organizations/" + org.id, setError, onDeleted, "Could not delete organization");

    const handleUndelete = () => saveResource(api, "PATCH", "/admin/organizations/" + org.id,
        { organization: { isDeleted: false } }, setError, onSaved, "Could not undelete organization");

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
        await saveResource(api, "POST", "/admin/organizations",
            { organization: { name, defaultLocale: locale } }, setError, onCreated, "Could not create organization");
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
