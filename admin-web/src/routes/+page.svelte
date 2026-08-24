<script lang="ts">
	import { goto } from '$app/navigation';
	import { auth } from '$lib/auth.svelte';

	$effect(() => {
		if (auth.loading) return;
		if (!auth.session) {
			goto('/login', { replaceState: true });
		} else if (auth.isAdmin) {
			goto('/users', { replaceState: true });
		}
	});
</script>

<p class="text-slate-500">Mengalihkan...</p>
