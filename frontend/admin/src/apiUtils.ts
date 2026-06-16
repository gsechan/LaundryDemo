export async function deleteResource(
    api: (url: string, options?: RequestInit) => Promise<Response>,
    url: string,
    setError: (msg: string | null) => void,
    onDeleted: () => void,
    errorMsg: string,
) {
    setError(null);
    try {
        const res = await api(url, { method: "DELETE" });
        const body = await res.json();
        if (body.errorType === "NONE") { onDeleted(); }
        else { setError((body.errors && body.errors.join(", ")) || errorMsg); }
    } catch (err) { setError("Could not reach the server"); }
}
