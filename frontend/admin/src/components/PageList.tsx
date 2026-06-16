interface Org {
    id: string;
    name: string;
}

interface PageListProps {
    title: string;
    orgs?: Org[];
    orgId?: string;
    onOrgChange?: (orgId: string) => void;
    canAdd?: boolean;
    onAdd?: () => void;
    loading?: boolean;
    error?: string | null;
    children?: React.ReactNode;
}

export default function PageList({
    title,
    orgs,
    orgId,
    onOrgChange,
    canAdd,
    onAdd,
    loading,
    error,
    children,
}: PageListProps) {
    return (
        <div>
            <h1>{title}</h1>
            {orgs !== undefined && (
                <label>Organization{" "}
                    <select value={orgId ?? ""} onChange={(e) => onOrgChange?.(e.target.value)}>
                        <option value="">Select…</option>
                        {orgs.map((o) => <option key={o.id} value={o.id}>{o.name}</option>)}
                    </select>
                </label>
            )}
            {canAdd && (
                <div style={{ marginTop: "12px", marginBottom: "12px" }}>
                    <button onClick={onAdd}>Add New</button>
                </div>
            )}
            {error && <div className="error">{error}</div>}
            {loading && <div>Loading…</div>}
            {children}
        </div>
    );
}
