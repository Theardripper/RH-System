import { Component, signal, computed } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '@core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: string[];
}

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="layout" [class.sidebar-collapsed]="sidebarCollapsed()">

      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <div class="sidebar-logo">
            <span class="logo-icon">HR</span>
            @if (!sidebarCollapsed()) {
              <span class="logo-text">HR System</span>
            }
          </div>
          <button class="collapse-btn" (click)="toggleSidebar()">
            {{ sidebarCollapsed() ? '→' : '←' }}
          </button>
        </div>

        <nav class="sidebar-nav">
          @for (item of visibleNavItems(); track item.route) {
            <a
              [routerLink]="item.route"
              routerLinkActive="active"
              class="nav-item"
              [title]="sidebarCollapsed() ? item.label : ''"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              @if (!sidebarCollapsed()) {
                <span class="nav-label">{{ item.label }}</span>
              }
            </a>
          }
        </nav>

        <div class="sidebar-footer">
          <div class="user-info" *ngIf="!sidebarCollapsed()">
            <div class="user-avatar">
              {{ userInitial() }}
            </div>
            <div class="user-details">
              <span class="user-name">{{ currentUser()?.username }}</span>
              <span class="user-role">{{ roleLabel() }}</span>
            </div>
          </div>
          <button class="logout-btn" (click)="logout()" [title]="'Sair'">
            🚪
          </button>
        </div>
      </aside>

      <!-- Conteúdo principal -->
      <div class="main-wrapper">
        <header class="topbar">
          <div class="topbar-left">
            <button class="mobile-menu-btn" (click)="toggleSidebar()">☰</button>
          </div>
          <div class="topbar-right">
            <div class="user-badge">
              <span class="badge badge-info">{{ roleLabel() }}</span>
              <span class="topbar-username">{{ currentUser()?.username }}</span>
            </div>
          </div>
        </header>

        <main class="content">
          <router-outlet />
        </main>
      </div>

    </div>
  `,
  styles: [`
    .layout {
      display: flex;
      min-height: 100vh;
      background: var(--color-bg);
    }

    /* ---- Sidebar ---- */
    .sidebar {
      width: var(--sidebar-width);
      background: var(--sidebar-bg);
      display: flex;
      flex-direction: column;
      position: fixed;
      top: 0;
      left: 0;
      height: 100vh;
      z-index: 100;
      transition: width 0.25s ease;
      overflow: hidden;
    }

    .layout.sidebar-collapsed .sidebar {
      width: 64px;
    }

    .sidebar-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1.25rem 1rem;
      border-bottom: 1px solid rgba(255,255,255,0.08);
      min-height: 64px;
    }

    .sidebar-logo {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      overflow: hidden;

      .logo-icon {
        width: 36px;
        height: 36px;
        background: var(--color-primary);
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.8rem;
        font-weight: 700;
        color: white;
        flex-shrink: 0;
      }

      .logo-text {
        font-size: 1rem;
        font-weight: 700;
        color: white;
        white-space: nowrap;
      }
    }

    .collapse-btn {
      background: none;
      border: none;
      color: var(--sidebar-text);
      cursor: pointer;
      padding: 0.25rem;
      font-size: 0.75rem;
      flex-shrink: 0;
      &:hover { color: white; }
    }

    .sidebar-nav {
      flex: 1;
      padding: 1rem 0.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      overflow-y: auto;
    }

    .nav-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.625rem 0.75rem;
      border-radius: 0.5rem;
      color: var(--sidebar-text);
      text-decoration: none;
      font-size: 0.875rem;
      transition: all 0.15s ease;
      white-space: nowrap;
      overflow: hidden;

      &:hover {
        background: rgba(255,255,255,0.08);
        color: white;
      }

      &.active {
        background: var(--sidebar-active-bg);
        color: var(--sidebar-text-active);
      }

      .nav-icon { font-size: 1.1rem; flex-shrink: 0; }
    }

    .sidebar-footer {
      padding: 1rem 0.75rem;
      border-top: 1px solid rgba(255,255,255,0.08);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 0.5rem;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      overflow: hidden;

      .user-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: var(--color-primary);
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.8rem;
        font-weight: 700;
        flex-shrink: 0;
      }

      .user-details {
        display: flex;
        flex-direction: column;
        overflow: hidden;

        .user-name {
          font-size: 0.8rem;
          font-weight: 600;
          color: white;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .user-role {
          font-size: 0.7rem;
          color: var(--sidebar-text);
        }
      }
    }

    .logout-btn {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 1.1rem;
      padding: 0.25rem;
      flex-shrink: 0;
      opacity: 0.7;
      &:hover { opacity: 1; }
    }

    /* ---- Main ---- */
    .main-wrapper {
      flex: 1;
      margin-left: var(--sidebar-width);
      display: flex;
      flex-direction: column;
      transition: margin-left 0.25s ease;
      min-width: 0;
    }

    .layout.sidebar-collapsed .main-wrapper {
      margin-left: 64px;
    }

    .topbar {
      height: 64px;
      background: white;
      border-bottom: 1px solid var(--color-border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 1.5rem;
      position: sticky;
      top: 0;
      z-index: 50;
    }

    .mobile-menu-btn {
      display: none;
      background: none;
      border: none;
      font-size: 1.25rem;
      cursor: pointer;
      color: var(--color-text);

      @media (max-width: 768px) { display: block; }
    }

    .topbar-right {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .user-badge {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .topbar-username {
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--color-text);
    }

    .content {
      flex: 1;
      padding: 1.5rem;
      max-width: 1400px;
      width: 100%;
    }

    /* Mobile */
    @media (max-width: 768px) {
      .sidebar {
        transform: translateX(-100%);
        &.open { transform: translateX(0); }
      }

      .main-wrapper {
        margin-left: 0;
      }
    }
  `]
})
export class LayoutComponent {

  sidebarCollapsed = signal(false);

  readonly currentUser = this.authService.currentUser;

  readonly navItems: NavItem[] = [
    { label: 'Dashboard',    icon: '📊', route: '/dashboard' },
    { label: 'Funcionários', icon: '👥', route: '/employees' },
    { label: 'Departamentos',icon: '🏢', route: '/departments', roles: ['ADMIN'] },
    { label: 'Férias',       icon: '🏖️', route: '/vacations' },
  ];

  readonly visibleNavItems = computed(() => {
    const role = this.currentUser()?.role;
    return this.navItems.filter(item =>
      !item.roles || (role && item.roles.includes(role))
    );
  });

  readonly userInitial = computed(() =>
    (this.currentUser()?.username?.[0] ?? 'U').toUpperCase()
  );

  readonly roleLabel = computed(() => {
    const map: Record<string, string> = {
      ADMIN: 'Administrador',
      MANAGER: 'Gerente',
      EMPLOYEE: 'Funcionário'
    };
    return map[this.currentUser()?.role ?? ''] ?? '';
  });

  constructor(private authService: AuthService) {}

  toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }

  logout(): void {
    this.authService.logout();
  }
}
