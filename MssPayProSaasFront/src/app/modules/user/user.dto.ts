import { GroupDto } from './../group/group.dto';

export class UserDTO {
  id?: string;
  userName?: string;
  username?: string;
  emailId?: string;
  email?: string;
  password?: string;
  firstname?: string;
  lastName?: string;
  groups?: GroupDto[];
  firstName?: string;
pictureUrl?: any;
}
