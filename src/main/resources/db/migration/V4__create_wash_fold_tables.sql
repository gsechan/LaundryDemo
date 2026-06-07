CREATE TABLE public.wash_fold_prices (
    id uuid NOT NULL,
    avg_weight numeric(10,2) NOT NULL,
    organization_id uuid NOT NULL UNIQUE REFERENCES public.organizations(id),
    price numeric(10,2) NOT NULL
);
