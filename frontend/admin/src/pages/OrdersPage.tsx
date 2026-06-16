import { useState, useEffect } from "react";
import { useAuth } from "../AuthContext";
import useApi from "../useApi";
import { saveResource, loadResource, deleteResource } from "../apiUtils";
import PageList from "../components/PageList";
import DetailView from "../components/DetailView";

const ORDER_STATES = [
    "SUBMITTED", "PICKUP_IN_PROGRESS", "PICKED_UP", "CLEANING",
    "AWAITING_DROP_OFF", "DROPPING_OFF", "COMPLETED",
];
const ADDR_FIELDS = ["street1", "street2", "city", "state", "country", "postcode"];

function msToInput(ms) {
    if (ms == null) return "";
    const off = new Date(ms).getTimezoneOffset() * 60000;
    return new Date(ms - off).toISOString().slice(0, 16);
}
function inputToMs(s) { return s ? new Date(s).getTime() : null; }
function msToLabel(ms) { return ms == null ? "—" : new Date(ms).toLocaleString(); }

function OrderDetail({ order, orgId, onBack, onSaved, onDeleted }) {
    const { hasAnyPermission } = useAuth();
    const api = useApi();
    const canEdit = hasAnyPermission("EDIT_ORG", "CREATE_ORG");
    const [state, setState] = useState(order.state || "SUBMITTED");
    const [pickupAt, setPickupAt] = useState(msToInput(order.scheduledPickup));
    const [dropoffAt, setDropoffAt] = useState(msToInput(order.scheduledDropoff));
    const [pickup, setPickup] = useState({ ...order.pickupAddress });
    const [dropoff, setDropoff] = useState({ ...order.dropoffAddress });
    const [lineQtys, setLineQtys] = useState(
        Object.fromEntries(order.lines.map((l) => [l.id, l.quantity || ""]))
    );
    const [error, setError] = useState(null);

    const orderUrl = `/admin/organizations/${orgId}/orders/${order.id}`;

    const handleSave = () => saveResource(api, "PATCH", orderUrl, {
        order: {
            state,
            scheduledPickup: inputToMs(pickupAt),
            scheduledDropoff: inputToMs(dropoffAt),
            pickupAddress: pickup,
            dropoffAddress: dropoff,
            lines: order.lines.map((l) => ({
                id: l.id,
                quantity: lineQtys[l.id] === "" ? null : lineQtys[l.id],
            })),
        },
    }, setError, onSaved, "Could not save order");

    const handleDelete = () => deleteResource(api, orderUrl, setError, onDeleted, "Could not delete order");

    function addressEditor(label, addr, setAddr) {
        return (
            <div>
                <h3>{label}</h3>
                <div className="edit-form">
                    {ADDR_FIELDS.map((f) => (
                        <label key={f}>{f}
                            <input type="text" value={addr[f] || ""}
                                   onChange={(e) => setAddr((prev) => ({ ...prev, [f]: e.target.value }))} />
                        </label>
                    ))}
                </div>
            </div>
        );
    }

    return (
        <DetailView
            title="Edit Order"
            backLabel="Back to orders"
            onBack={onBack}
            canSave={canEdit}
            onSave={handleSave}
            canDelete={canEdit}
            onDelete={handleDelete}
            error={error}
        >
            <div className="edit-form">
                <label>State
                    <select value={state} onChange={(e) => setState(e.target.value)}>
                        {ORDER_STATES.map((s) => <option key={s} value={s}>{s}</option>)}
                    </select>
                </label>
                <label>Scheduled pickup
                    <input type="datetime-local" value={pickupAt} onChange={(e) => setPickupAt(e.target.value)} />
                </label>
                <label>Scheduled dropoff
                    <input type="datetime-local" value={dropoffAt} onChange={(e) => setDropoffAt(e.target.value)} />
                </label>
            </div>
            {addressEditor("Pickup address", pickup, setPickup)}
            {addressEditor("Dropoff address", dropoff, setDropoff)}
            <h3>Lines</h3>
            {order.lines.map((l) => (
                <div className="name-row" key={l.id}>
                    <span style={{ minWidth: "160px" }}>{l.name || "(item)"} ({l.itemType})</span>
                    <span>@ {l.pricePerUnit}</span>
                    <input type="text" style={{ width: "70px" }} value={lineQtys[l.id]}
                           onChange={(e) => setLineQtys((prev) => ({ ...prev, [l.id]: e.target.value }))} />
                    <span>= {l.totalCost || "—"}</span>
                </div>
            ))}
        </DetailView>
    );
}

export default function OrdersPage({ orgId, onOrgChange }) {
    const api = useApi();
    const [orgs, setOrgs] = useState(null);
    const [orders, setOrders] = useState(null);
    const [selected, setSelected] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadResource(api, "/admin/organizations", setError, setOrgs, "Could not load organizations");
    }, []);

    async function loadOrders() {
        if (!orgId) { setOrders(null); return; }
        await loadResource(api, `/admin/organizations/${orgId}/orders`, setError, setOrders, "Could not load orders");
    }

    useEffect(() => { setSelected(null); loadOrders(); }, [orgId]);

    if (orgId && selected) {
        return (
            <OrderDetail
                order={selected}
                orgId={orgId}
                onBack={() => setSelected(null)}
                onSaved={() => { setSelected(null); loadOrders(); }}
                onDeleted={() => { setSelected(null); loadOrders(); }}
            />
        );
    }

    return (
        <PageList
            title="Orders"
            orgs={orgs ?? []}
            orgId={orgId}
            onOrgChange={onOrgChange}
            loading={!!orgId && !orders && !error}
            error={error}
        >
            {orders && orders.map((o) => (
                <div className="admin-row" key={o.id} onClick={() => setSelected(o)}>
                    <span className="name">{o.state}</span>
                    <span className="meta"> — pickup {msToLabel(o.scheduledPickup)} — {o.lines.length} item(s)</span>
                </div>
            ))}
        </PageList>
    );
}
