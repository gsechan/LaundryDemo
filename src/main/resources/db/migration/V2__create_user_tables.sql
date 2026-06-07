CREATE TABLE public.users (
    id uuid NOT NULL PRIMARY KEY,
    email character varying(255),
    name character varying(255) NOT NULL,
    phone character varying(255) NOT NULL,
    organization_id uuid NOT NULL REFERENCES public.organizations(id),
    CONSTRAINT users_org_phone_unique UNIQUE (organization_id, phone)
);

CREATE TABLE public.addresses (
    id uuid NOT NULL PRIMARY KEY,
    city character varying(255) NOT NULL,
    country character varying(255) NOT NULL,
    is_default boolean NOT NULL,
    postcode character varying(255) NOT NULL,
    state character varying(255) NOT NULL,
    street1 character varying(255) NOT NULL,
    street2 character varying(255),
    user_id uuid NOT NULL REFERENCES public.users(id)
);

CREATE UNIQUE INDEX idx_one_default_per_user ON public.addresses (user_id) WHERE (is_default = true);
CREATE INDEX idx_addresses_user_id ON public.addresses (user_id);