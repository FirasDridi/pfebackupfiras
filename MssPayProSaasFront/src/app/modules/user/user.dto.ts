// File Path: src/app/modules/user/user.dto.ts

import { GroupDto } from './../group/group.dto';

export class UserDTO {
  id?: any;
  userName?: string;
  username?: string;
  emailId?: string;
  email?: string;
  password?: string;
  firstname?: string;
  firstName?: string;
  lastName?: string;
  lastname?: string; // Added this line
  keycloakId?: any;
  groups?: GroupDto[];
  pictureUrl?: any;
  roles?: string[];
  superUser: boolean = false;
}
