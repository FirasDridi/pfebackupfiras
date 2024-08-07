import { UserDTO } from '../user/user.dto';

export class GroupDto {
  id?: number;
  name?: string;
  user?: UserDTO;
  groupName?: string;
  groupId?: number;
}
