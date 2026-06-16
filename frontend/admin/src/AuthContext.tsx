import { createContext, useContext } from "react";

interface AuthContextValue {
    token: string;
    currentAdmin: { permissions: string[]; [key: string]: any };
    hasAnyPermission: (...perms: string[]) => boolean;
    hasAllPermissions: (...perms: string[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth(): AuthContextValue {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}

export function makeAuthValue(token: string, currentAdmin: { permissions: string[]; [key: string]: any }): AuthContextValue {
    const perms = currentAdmin.permissions;
    return {
        token,
        currentAdmin,
        hasAnyPermission: (...list) => list.length === 0 || list.some((p) => perms.includes(p)),
        hasAllPermissions: (...list) => list.length === 0 || list.every((p) => perms.includes(p)),
    };
}

export default AuthContext;
