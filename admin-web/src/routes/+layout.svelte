<script lang="ts">
	import '../app.css';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import { auth } from '$lib/auth.svelte';

	let { children } = $props();

	$effect(() => {
		auth.init().then(() => {
			if (!auth.session && page.url.pathname !== '/login') {
				goto('/login', { replaceState: true });
			}
		});
	});

	async function handleLogout() {
		await auth.signOut();
		goto('/login', { replaceState: true });
	}
</script>

{#if page.url.pathname !== '/login' && auth.session}
	<header class="bg-white shadow-sm">
		<div class="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
			<nav class="flex items-center gap-4">
				<span class="font-bold text-emerald-700">AminMart Admin</span>
				<a
					href="/users"
					class="text-sm hover:text-emerald-600 {page.url.pathname === '/users'
						? 'font-semibold text-emerald-600'
						: 'text-slate-600'}"
				>
					Pengguna
				</a>
				{#if auth.isSuperAdmin}
					<a
						href="/stores"
						class="text-sm hover:text-emerald-600 {page.url.pathname === '/stores'
							? 'font-semibold text-emerald-600'
							: 'text-slate-600'}"
					>
						Toko
					</a>
				{/if}
			</nav>
			<div class="flex items-center gap-3 text-sm">
				<span class="hidden text-slate-500 sm:inline">
					{auth.profile?.email}
					<span class="ml-1 rounded bg-slate-100 px-1.5 py-0.5 text-xs uppercase">
						{auth.profile?.role}
					</span>
				</span>
				<button onclick={handleLogout} class="rounded-lg bg-slate-100 px-3 py-1.5 hover:bg-slate-200">
					Keluar
				</button>
			</div>
		</div>
	</header>
{/if}

<main class="mx-auto max-w-6xl px-4 py-6">
	{#if !auth.loading}
		{@render children()}
	{/if}
</main>
