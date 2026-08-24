<script lang="ts">
	import { goto } from '$app/navigation';
	import { supabase } from '$lib/supabase';
	import { auth } from '$lib/auth.svelte';

	let email = $state('');
	let password = $state('');
	let errorMsg = $state('');
	let submitting = $state(false);

	async function handleLogin(event: SubmitEvent) {
		event.preventDefault();
		errorMsg = '';
		submitting = true;
		const { error } = await supabase.auth.signInWithPassword({ email, password });
		if (error) {
			errorMsg = 'Email atau password salah.';
			submitting = false;
			return;
		}
		await auth.loadProfile();
		goto('/users', { replaceState: true });
	}
</script>

<svelte:head>
	<title>Masuk - AminMart Admin</title>
</svelte:head>

<div class="flex min-h-screen items-center justify-center px-4">
	<form onsubmit={handleLogin} class="w-full max-w-sm space-y-4 rounded-xl bg-white p-8 shadow-md">
		<h1 class="text-center text-xl font-bold">AminMart Admin</h1>

		<label class="block text-sm font-medium">
			Email
			<input
				type="email"
				bind:value={email}
				required
				class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-emerald-500 focus:outline-none"
			/>
		</label>

		<label class="block text-sm font-medium">
			Password
			<input
				type="password"
				bind:value={password}
				required
				minlength={6}
				class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-emerald-500 focus:outline-none"
			/>
		</label>

		{#if errorMsg}
			<p class="text-sm text-red-600">{errorMsg}</p>
		{/if}

		<button
			type="submit"
			disabled={submitting}
			class="w-full rounded-lg bg-emerald-600 py-2 font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
		>
			{submitting ? 'Memproses...' : 'Masuk'}
		</button>
	</form>
</div>
