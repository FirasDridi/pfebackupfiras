import { GroupDto } from "../group/group.dto";
import { UserDTO } from "../user/user.dto";

export interface ServiceDetailsDto {
  id: string;
  name: string;
  description: string;
  version: string;
  endpoint: string;
  status: boolean;
  configuration: string;
  pricing?: number; 
  groups: GroupDto[];
  users: UserDTO[];
}
