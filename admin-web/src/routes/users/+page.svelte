<script lang="ts">
	import { supabase } from '$lib/supabase';
	import { auth, type Profile, type UserRole } from '$lib/auth.svelte';

	type Store = { id: string; name: string };

	let users = $state<(Profile & { stores: { name: string } | null })[]>([]);
	let stores = $state<Store[]>([]);
	let loadingData = $state(true);
	let errorMsg = $state('');

	// Form tambah user
	let showForm = $state(false);
	let newEmail = $state('');
	let newPassword = $state('');
	let newName = $state('');
	let newRole = $state<UserRole>('kasir');
	let newStoreId = $state('');
	let creating = $state(false);

	const roleLabels: Record<UserRole, string> = {
		super_admin: 'Super Admin',
		admin: 'Admin',
		kasir: 'Kasir'
	};

	async function loadData() {
		loadingData = true;
		errorMsg = '';
		const [profilesRes, storesRes] = await Promise.all([
			supabase.from('profiles').select('*, stores(name)').order('created_at'),
			supabase.from('stores').select('id, name')
		]);
		if (profilesRes.error) errorMsg = profilesRes.error.message;
		users = (profilesRes.data as typeof users) ?? [];
		stores = storesRes.data ?? [];
		if (!newStoreId && stores.length > 0) {
			newStoreId = auth.isSuperAdmin ? '' : (auth.profile?.store_id ?? '');
		}
		loadingData = false;
	}

	async function createUser(event: SubmitEvent) {
		event.preventDefault();
		errorMsg = '';
		creating = true;
		try {
			const res = await fetch('/api/users', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Bearer ${auth.session?.access_token}`
				},
				body: JSON.stringify({
					email: newEmail,
					password: newPassword,
					full_name: newName,
					role: newRole,
					store_id: newStoreId || null
				})
			});
			const body = await res.json();
			if (!res.ok) throw new Error(body.error ?? 'Gagal membuat user');
			showForm = false;
			newEmail = newPassword = newName = '';
			await loadData();
		} catch (err) {
			errorMsg = err instanceof Error ? err.message : String(err);
		} finally {
			creating = false;
		}
	}

	async function updateRole(user: Profile, role: UserRole) {
		const { error } = await supabase.from('profiles').update({ role }).eq('id', user.id);
		if (error) {
			errorMsg = error.message;
			await loadData();
		}
	}

	async function toggleActive(user: Profile) {
		const { error } = await supabase
			.from('profiles')
			.update({ is_active: !user.is_active })
			.eq('id', user.id);
		if (error) {
			errorMsg = error.message;
			await loadData();
		} else {
			user.is_active = !user.is_active;
		}
	}

	async function removeUser(user: Profile) {
		if (!confirm(`Hapus user ${user.email}? Tindakan ini permanen.`)) return;
		errorMsg = '';
		const res = await fetch(`/api/users?userId=${user.id}`, {
			method: 'DELETE',
			headers: { Authorization: `Bearer ${auth.session?.access_token}` }
		});
		if (!res.ok) {
			const body = await res.json().catch(() => ({}));
			errorMsg = body.error ?? 'Gagal menghapus user';
			return;
		}
		await loadData();
	}

	function canEdit(user: Profile): boolean {
		if (auth.isSuperAdmin) return true;
		return user.role !== 'super_admin' && user.store_id === auth.profile?.store_id;
	}

	const assignableRoles = $derived<UserRole[]>(
		auth.isSuperAdmin ? ['admin', 'kasir'] : ['kasir']
	);
</script>

<svelte:head>
	<title>Kelola Pengguna - AminMart Admin</title>
</svelte:head>

{#if !auth.isAdmin}
	<p class="rounded-lg bg-white p-8 text-center text-slate-500 shadow">
		Akses ditolak. Halaman ini hanya untuk admin &amp; super admin.
	</p>
{:else}
	<div class="mb-4 flex items-center justify-between">
		<h1 class="text-xl font-bold">Kelola Pengguna</h1>
		<button
			onclick={() => (showForm = !showForm)}
			class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
		>
			+ Tambah User
		</button>
	</div>

	{#if errorMsg}
		<p class="mb-3 rounded-lg bg-red-50 px-4 py-2 text-sm text-red-700">{errorMsg}</p>
	{/if}

	{#if showForm}
		<form
			onsubmit={createUser}
			class="mb-6 grid gap-3 rounded-xl bg-white p-5 shadow sm:grid-cols-2"
		>
			<label class="text-sm font-medium">
				Email
				<input
					type="email"
					bind:value={newEmail}
					required
					class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
				/>
			</label>
			<label class="text-sm font-medium">
				Password awal
				<input
					type="text"
					bind:value={newPassword}
					required
					minlength={6}
					class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
				/>
			</label>
			<label class="text-sm font-medium">
				Nama lengkap
				<input
					type="text"
					bind:value={newName}
					class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
				/>
			</label>
			<label class="text-sm font-medium">
				Role
				<select
					bind:value={newRole}
					class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
				>
					{#each assignableRoles as r}
						<option value={r}>{roleLabels[r]}</option>
					{/each}
				</select>
			</label>
			{#if auth.isSuperAdmin && newRole !== 'super_admin'}
				<label class="text-sm font-medium">
					Toko
					<select
						bind:value={newStoreId}
						required
						class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
					>
						<option value="" disabled>Pilih toko</option>
						{#each stores as s}
							<option value={s.id}>{s.name}</option>
						{/each}
					</select>
				</label>
			{/if}
			<div class="sm:col-span-2">
				<button
					type="submit"
					disabled={creating}
					class="rounded-lg bg-emerald-600 px-4 py-2 text-sm text-white hover:bg-emerald-700 disabled:opacity-50"
				>
					{creating ? 'Menyimpan...' : 'Simpan User'}
				</button>
			</div>
		</form>
	{/if}

	{#if loadingData}
		<p class="text-slate-500">Memuat...</p>
	{:else}
		<div class="overflow-x-auto rounded-xl bg-white shadow">
			<table class="w-full text-left text-sm">
				<thead class="border-b bg-slate-50 text-xs uppercase text-slate-500">
					<tr>
						<th class="px-4 py-3">Email</th>
						<th class="px-4 py-3">Nama</th>
						<th class="px-4 py-3">Toko</th>
						<th class="px-4 py-3">Role</th>
						<th class="px-4 py-3">Status</th>
						<th class="px-4 py-3"></th>
					</tr>
				</thead>
				<tbody>
					{#each users as u (u.id)}
						<tr class="border-b last:border-none">
							<td class="px-4 py-3 font-medium">{u.email}</td>
							<td class="px-4 py-3">{u.full_name || '-'}</td>
							<td class="px-4 py-3">{u.stores?.name ?? '-'}</td>
							<td class="px-4 py-3">
								{#if canEdit(u)}
									<select
										value={u.role}
										onchange={(e) => updateRole(u, e.currentTarget.value as UserRole)}
										class="rounded border border-slate-300 px-2 py-1"
									>
										{#if auth.isSuperAdmin}
											<option value="super_admin">{roleLabels.super_admin}</option>
										{/if}
										<option value="admin">{roleLabels.admin}</option>
										<option value="kasir">{roleLabels.kasir}</option>
									</select>
								{:else}
									{roleLabels[u.role]}
								{/if}
							</td>
							<td class="px-4 py-3">
								<span
									class="rounded px-2 py-0.5 text-xs {u.is_active
										? 'bg-emerald-100 text-emerald-800'
										: 'bg-red-100 text-red-800'}"
								>
									{u.is_active ? 'Aktif' : 'Nonaktif'}
								</span>
							</td>
							<td class="space-x-2 px-4 py-3 text-right whitespace-nowrap">
								<button onclick={() => toggleActive(u)} class="text-blue-600 hover:underline">
									{u.is_active ? 'Nonaktifkan' : 'Aktifkan'}
								</button>
								{#if canEdit(u)}
									<button onclick={() => removeUser(u)} class="text-red-600 hover:underline">
										Hapus
									</button>
								{/if}
							</td>
						</tr>
					{/each}
				</tbody>
			</table>
		</div>
	{/if}
{/if}
