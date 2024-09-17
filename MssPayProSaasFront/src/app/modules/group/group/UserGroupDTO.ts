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
  roles: any;
}
