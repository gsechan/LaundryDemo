-- Admin role / permission model. Permissions are developer-defined (the
-- AdminPermissions enum); roles group permissions and are joined to admins
-- many-to-many. The permission column is constrained to the enum's values.

CREATE TABLE public.admin_roles (
    id uuid NOT NULL PRIMARY KEY,
    name character varying(255) NOT NULL UNIQUE
);

CREATE TABLE public.admin_role_permissions (
    id uuid NOT NULL PRIMARY KEY,
    role_id uuid NOT NULL REFERENCES public.admin_roles(id),
    permission character varying(255) NOT NULL,
    CONSTRAINT admin_role_permissions_permission_check
        CHECK (permission IN ('CREATE_ORG', 'DELETE_ORG', 'EDIT_ORG', 'CREATE_ADMIN', 'DELETE_ADMIN')),
    CONSTRAINT admin_role_permissions_role_permission_unique UNIQUE (role_id, permission)
);

CREATE INDEX idx_admin_role_permissions_role_id ON public.admin_role_permissions (role_id);

CREATE TABLE public.admin_role_membership (
    id uuid NOT NULL PRIMARY KEY,
    admin_id uuid NOT NULL REFERENCES public.admins(id),
    role_id uuid NOT NULL REFERENCES public.admin_roles(id),
    CONSTRAINT admin_role_membership_admin_role_unique UNIQUE (admin_id, role_id)
);

CREATE INDEX idx_admin_role_membership_admin_id ON public.admin_role_membership (admin_id);
CREATE INDEX idx_admin_role_membership_role_id ON public.admin_role_membership (role_id);

-- Seed a "Root" role holding every permission, assigned to the seeded admin.
INSERT INTO public.admin_roles (id, name)
VALUES ('b0000000-0000-0000-0000-000000000001', 'Root');

INSERT INTO public.admin_role_permissions (id, role_id, permission) VALUES
    ('b0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'CREATE_ORG'),
    ('b0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001', 'DELETE_ORG'),
    ('b0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000001', 'EDIT_ORG'),
    ('b0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000001', 'CREATE_ADMIN'),
    ('b0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000001', 'DELETE_ADMIN');

INSERT INTO public.admin_role_membership (id, admin_id, role_id)
VALUES ('b0000000-0000-0000-0000-000000000007', 'a0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001');
