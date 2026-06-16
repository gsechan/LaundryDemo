const PAGES = [
    { key: "admins", label: "Admins" },
    { key: "roles", label: "Roles" },
    { key: "organizations", label: "Organizations" },
    { key: "locations", label: "Locations" },
    { key: "users", label: "Users" },
    { key: "employees", label: "Employees" },
    { key: "orders", label: "Orders" },
    { key: "items", label: "Items" },
];


function pageVisible(key, perms) {
    const has = (p) => perms.includes(p);
    if (key === "admins") return has("CREATE_ADMIN") || has("DELETE_ADMIN") || has("ASSIGN_ADMIN_ROLES");
    if (key === "organizations") return has("CREATE_ORG") || has("DELETE_ORG") || has("EDIT_ORG");
    if (key === "locations") return has("CREATE_ORG") || has("DELETE_ORG") || has("EDIT_ORG");
    if (key === "items") return has("CREATE_ORG") || has("DELETE_ORG") || has("EDIT_ORG");
    if (key === "orders") return has("CREATE_ORG") || has("DELETE_ORG") || has("EDIT_ORG");
    if (key === "users") return has("CREATE_ORG") || has("DELETE_ORG") || has("EDIT_ORG");
    if (key === "employees") return has("EDIT_ORG");
    if (key === "roles") return has("ASSIGN_ADMIN_ROLES");
    return true;
}

export function getInitialPage(perms) {
    const allVisible = [
        ...PAGES.filter((p) => pageVisible(p.key, perms)),
    ];
    return allVisible.length ? allVisible[0].key : null;
}

export default function Navbar({ perms, page, onNavigate }) {
    const visiblePages = PAGES.filter((p) => pageVisible(p.key, perms));

    return (
        <nav className="navbar">
            {visiblePages.map((p) => (
                <button
                    key={p.key}
                    className={"nav-item" + (page === p.key ? " active" : "")}
                    onClick={() => onNavigate(p.key)}
                >
                    {p.label}
                </button>
            ))}
        </nav>
    );
}
