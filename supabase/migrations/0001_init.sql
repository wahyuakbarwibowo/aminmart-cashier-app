-- ============================================================
-- AminMart Kasir v2 - Skema awal SaaS (multi-tenant)
-- Jalankan di Supabase Dashboard > SQL Editor (urut nomor file)
-- ============================================================

-- Role pengguna: super_admin = pemilik SaaS, admin = pemilik toko, kasir = operasional toko
create type public.user_role as enum ('super_admin', 'admin', 'kasir');

-- Toko (tenant)
create table public.stores (
    id uuid primary key default gen_random_uuid(),
    name text not null,
    address text,
    phone text,
    is_active boolean not null default true,
    created_at timestamptz not null default now()
);

-- Profil user; id = auth.users.id
create table public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    store_id uuid references public.stores(id) on delete set null,
    email text not null,
    full_name text not null default '',
    role public.user_role not null default 'kasir',
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index profiles_store_id_idx on public.profiles (store_id);

-- updated_at otomatis
create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
    new.updated_at = now();
    return new;
end $$;

create trigger profiles_set_updated_at
    before update on public.profiles
    for each row execute function public.set_updated_at();

-- Trigger: setiap user baru di auth.users otomatis dapat profil.
-- store_id/role/full_name bisa dikirim lewat user metadata saat signup/createUser:
--   raw_user_meta_data: { "store_id": "...", "role": "admin", "full_name": "Budi" }
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
declare
    meta jsonb := new.raw_user_meta_data;
    requested_role public.user_role;
    valid_store uuid;
begin
    -- Role dari metadata divalidasi: hanya super_admin yang boleh menetapkan selain kasir
    if coalesce(meta->>'role', '') in ('super_admin', 'admin') then
        if exists (
            select 1 from public.profiles p
            where p.id = auth.uid() and p.role = 'super_admin'
        ) then
            requested_role := (meta->>'role')::public.user_role;
        else
            requested_role := 'kasir';
        end if;
    else
        requested_role := 'kasir';
    end if;

    -- Validasi store_id bila ada
    if coalesce(meta->>'store_id', '') <> '' then
        select s.id into valid_store from public.stores s where s.id = (meta->>'store_id')::uuid;
    end if;

    insert into public.profiles (id, email, full_name, role, store_id)
    values (
        new.id,
        new.email,
        coalesce(meta->>'full_name', ''),
        requested_role,
        valid_store
    );
    return new;
end $$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- ============================================================
-- Helper untuk RLS
-- ============================================================
create or replace function public.my_profile()
returns public.profiles language sql stable security definer set search_path = public as $$
    select * from public.profiles where id = auth.uid()
$$;

create or replace function public.is_super_admin()
returns boolean language sql stable security definer set search_path = public as $$
    select exists(select 1 from public.profiles where id = auth.uid() and role = 'super_admin')
$$;

create or replace function public.is_store_member(target_store uuid)
returns boolean language sql stable security definer set search_path = public as $$
    select exists(
        select 1 from public.profiles
        where id = auth.uid() and store_id = target_store and store_id is not null
    )
$$;

-- ============================================================
-- RLS
-- ============================================================
alter table public.stores enable row level security;
alter table public.profiles enable row level security;

--- stores ---
create policy stores_select on public.stores for select to authenticated
    using (public.is_super_admin() or public.is_store_member(id));

create policy stores_insert on public.stores for insert to authenticated
    with check (public.is_super_admin());

create policy stores_update on public.stores for update to authenticated
    using (public.is_super_admin())
    with check (public.is_super_admin());

create policy stores_delete on public.stores for delete to authenticated
    using (public.is_super_admin());

--- profiles ---
-- Lihat: super_admin semua; admin/kasir hanya se-toko; tambahan lihat diri sendiri
create policy profiles_select on public.profiles for select to authenticated
    using (
        public.is_super_admin()
        or id = auth.uid()
        or (store_id is not null and public.is_store_member(store_id))
    );

-- Update: super_admin semua; admin se-toko kecuali super_admin; tiap user boleh ubah nama sendiri
create policy profiles_update on public.profiles for update to authenticated
    using (
        public.is_super_admin()
        or (
            id = auth.uid()
            and role <> 'super_admin'
            and store_id = (select store_id from public.profiles where id = auth.uid())
        )
        or (
            store_id is not null
            and public.is_store_member(store_id)
            and exists(select 1 from public.profiles where id = auth.uid() and role = 'admin')
            and role in ('admin', 'kasir')
        )
    )
    with check (
        -- larang eskalasi: tidak boleh mengubah role jadi super_admin, dan admin tidak boleh
        -- memindahkan user lintas toko
        role <> 'super_admin'
        and (
            public.is_super_admin()
            or store_id = (select store_id from public.profiles where id = auth.uid())
        )
    );

-- Insert langsung hanya super_admin (biasanya lewat trigger handle_new_user)
create policy profiles_insert on public.profiles for insert to authenticated
    with check (public.is_super_admin());

create policy profiles_delete on public.profiles for delete to authenticated
    using (
        public.is_super_admin()
        or (
            store_id is not null
            and public.is_store_member(store_id)
            and exists(select 1 from public.profiles where id = auth.uid() and role = 'admin')
            and role in ('admin', 'kasir')
        )
    );
