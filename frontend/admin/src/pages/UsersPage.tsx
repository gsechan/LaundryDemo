import { useState, useEffect } from "react";
import PageList from "../components/PageList";

export default function UsersPage({ token }) {
    const [orgs, setOrgs] = useState(null);
    const [orgId, setOrgId] = useState("");
    const [users, setUsers] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        async function loadOrgs() {
            try {
                const res = await fetch("/admin/organizations", { headers: { "Authorization": "Bearer " + token } });
                const body = await res.json();
                if (body.errorType === "NONE") { setOrgs(body.data); }
                else { setError((body.errors && body.errors[0]) || "Could not load organizations"); }
            } catch (err) { setError("Could not reach the server"); }
        }
        loadOrgs();
    }, [token]);

    async function loadUsers() {
        if (!orgId) { setUsers(null); return; }
        setError(null);
        try {
            const res = await fetch("/admin/organizations/" + orgId + "/users", {
                headers: { "Authorization": "Bearer " + token },
            });
            const body = await res.json();
            if (body.errorType === "NONE") { setUsers(body.data); }
            else { setError((body.errors && body.errors[0]) || "Could not load users"); }
        } catch (err) { setError("Could not reach the server"); }
    }

    useEffect(() => { loadUsers(); }, [orgId]);

    return (
        <PageList
            title="Users"
            orgs={orgs ?? []}
            orgId={orgId}
            onOrgChange={setOrgId}
            loading={!!orgId && !users && !error}
            error={error}
        >
            {users && users.map((u) => (
                <div className="admin-row" key={u.id} style={{ cursor: "default" }}>
                    <span className="name">{u.name}</span>
                    <span className="meta"> — {u.email} — {u.phone}</span>
                </div>
            ))}
        </PageList>
    );
}
