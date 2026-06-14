-- Deleting an admin should remove the auth/role rows that belong solely to that
-- admin. Recreate the FKs referencing admins(id) with ON DELETE CASCADE.

ALTER TABLE public.adminpasswords DROP CONSTRAINT adminpasswords_admin_id_fkey;
ALTER TABLE public.adminpasswords
    ADD CONSTRAINT adminpasswords_admin_id_fkey
    FOREIGN KEY (admin_id) REFERENCES public.admins(id) ON DELETE CASCADE;

ALTER TABLE public.adminsessions DROP CONSTRAINT adminsessions_admin_id_fkey;
ALTER TABLE public.adminsessions
    ADD CONSTRAINT adminsessions_admin_id_fkey
    FOREIGN KEY (admin_id) REFERENCES public.admins(id) ON DELETE CASCADE;

ALTER TABLE public.admin_role_membership DROP CONSTRAINT admin_role_membership_admin_id_fkey;
ALTER TABLE public.admin_role_membership
    ADD CONSTRAINT admin_role_membership_admin_id_fkey
    FOREIGN KEY (admin_id) REFERENCES public.admins(id) ON DELETE CASCADE;
