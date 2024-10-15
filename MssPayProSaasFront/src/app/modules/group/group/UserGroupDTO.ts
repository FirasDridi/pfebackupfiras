import { SimpleBaseEntity } from "../../../base/base.model";

export class UserGroupDTO {
  id?: number;
  userName?: string;
  firstName?: string;
  lastName?: string;
  emailId?: string;
  password?: string;
  groupName?: string;
  description?: string;
  name?: string;  // Add this line
  tokenGenerated?: boolean; // Add this line
  superUser?: boolean; // Add this property


  roles: any;
  keycloakId: any;
  lastname: string | undefined;
  firstname: string | undefined;
}
