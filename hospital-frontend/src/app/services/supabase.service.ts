import { Injectable } from '@angular/core';
import { createClient, SupabaseClient, User } from '@supabase/supabase-js';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SupabaseService {
  private supabase: SupabaseClient;
  private _currentUser: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);

  constructor() {
    this.supabase = createClient(environment.supabaseUrl, environment.supabaseAnonKey);

    this.supabase.auth.onAuthStateChange((event, session) => {
      if (session) {
        this._currentUser.next(session.user);
      } else {
        this._currentUser.next(null);
      }
    });
  }

  onAuthStateChange(callback: (event: string, session: any) => void) {
    return this.supabase.auth.onAuthStateChange(callback);
  }

  get currentUser(): Observable<User | null> {
    return this._currentUser.asObservable();
  }

  async getSession() {
    return this.supabase.auth.getSession();
  }

  async signInWithGoogle() {
    return this.supabase.auth.signInWithOAuth({
      provider: 'google',
    });
  }

  async signInWithEmail(email: string, password: string) {
    return this.supabase.auth.signInWithPassword({
      email,
      password,
    });
  }

  async signUp(email: string, password: string) {
    return this.supabase.auth.signUp({
      email,
      password
    });
  }

  async signOut() {
    return this.supabase.auth.signOut();
  }

  async resetPasswordForEmail(email: string) {
    return this.supabase.auth.resetPasswordForEmail(email);
  }
}
