export interface Notification {
  status: string;
  id: number;
  message: string;
  timestamp: string;
  user: {
    id: number;
    username: string;
    firstname:any;
  };
  group: {
    name: string;
  } | null;
  read: boolean;
  serviceName: string;
}
