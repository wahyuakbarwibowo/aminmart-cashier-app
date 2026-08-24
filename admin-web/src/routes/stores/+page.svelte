<script lang="ts">
	import { supabase } from '$lib/supabase';
	import { auth } from '$lib/auth.svelte';

	type Store = {
		id: string;
		name: string;
		address: string | null;
		phone: string | null;
		is_active: boolean;
	};

	let stores = $state<Store[]>([]);
	let loadingData = $state(true);
	let errorMsg = $state('');
	let showForm = $state(false);
	let newName = $state('');
	let newAddress = $state('');
	let newPhone = $state('');

	async function loadData() {
		loadingData = true;
		const { data, error } = await supabase.from('stores').select('*').order('created_at');
		if (error) errorMsg = error.message;
		stores = data ?? [];
		loadingData = false;
	}

	async function createStore(event: SubmitEvent) {
		event.preventDefault();
		errorMsg = '';
		const { error } = await supabase
			.from('stores')
			.insert({ name: newName, address: newAddress || null, phone: newPhone || null });
		if (error) {
			errorMsg = error.message;
			return;
		}
		showForm = false;
		newName = newAddress = newPhone = '';
		await loadData();
	}

	async function toggleActive(store: Store) {
		const { error } = await supabase
			.from('stores')
			.update({ is_active: !store.is_active })
			.eq('id', store.id);
		if (error) errorMsg = error.message;
		else store.is_active = !store.is_active;
	}

	async function removeStore(store: Store) {
		if (!confirm(`Hapus toko "${store.name}"? User di toko ini akan kehilangan relasi toko.`)) return;
		errorMsg = '';
		const { error } = await supabase.from('stores').delete().eq('id', store.id);
		if (error) errorMsg = error.message;
		else await loadData();
	}
</script>

<svelte:head>
	<title>Kelola Toko - AminMart Admin</title>
</svelte:head>

{#if !auth.isSuperAdmin}
	<p class="rounded-lg bg-white p-8 text-center text-slate-500 shadow">
		Akses ditolak. Halaman ini hanya untuk super admin.
	</p>
{:else}
	<div class="mb-4 flex items-center justify-between">
		<h1 class="text-xl font-bold">Kelola Toko</h1>
		<button
			onclick={() => (showForm = !showForm)}
			class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
		>
			+ Tambah Toko
		</button>
	</div>

	{#if errorMsg}
		<p class="mb-3 rounded-lg bg-red-50 px-4 py-2 text-sm text-red-700">{errorMsg}</p>
	{/if}

	{#if showForm}
		<form onsubmit={createStore} class="mb-6 grid gap-3 rounded-xl bg-white p-5 shadow sm:grid-cols-3">
			<label class="text-sm font-medium">
				Nama toko
				<input
					type="text"
					bind:value={newName}
					required
					class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
				/>
			</label>
			<label class="text-sm font-medium">
				Alamat
				<input
					type="text"
					bind:value={newAddress}
					class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
				/>
			</label>
			<label class="text-sm font-medium">
				Telepon
				<input
					type="tel"
					bind:value={newPhone}
					class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
				/>
			</label>
			<div class="sm:col-span-3">
				<button
					type="submit"
					class="rounded-lg bg-emerald-600 px-4 py-2 text-sm text-white hover:bg-emerald-700"
				>
					Simpan Toko
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
						<th class="px-4 py-3">Nama</th>
						<th class="px-4 py-3">Alamat</th>
						<th class="px-4 py-3">Telepon</th>
						<th class="px-4 py-3">Status</th>
						<th class="px-4 py-3"></th>
					</tr>
				</thead>
				<tbody>
					{#each stores as s (s.id)}
						<tr class="border-b last:border-none">
							<td class="px-4 py-3 font-medium">{s.name}</td>
							<td class="px-4 py-3">{s.address ?? '-'}</td>
							<td class="px-4 py-3">{s.phone ?? '-'}</td>
							<td class="px-4 py-3">
								<span
									class="rounded px-2 py-0.5 text-xs {s.is_active
										? 'bg-emerald-100 text-emerald-800'
										: 'bg-red-100 text-red-800'}"
								>
									{s.is_active ? 'Aktif' : 'Nonaktif'}
								</span>
							</td>
							<td class="space-x-2 px-4 py-3 text-right whitespace-nowrap">
								<button onclick={() => toggleActive(s)} class="text-blue-600 hover:underline">
									{s.is_active ? 'Nonaktifkan' : 'Aktifkan'}
								</button>
								<button onclick={() => removeStore(s)} class="text-red-600 hover:underline">
									Hapus
								</button>
							</td>
						</tr>
					{:else}
						<tr><td colspan="5" class="px-4 py-6 text-center text-slate-400">Belum ada toko.</td></tr>
					{/each}
				</tbody>
			</table>
		</div>
	{/if}
{/if}
