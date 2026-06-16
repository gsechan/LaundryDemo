CREATE TABLE public.employee_roles (
    id uuid NOT NULL PRIMARY KEY,
    name character varying(255) NOT NULL,
    organization_id uuid NOT NULL REFERENCES public.organizations(id),
    CONSTRAINT employee_roles_name_org_unique UNIQUE (name, organization_id)
);

CREATE INDEX idx_employee_roles_organization_id ON public.employee_roles (organization_id);

CREATE TABLE public.employee_role_permissions (
    id uuid NOT NULL PRIMARY KEY,
    role_id uuid NOT NULL REFERENCES public.employee_roles(id) ON DELETE CASCADE,
    organization_id uuid NOT NULL REFERENCES public.organizations(id),
    permission character varying(255) NOT NULL,
    CONSTRAINT employee_role_permissions_permission_check
        CHECK (permission IN ('CREATE_ITEM', 'DELETE_ITEM', 'CREATE_LOCATION', 'DELETE_LOCATION')),
    CONSTRAINT employee_role_permissions_role_permission_unique UNIQUE (role_id, permission)
);

CREATE INDEX idx_employee_role_permissions_role_id ON public.employee_role_permissions (role_id);
CREATE INDEX idx_employee_role_permissions_organization_id ON public.employee_role_permissions (organization_id);

CREATE TABLE public.employee_role_membership (
    id uuid NOT NULL PRIMARY KEY,
    employee_id uuid NOT NULL REFERENCES public.employees(id),
    role_id uuid NOT NULL REFERENCES public.employee_roles(id) ON DELETE CASCADE,
    location_id uuid REFERENCES public.locations(id),
    CONSTRAINT employee_role_membership_employee_role_unique UNIQUE (employee_id, role_id)
);

CREATE INDEX idx_employee_role_membership_employee_id ON public.employee_role_membership (employee_id);
CREATE INDEX idx_employee_role_membership_role_id ON public.employee_role_membership (role_id);
CREATE INDEX idx_employee_role_membership_location_id ON public.employee_role_membership (location_id);
