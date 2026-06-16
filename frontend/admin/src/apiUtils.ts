export async function saveResource(
    api: (url: string, options?: RequestInit) => Promise<Response>,
    method: string,
    url: string,
    payload: unknown,
    setError: (msg: string | null) => void,
    onSuccess: (data?: any) => void,
    errorMsg: string,
) {
    setError(null);
    try {
        const res = await api(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        const body = await res.json();
        if (body.errorType === "NONE") { onSuccess(body.data); }
        else { setError((body.errors && body.errors.join(", ")) || errorMsg); }
    } catch (err) { setError("Could not reach the server"); }
}

export async function loadResource(
    api: (url: string, options?: RequestInit) => Promise<Response>,
    url: string,
    setError: (msg: string | null) => void,
    onLoaded: (data: any) => void,
    errorMsg: string,
) {
    setError(null);
    try {
        const res = await api(url);
        const body = await res.json();
        if (body.errorType === "NONE") { onLoaded(body.data); }
        else { setError((body.errors && body.errors[0]) || errorMsg); }
    } catch (err) { setError("Could not reach the server"); }
}

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
