import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

const ITEM_TYPES = ["WASH_AND_FOLD", "DRY_CLEANING", "OTHER"];

function ItemDetail({ orgId, item, onBack, onSaved, onDeleted }) {
    const { currentAdmin } = useAuth();
    const api = useApi();
    const canEdit = currentAdmin.permissions.includes("EDIT_ORG") || currentAdmin.permissions.includes("CREATE_ORG");
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

    async function saveName(n) {
        setError(null);
        try {
            const res = await api(namesUrl + "/" + n.id, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: n.name, locale: n.locale }),
            });
            const body = await res.json();
            if (body.errorType !== "NONE") {
                setError((body.errors && body.errors.join(", ")) || "Could not save name");
            }
        } catch (err) { setError("Could not reach the server"); }
    }

    async function deleteName(id) {
        setError(null);
        try {
            const res = await api(namesUrl + "/" + id, { method: "DELETE" });
            const body = await res.json();
            if (body.errorType === "NONE") { setNames((prev) => prev.filter((n) => n.id !== id)); }
            else { setError((body.errors && body.errors.join(", ")) || "Could not delete name"); }
        } catch (err) { setError("Could not reach the server"); }
    }

    async function addName() {
        setError(null);
        if (!newName || !newLocale) return;
        try {
            const res = await api(namesUrl, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: newName, locale: newLocale }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") {
                setNames((prev) => [...prev, body.data]);
                setNewName("");
                setNewLocale("");
            } else {
                setError((body.errors && body.errors.join(", ")) || "Could not add name");
            }
        } catch (err) { setError("Could not reach the server"); }
    }

    async function handleSave() {
        setError(null);
        try {
            const res = await api("/admin/organizations/" + orgId + "/items/" + item.id, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ item: { price, itemType } }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") { onSaved(); }
            else { setError((body.errors && body.errors.join(", ")) || "Could not save item"); }
        } catch (err) { setError("Could not reach the server"); }
    }

    async function handleDelete() {
        setError(null);
        try {
            const res = await api("/admin/organizations/" + orgId + "/items/" + item.id, { method: "DELETE" });
            const body = await res.json();
            if (body.errorType === "NONE") { onDeleted(); }
            else { setError((body.errors && body.errors.join(", ")) || "Could not delete item"); }
        } catch (err) { setError("Could not reach the server"); }
    }

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
        setError(null);
        const cleanNames = names.filter((n) => n.name && n.locale);
        try {
            const res = await api("/admin/organizations/" + orgId + "/items", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ item: { price, itemType, names: cleanNames } }),
            });
            const body = await res.json();
            if (body.errorType === "NONE") { onCreated(); }
            else { setError((body.errors && body.errors.join(", ")) || "Could not create item"); }
        } catch (err) { setError("Could not reach the server"); }
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
    const { currentAdmin } = useAuth();
    const api = useApi();
    const canEdit = currentAdmin.permissions.includes("EDIT_ORG") || currentAdmin.permissions.includes("CREATE_ORG");
    const [orgs, setOrgs] = useState(null);
    const [orgId, setOrgId] = useState("");
    const [items, setItems] = useState(null);
    const [selected, setSelected] = useState(null);
    const [creating, setCreating] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function loadOrgs() {
            try {
                const res = await api("/admin/organizations");
                const body = await res.json();
                if (body.errorType === "NONE") { setOrgs(body.data); }
                else { setError((body.errors && body.errors[0]) || "Could not load organizations"); }
            } catch (err) { setError("Could not reach the server"); }
        }
        loadOrgs();
    }, []);

    async function loadItems() {
        if (!orgId) { setItems(null); return; }
        setError(null);
        try {
            const res = await api("/admin/organizations/" + orgId + "/items");
            const body = await res.json();
            if (body.errorType === "NONE") { setItems(body.data); }
            else { setError((body.errors && body.errors[0]) || "Could not load items"); }
        } catch (err) { setError("Could not reach the server"); }
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
