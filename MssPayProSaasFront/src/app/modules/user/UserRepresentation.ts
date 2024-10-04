export interface UserRepresentation {
  id: number;
  userName: string;
  firstname: string;
  lastName: string;
  email: string;
  password: string | null;
  keycloakId: string | null;
  roles: string[];
}
