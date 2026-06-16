interface DetailViewProps {
    title: string;
    backLabel: string;
    onBack: () => void;
    canSave?: boolean;
    onSave?: () => void;
    canDelete?: boolean;
    onDelete?: () => void;
    deleteLabel?: string;
    deleteDanger?: boolean;
    error?: string | null;
    children?: React.ReactNode;
}

export default function DetailView({
    title,
    backLabel,
    onBack,
    canSave,
    onSave,
    canDelete,
    onDelete,
    deleteLabel = "Delete",
    deleteDanger = true,
    error,
    children,
}: DetailViewProps) {
    return (
        <div>
            <button className="back-link" onClick={onBack}>← {backLabel}</button>
            <div className="detail-header">
                <h1>{title}</h1>
                {canSave && <button onClick={onSave}>Save</button>}
            </div>
            {children}
            {canDelete && (
                <div>
                    <button className={deleteDanger ? "danger" : ""} onClick={onDelete}>
                        {deleteLabel}
                    </button>
                </div>
            )}
            {error && <div className="error">{error}</div>}
        </div>
    );
}
