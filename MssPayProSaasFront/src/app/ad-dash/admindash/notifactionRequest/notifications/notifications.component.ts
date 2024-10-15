import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../notification-service.service';
import { AdminService } from '../admin-service.service';
import { Notification } from './notifications/notifications.module';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  unreadCount = 0;
  requests: any[] = [];

  constructor(
    private notificationService: NotificationService,
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadNotifications();
  }
  loadNotifications() {
    this.notificationService.getNotifications().subscribe(data => {
      this.notifications = data.filter(n => n.status === 'PENDING').map(notification => {
        // Assign default values for missing data to prevent issues during rendering
        notification.user = notification.user || { username: 'Unknown User' };
        notification.serviceName = notification.serviceName || 'Unknown Service';
        return notification;
      });
      this.unreadCount = this.notifications.length;
    }, error => {
      console.error('Error fetching notifications:', error);
    });
  }




  markAsRead(notificationId: number) {
    this.notificationService.markAsRead(notificationId).subscribe(() => {
      this.notifications = this.notifications.filter(notification => notification.id !== notificationId);
      this.unreadCount = this.notifications.length;
    }, error => {
      console.error('Error marking notification as read:', error);
    });
  }

  approveRequest(requestId: number) {
    this.adminService.approveRequest(requestId).subscribe(() => {
      this.notifications = this.notifications.filter(notification => notification.id !== requestId);
      this.unreadCount = this.notifications.length;
      this.snackBar.open('Request approved', 'Close', { duration: 3000 });
    }, error => {
      console.error('Error approving request:', error);
    });
  }

  rejectRequest(requestId: number) {
    this.adminService.rejectRequest(requestId).subscribe(() => {
      this.notifications = this.notifications.filter(notification => notification.id !== requestId);
      this.unreadCount = this.notifications.length;
      this.snackBar.open('Request rejected', 'Close', { duration: 3000 });
    }, error => {
      console.error('Error rejecting request:', error);
    });
  }
}
