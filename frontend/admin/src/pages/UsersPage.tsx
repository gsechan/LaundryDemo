import { useState, useEffect } from "react";
import useApi from "../useApi";
import { loadResource } from "../apiUtils";
import PageList from "../components/PageList";

export default function UsersPage({ orgId, onOrgChange }) {
    const api = useApi();
    const [orgs, setOrgs] = useState(null);
    const [users, setUsers] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadResource(api, "/admin/organizations", setError, setOrgs, "Could not load organizations");
    }, []);

    async function loadUsers() {
        if (!orgId) { setUsers(null); return; }
        await loadResource(api, "/admin/organizations/" + orgId + "/users", setError, setUsers, "Could not load users");
    }

    useEffect(() => { loadUsers(); }, [orgId]);

    return (
        <PageList
            title="Users"
            orgs={orgs ?? []}
            orgId={orgId}
            onOrgChange={onOrgChange}
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
