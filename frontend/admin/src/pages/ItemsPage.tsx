import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { saveResource, loadResource, deleteResource } from "../apiUtils";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

const ITEM_TYPES = ["WASH_AND_FOLD", "DRY_CLEANING", "OTHER"];

function ItemDetail({ orgId, item, onBack, onSaved, onDeleted }) {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG", "CREATE_ORG");
    const [price, setPrice] = useState(item.price || "");
    const [itemType, setItemType] = useState(item.itemType || "DRY_CLEANING");
    const [names, setNames] = useState(item.names || []);
    const [newName, setNewName] = useState("");
    const [newLocale, setNewLocale] = useState("");
    const [error, setError] = useState(null);

    const namesUrl = "/admin/organizations/" + orgId + "/items/" + item.id + "/names";

    function updateNameField(id, field, value) {
        setNames((prev) => prev.map((n) => n.id === id ? { ...n, [field]: value } : n));
    }

    const saveName = (n) => saveResource(api, "PATCH", namesUrl + "/" + n.id,
        { name: n.name, locale: n.locale }, setError, () => {}, "Could not save name");

    const deleteName = (id) => deleteResource(api, namesUrl + "/" + id, setError,
        () => setNames((prev) => prev.filter((n) => n.id !== id)), "Could not delete name");

    async function addName() {
        if (!newName || !newLocale) return;
        await saveResource(api, "POST", namesUrl, { name: newName, locale: newLocale }, setError,
            (data) => { setNames((prev) => [...prev, data]); setNewName(""); setNewLocale(""); },
            "Could not add name");
    }

    const handleSave = () => saveResource(api, "PATCH", "/admin/organizations/" + orgId + "/items/" + item.id,
        { item: { price, itemType } }, setError, onSaved, "Could not save item");

    const handleDelete = () => deleteResource(api, "/admin/organizations/" + orgId + "/items/" + item.id, setError, onDeleted, "Could not delete item");

    return (
        <DetailView
            title="Edit Item"
            backLabel="Back to items"
            onBack={onBack}
            canSave={canEdit}
            onSave={handleSave}
            canDelete={canEdit}
            onDelete={handleDelete}
            error={error}
        >
            <div className="edit-form">
                <label>Price
                    <input type="text" value={price} onChange={(e) => setPrice(e.target.value)} />
                </label>
                <label>Type
                    <select value={itemType} onChange={(e) => setItemType(e.target.value)}>
                        {ITEM_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                    </select>
                </label>
            </div>
            <h3>Names</h3>
            {canEdit ? (
                <div>
                    {names.map((n) => (
                        <div className="name-row" key={n.id}>
                            <input type="text" value={n.name || ""}
                                   onChange={(e) => updateNameField(n.id, "name", e.target.value)} />
                            <input type="text" value={n.locale || ""}
                                   onChange={(e) => updateNameField(n.id, "locale", e.target.value)} />
                            <button type="button" onClick={() => saveName(n)}>Save</button>
                            <button type="button" onClick={() => deleteName(n.id)}>✕</button>
                        </div>
                    ))}
                    <div className="name-row">
                        <input type="text" placeholder="Name" value={newName}
                               onChange={(e) => setNewName(e.target.value)} />
                        <input type="text" placeholder="Locale" value={newLocale}
                               onChange={(e) => setNewLocale(e.target.value)} />
                        <button type="button" onClick={addName}>Add</button>
                    </div>
                </div>
            ) : (
                names.map((n, i) => (
                    <div className="detail-field" key={i}>{n.name} <span className="label">({n.locale})</span></div>
                ))
            )}
        </DetailView>
    );
}

function ItemCreate({ orgId, onBack, onCreated }) {
    const api = useApi();
    const [price, setPrice] = useState("");
    const [itemType, setItemType] = useState("DRY_CLEANING");
    const [names, setNames] = useState([{ name: "", locale: "" }]);
    const [error, setError] = useState(null);

    function updateName(i, field, value) {
        setNames((prev) => prev.map((n, idx) => idx === i ? { ...n, [field]: value } : n));
    }
    function addName() { setNames((prev) => [...prev, { name: "", locale: "" }]); }
    function removeName(i) { setNames((prev) => prev.filter((_, idx) => idx !== i)); }

    async function handleSubmit(e) {
        e.preventDefault();
        const cleanNames = names.filter((n) => n.name && n.locale);
        await saveResource(api, "POST", "/admin/organizations/" + orgId + "/items",
            { item: { price, itemType, names: cleanNames } }, setError, onCreated, "Could not create item");
    }

    return (
        <div>
            <button className="back-link" onClick={onBack}>← Back to items</button>
            <h1>Add Item</h1>
            <form onSubmit={handleSubmit}>
                <input type="text" placeholder="Price" value={price}
                       onChange={(e) => setPrice(e.target.value)} />
                <select value={itemType} onChange={(e) => setItemType(e.target.value)}>
                    {ITEM_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
                <h3>Names</h3>
                {names.map((n, i) => (
                    <div className="name-row" key={i}>
                        <input type="text" placeholder="Name" value={n.name}
                               onChange={(e) => updateName(i, "name", e.target.value)} />
                        <input type="text" placeholder="Locale" value={n.locale}
                               onChange={(e) => updateName(i, "locale", e.target.value)} />
                        <button type="button" onClick={() => removeName(i)}>✕</button>
                    </div>
                ))}
                <button type="button" onClick={addName}>Add name</button>
                <button type="submit">Create</button>
                {error && <div className="error">{error}</div>}
            </form>
        </div>
    );
}

export default function ItemsPage() {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG", "CREATE_ORG");
    const [orgs, setOrgs] = useState(null);
    const [orgId, setOrgId] = useState("");
    const [items, setItems] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadResource(api, "/admin/organizations", setError, setOrgs, "Could not load organizations");
    }, []);

    async function loadItems() {
        if (!orgId) { setItems(null); return; }
        await loadResource(api, "/admin/organizations/" + orgId + "/items", setError, setItems, "Could not load items");
    }

    useEffect(() => { setSelected(null); setCreating(false); loadItems(); }, [orgId]);

    if (orgId && creating) {
        return (
            <ItemCreate
                orgId={orgId}
                onBack={() => setCreating(false)}
                onCreated={() => { setCreating(false); loadItems(); }}
            />
        );
    }

    if (orgId && selected) {
        return (
            <ItemDetail
                orgId={orgId}
                item={selected}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); loadItems(); }}
                onDeleted={() => { setSelected(null); loadItems(); }}
            />
        );
    }

    return (
        <PageList
            title="Items"
            orgs={orgs ?? []}
            orgId={orgId}
            onOrgChange={setOrgId}
            canAdd={!!orgId && canEdit}
            onAdd={() => setCreating(true)}
            loading={!!orgId && !items && !error}
            error={error}
        >
            {items && items.map((it) => (
                <div className="admin-row" key={it.id} onClick={() => setSelected(it)}>
                    <span className="name">{it.names.map((n) => n.name).join(" / ") || "(no name)"}</span>
                    <span className="meta"> — {it.itemType} — {it.price}</span>
                </div>
            ))}
        </PageList>
    );
}
