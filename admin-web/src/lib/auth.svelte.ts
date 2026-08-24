import type { Session } from '@supabase/supabase-js';
import { supabase } from './supabase';

export type UserRole = 'super_admin' | 'admin' | 'kasir';

export type Profile = {
	id: string;
	store_id: string | null;
	email: string;
	full_name: string;
	role: UserRole;
	is_active: boolean;
	stores: { name: string } | null;
};

class AuthState {
	session = $state<Session | null>(null);
	profile = $state<Profile | null>(null);
	loading = $state(true);

	get isSuperAdmin(): boolean {
		return this.profile?.role === 'super_admin';
	}

	get isAdmin(): boolean {
		return this.isSuperAdmin || this.profile?.role === 'admin';
	}

	async init() {
		const { data } = await supabase.auth.getSession();
		this.session = data.session ?? null;
		if (this.session) await this.loadProfile();
		supabase.auth.onAuthStateChange((_event, newSession) => {
			this.session = newSession;
			if (!newSession) this.profile = null;
		});
		this.loading = false;
	}

	async loadProfile() {
		if (!this.session?.user) return;
		const { data } = await supabase
			.from('profiles')
			.select('*, stores(name)')
			.eq('id', this.session.user.id)
			.single();
		this.profile = data as Profile;
	}

	async signOut() {
		await supabase.auth.signOut();
		this.session = null;
		this.profile = null;
	}
}

export const auth = new AuthState();
