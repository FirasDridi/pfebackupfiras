export interface Notification {
  groupName: any;
  id: number; // Notification ID
  status: string; // Status of the notification
  message: string; // Notification message
  timestamp: string; // Timestamp for when the notification was generated
  user: {
    id: number; // User ID
    username: string; // Username of the user related to the notification
    firstname?: string; // Optional: First name of the user
  };
  group?: {
    name: string; // Group name, if applicable
  } | null; // Can be null or undefined if not applicable
  read: boolean; // Boolean indicating if the notification has been read
  serviceName: string; // The name of the service related to the notification
  subscriptionRequestId?: number; // Add this property if it exists in the actual data
}
