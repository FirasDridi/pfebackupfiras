// src/app/components/user-statistics/user-statistics.component.ts

import { Component, OnInit } from '@angular/core';
import { Color, ScaleType } from '@swimlane/ngx-charts';
import { MatSnackBar } from '@angular/material/snack-bar';
import { KeycloakService } from '../../../../keycloak/keycloak.service';

import { forkJoin, of } from 'rxjs';
import { catchError, map, mergeMap, finalize } from 'rxjs/operators';
import { UserService } from '../../../../user/user.service';
import { GroupService } from '../../../../group/group-service.service';
import { GroupRevenueStatisticsResponse, GroupUsageStatisticsResponse, StatisticsService, UserRevenueStatisticsResponse, UserUsageStatisticsResponse } from '../../../../statistics/statistics-dashboard/StatisticsService';
import { UserGroupDTO } from '../../../../group/group/UserGroupDTO';
// Adjust the path as necessary

// Interface for Group Users
interface GroupUser {
  keycloakId: string;
  userName: string | null | undefined;  // Allow undefined as a possible value
  // Add other relevant fields if necessary
}

@Component({
  selector: 'app-user-statistics',
  templateUrl: './user-statistics.component.html',
  styleUrls: ['./user-statistics.component.css']
})
export class UserStatisticsComponent implements OnInit {
  // Data for charts
  userUsageChartData: any[] = [];
  userRevenueChartData: any[] = [];
  groupUsageChartData: any[] = [];
  groupRevenueChartData: any[] = [];

  // User and group information
  userId: string | null = null;
  groupId: number | null = null;
  groupUsers: { id: string, name: string }[] = [];

  // Loading states
  isLoading: boolean = false;
  isUserUsageLoading: boolean = false;
  isUserRevenueLoading: boolean = false;
  isGroupUsageLoading: boolean = false;
  isGroupRevenueLoading: boolean = false;

  // Flags for no data
  noUserUsageData: boolean = false;
  noUserRevenueData: boolean = false;
  noGroupUsageData: boolean = false;
  noGroupRevenueData: boolean = false;

  // Chart color configurations
  colorScheme: Color = {
    name: 'custom',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#5AA454', '#E44D25', '#CFC0BB', '#7aa3e5', '#a8385d', '#aae3f5']
  };

  // Dynamic color mapping
  colorMap: { [key: string]: string } = {};

  constructor(
    private statisticsService: StatisticsService,
    private keycloakService: KeycloakService,
    private snackBar: MatSnackBar,
    private userService: UserService,
    private groupService: GroupService
  ) {}

  ngOnInit(): void {
    this.isLoading = true;
    this.userId = this.keycloakService.getUserId() || null;

    if (this.userId) {
      this.loadGroupIdAndStatistics();
    } else {
      this.showSnackBar('Utilisateur non connecté.', 'Fermer');
      this.isLoading = false;
    }
  }

  private showSnackBar(message: string, action: string) {
    this.snackBar.open(message, action, { duration: 3000 });
  }

  getColor(name: string): string {
    if (this.colorMap[name]) {
      return this.colorMap[name];
    }
    const color = this.colorScheme.domain[Object.keys(this.colorMap).length % this.colorScheme.domain.length];
    this.colorMap[name] = color;
    return color;
  }

  private loadGroupIdAndStatistics(): void {
    this.userService.getUserDetails(this.userId!).pipe(
      map(response => {
        if (response.groups && response.groups.length > 0) {
          this.groupId = response.groups[0].groupId || null; // Ensure groupId is a number
        } else {
          this.groupId = null;
        }
      }),
      mergeMap(() => {
        if (this.groupId !== null) {
          return this.groupService.getGroupUsers(String(this.groupId)).pipe(
            map((groupUsers: UserGroupDTO[]) => {
              this.groupUsers = groupUsers
                .filter(user => user.keycloakId !== this.userId)
                .map(user => ({
                  id: user.keycloakId,
                  name: user.userName ?? 'Unknown User'  // Provide fallback for undefined
                }));
            }),
            catchError(error => {
              console.error('Erreur lors de la récupération des utilisateurs du groupe', error);
              this.showSnackBar('Échec du chargement des utilisateurs du groupe.', 'Fermer');
              return of(null);
            })
          );
        } else {
          this.showSnackBar('Aucun groupe trouvé pour l\'utilisateur.', 'Fermer');
          return of(null);
        }
      }),


      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe(
      () => {
        if (this.groupId !== null) {
          this.loadStatistics();
        }
      },
      error => {
        console.error('Erreur lors de la récupération de l\'ID du groupe ou des utilisateurs du groupe', error);
        this.showSnackBar('Échec du chargement de l\'ID du groupe ou des utilisateurs du groupe.', 'Fermer');
      }
    );
  }

  private loadStatistics(): void {
    this.loadGroupUsageStatistics();
    this.loadGroupRevenueStatistics();
    this.loadUsersUsageStatistics();
    this.loadUsersRevenueStatistics();
  }

  private loadUsersUsageStatistics(): void {
    if (this.groupUsers.length === 0) {
      this.noUserUsageData = true;
      return;
    }

    this.isUserUsageLoading = true;
    const userUsageObservables = this.groupUsers.map(user =>
      this.statisticsService.getUserUsageStatisticsById(user.id).pipe(
        catchError(error => {
          console.error(`Erreur lors de la récupération des statistiques d'utilisation pour l'utilisateur ${user.id}:`, error);
          return of(null); // Return null instead of empty object to handle in mapping
        })
      )
    );

    forkJoin(userUsageObservables).pipe(
      finalize(() => {
        this.isUserUsageLoading = false;
      })
    ).subscribe(usersUsageData => {
      this.userUsageChartData = usersUsageData.flatMap((data: UserUsageStatisticsResponse | null, index: number) => {
        if (data) {
          const serviceEntries = Object.entries(data.usageStatistics);
          return serviceEntries.map(([serviceName, usageCount]) => ({
            name: `${data.userName} - ${serviceName}`,
            value: usageCount,
            color: this.getColor(`${data.userName} - ${serviceName}`)
          }));
        }
        return [];
      });
      this.noUserUsageData = this.userUsageChartData.length === 0;
    }, (error: any) => { // Explicitly type 'error' as 'any'
      console.error('Erreur lors du chargement des statistiques d\'utilisation des utilisateurs', error);
      this.showSnackBar('Échec du chargement des statistiques d\'utilisation des utilisateurs.', 'Fermer');
    });
  }

  private loadUsersRevenueStatistics(): void {
    if (this.groupUsers.length === 0) {
      this.noUserRevenueData = true;
      return;
    }

    this.isUserRevenueLoading = true;
    const userRevenueObservables = this.groupUsers.map(user =>
      this.statisticsService.getUserRevenueStatisticsById(user.id).pipe(
        catchError(error => {
          console.error(`Erreur lors de la récupération des statistiques de revenu pour l'utilisateur ${user.id}:`, error);
          return of(null); // Return null instead of empty object to handle in mapping
        })
      )
    );

    forkJoin(userRevenueObservables).pipe(
      finalize(() => {
        this.isUserRevenueLoading = false;
      })
    ).subscribe(usersRevenueData => {
      this.userRevenueChartData = usersRevenueData.flatMap((data: UserRevenueStatisticsResponse | null, index: number) => {
        if (data) {
          const serviceEntries = Object.entries(data.revenueStatistics);
          return serviceEntries.map(([serviceName, revenueAmount]) => ({
            name: `${data.userName} - ${serviceName}`,
            value: revenueAmount,
            color: this.getColor(`${data.userName} - ${serviceName}`)
          }));
        }
        return [];
      });
      this.noUserRevenueData = this.userRevenueChartData.length === 0;
    }, (error: any) => { // Explicitly type 'error' as 'any'
      console.error('Erreur lors du chargement des statistiques de revenu des utilisateurs', error);
      this.showSnackBar('Échec du chargement des statistiques de revenu des utilisateurs.', 'Fermer');
    });
  }

  private loadGroupUsageStatistics(): void {
    if (this.groupId === null) {
      this.noGroupUsageData = true;
      return;
    }

    this.isGroupUsageLoading = true;
    this.statisticsService.getGroupUsageStatisticsById(this.groupId).pipe(
      finalize(() => {
        this.isGroupUsageLoading = false;
      }),
      catchError(error => {
        console.error('Erreur lors de la récupération des statistiques d\'utilisation du groupe:', error);
        this.showSnackBar('Erreur lors de la récupération des statistiques d\'utilisation du groupe.', 'Fermer');
        return of(null);
      })
    ).subscribe((data: GroupUsageStatisticsResponse | null) => {
      if (data) {
        const usageStatistics = data.usageStatistics;
        this.groupUsageChartData = Object.keys(usageStatistics).map(key => ({
          name: key,
          value: usageStatistics[key],
          color: this.getColor(key)
        }));
        this.noGroupUsageData = this.groupUsageChartData.length === 0;
      } else {
        this.groupUsageChartData = [];
        this.noGroupUsageData = true;
      }
    });
  }

  private loadGroupRevenueStatistics(): void {
    if (this.groupId === null) {
      this.noGroupRevenueData = true;
      return;
    }

    this.isGroupRevenueLoading = true;
    this.statisticsService.getGroupRevenueStatisticsById(this.groupId).pipe(
      finalize(() => {
        this.isGroupRevenueLoading = false;
      }),
      catchError(error => {
        console.error('Erreur lors de la récupération des statistiques de revenu du groupe:', error);
        this.showSnackBar('Erreur lors de la récupération des statistiques de revenu du groupe.', 'Fermer');
        return of(null);
      })
    ).subscribe((data: GroupRevenueStatisticsResponse | null) => {
      if (data) {
        const revenueStatistics = data.revenueStatistics;
        this.groupRevenueChartData = Object.keys(revenueStatistics).map(key => ({
          name: key,
          value: revenueStatistics[key],
          color: this.getColor(key)
        }));
        this.noGroupRevenueData = this.groupRevenueChartData.length === 0;
      } else {
        this.groupRevenueChartData = [];
        this.noGroupRevenueData = true;
      }
    });
  }
}
