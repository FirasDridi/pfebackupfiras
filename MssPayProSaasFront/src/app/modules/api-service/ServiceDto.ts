import { SimpleBaseEntity } from "../../base/base.model";


export class ServiceDto  {


  id?: string;
  name?: string;
  description?: string;
  version?: string;
  endpoint?: string;
  status?: boolean;
  configuration?: string;
  pricing?: number;
  groups?: any[];
  createdDate?: string;
  lastModifiedDate?: string;
  subscriptionStatus: any;


}
