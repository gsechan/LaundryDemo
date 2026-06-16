import { useAuth } from "./AuthContext";

export default function useApi() {
    const { token } = useAuth();
    return (url: string, options: RequestInit = {}) => fetch(url, {
        ...options,
        headers: {
            "Authorization": "Bearer " + token,
            ...options.headers,
        },
    });
}
