import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { saveResource, loadResource, deleteResource } from "../apiUtils";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

const ADDR_FIELDS = ["street1", "street2", "city", "state", "country", "postcode"];

function LocationDetail({ orgId, location, onBack, onSaved, onDeleted }) {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG", "CREATE_ORG");
    const [name, setName] = useState(location.name || "");
    const [addr, setAddr] = useState({ ...location.address });
    const [error, setError] = useState(null);

    const handleSave = () => saveResource(
        api, "PATCH",
        `/admin/organizations/${orgId}/locations/${location.id}`,
        { location: { name, ...addr } },
        setError, onSaved, "Could not save location",
    );

    const handleDelete = () => deleteResource(
        api,
        `/admin/organizations/${orgId}/locations/${location.id}`,
        setError, onDeleted, "Could not delete location",
    );

    return (
        <DetailView
            title="Edit Location"
            backLabel="Back to locations"
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
                {ADDR_FIELDS.map((f) => (
                    <label key={f}>{f}
                        <input type="text" value={addr[f] || ""}
                               onChange={(e) => setAddr((prev) => ({ ...prev, [f]: e.target.value }))} />
                    </label>
                ))}
            </div>
        </DetailView>
    );
}

function LocationCreate({ orgId, onBack, onCreated }) {
    const api = useApi();
    const [name, setName] = useState("");
    const [addr, setAddr] = useState({ street1: "", street2: "", city: "", state: "", country: "", postcode: "" });
    const [error, setError] = useState(null);

    async function handleSubmit(e) {
        e.preventDefault();
        await saveResource(
            api, "POST",
            `/admin/organizations/${orgId}/locations`,
            { location: { name, ...addr } },
            setError, onCreated, "Could not create location",
        );
    }

    return (
        <div>
            <button className="back-link" onClick={onBack}>← Back to locations</button>
            <h1>Add Location</h1>
            <form onSubmit={handleSubmit}>
                <div className="edit-form">
                    <label>Name
                        <input type="text" value={name} onChange={(e) => setName(e.target.value)} />
                    </label>
                    {ADDR_FIELDS.map((f) => (
                        <label key={f}>{f}
                            <input type="text" value={addr[f] || ""}
                                   onChange={(e) => setAddr((prev) => ({ ...prev, [f]: e.target.value }))} />
                        </label>
                    ))}
                </div>
                <button type="submit">Create</button>
                {error && <div className="error">{error}</div>}
            </form>
        </div>
    );
}

export default function LocationsPage({ orgId, onOrgChange }) {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG", "CREATE_ORG");
    const [orgs, setOrgs] = useState(null);
    const [locations, setLocations] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadResource(api, "/admin/organizations", setError, setOrgs, "Could not load organizations");
    }, []);

    async function loadLocations() {
        if (!orgId) { setLocations(null); return; }
        await loadResource(api, `/admin/organizations/${orgId}/locations`, setError, setLocations, "Could not load locations");
    }

    useEffect(() => { setSelected(null); setCreating(false); loadLocations(); }, [orgId]);

    if (orgId && creating) {
        return (
            <LocationCreate
                orgId={orgId}
                onBack={() => setCreating(false)}
                onCreated={() => { setCreating(false); loadLocations(); }}
            />
        );
    }

    if (orgId && selected) {
        return (
            <LocationDetail
                orgId={orgId}
                location={selected}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); loadLocations(); }}
                onDeleted={() => { setSelected(null); loadLocations(); }}
            />
        );
    }

    return (
        <PageList
            title="Locations"
            orgs={orgs ?? []}
            orgId={orgId}
            onOrgChange={onOrgChange}
            canAdd={!!orgId && canEdit}
            onAdd={() => setCreating(true)}
            loading={!!orgId && !locations && !error}
            error={error}
        >
            {locations && locations.map((loc) => (
                <div className="admin-row" key={loc.id} onClick={() => setSelected(loc)}>
                    <span className="name">{loc.name}</span>
                    <span className="meta"> — {loc.address.street1}, {loc.address.city}, {loc.address.state}</span>
                </div>
            ))}
        </PageList>
    );
}
