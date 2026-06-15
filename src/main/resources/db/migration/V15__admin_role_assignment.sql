-- Add the ASSIGN_ADMIN_ROLES permission, widen the permission CHECK to include
-- it, grant it to Root, and add a "Customer Service" role. Also cascade role
-- deletes to their permission/membership rows so a role can be deleted.

ALTER TABLE public.admin_role_permissions DROP CONSTRAINT admin_role_permissions_permission_check;
ALTER TABLE public.admin_role_permissions
    ADD CONSTRAINT admin_role_permissions_permission_check
    CHECK (permission IN ('CREATE_ORG', 'DELETE_ORG', 'EDIT_ORG', 'CREATE_ADMIN', 'DELETE_ADMIN', 'ASSIGN_ADMIN_ROLES'));

ALTER TABLE public.admin_role_permissions DROP CONSTRAINT admin_role_permissions_role_id_fkey;
ALTER TABLE public.admin_role_permissions
    ADD CONSTRAINT admin_role_permissions_role_id_fkey
    FOREIGN KEY (role_id) REFERENCES public.admin_roles(id) ON DELETE CASCADE;

ALTER TABLE public.admin_role_membership DROP CONSTRAINT admin_role_membership_role_id_fkey;
ALTER TABLE public.admin_role_membership
    ADD CONSTRAINT admin_role_membership_role_id_fkey
    FOREIGN KEY (role_id) REFERENCES public.admin_roles(id) ON DELETE CASCADE;

-- Grant ASSIGN_ADMIN_ROLES to the existing Root role.
INSERT INTO public.admin_role_permissions (id, role_id, permission)
VALUES ('b0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000001', 'ASSIGN_ADMIN_ROLES');

-- Customer Service role: CREATE_ORG + EDIT_ORG only.
INSERT INTO public.admin_roles (id, name)
VALUES ('c0000000-0000-0000-0000-000000000001', 'Customer Service');

INSERT INTO public.admin_role_permissions (id, role_id, permission) VALUES
    ('c0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', 'CREATE_ORG'),
    ('c0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001', 'EDIT_ORG');
