CREATE TABLE public.locations (
    id uuid NOT NULL PRIMARY KEY,
    name character varying(255) NOT NULL,
    street1 character varying(255) NOT NULL,
    street2 character varying(255),
    city character varying(255) NOT NULL,
    state character varying(255) NOT NULL,
    country character varying(255) NOT NULL,
    postcode character varying(255) NOT NULL,
    organization_id uuid NOT NULL REFERENCES public.organizations(id)
);

CREATE INDEX idx_locations_organization_id ON public.locations (organization_id);
