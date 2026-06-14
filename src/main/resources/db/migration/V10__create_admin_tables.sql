-- Super-admins belong to the software provider, not to any organization, so the
-- admins table mirrors users but drops organization_id (and the org-scoped
-- uniqueness) and makes email non-nullable. Admins log in by email, so email is
-- unique (its UNIQUE constraint provides the index); phone is non-unique and
-- unindexed (lookups by phone are rare enough to accept a scan).
-- adminpasswords / adminsessions mirror passwords / sessions.

CREATE TABLE public.admins (
    id uuid NOT NULL PRIMARY KEY,
    email character varying(255) NOT NULL UNIQUE,
    name character varying(255) NOT NULL,
    phone character varying(255) NOT NULL
);

CREATE TABLE public.adminpasswords (
    id uuid NOT NULL PRIMARY KEY,
    hash character varying(255) NOT NULL,
    admin_id uuid NOT NULL REFERENCES public.admins(id)
);

CREATE INDEX idx_adminpassword_admin_id ON public.adminpasswords (admin_id);

CREATE TABLE public.adminsessions (
    id uuid NOT NULL PRIMARY KEY,
    expiration timestamp(6) without time zone NOT NULL,
    token character varying(255) NOT NULL UNIQUE,
    admin_id uuid NOT NULL REFERENCES public.admins(id)
);
