import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { ShellComponent } from './layout/shell.component';
import { DashboardPage } from './pages/dashboard.page';
import { LoginPage } from './pages/login.page';
import { ProjectDetailPage } from './pages/project-detail.page';
import { ProjectsPage } from './pages/projects.page';
import { UsersPage } from './pages/users.page';

export const routes: Routes = [
	{ path: 'login', component: LoginPage },
	{
		path: '',
		component: ShellComponent,
		canActivate: [authGuard],
		children: [
			{ path: 'dashboard', component: DashboardPage },
			{ path: 'projects', component: ProjectsPage },
			{ path: 'projects/:projectId', component: ProjectDetailPage },
			{ path: 'users', component: UsersPage },
			{ path: '', pathMatch: 'full', redirectTo: 'dashboard' }
		]
	},
	{ path: '**', redirectTo: '' }
];
