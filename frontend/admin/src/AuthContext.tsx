import { createContext, useContext } from "react";

interface AuthContextValue {
    token: string;
    currentAdmin: { permissions: string[]; [key: string]: any };
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth(): AuthContextValue {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}

export default AuthContext;
