import { SimpleBaseEntity } from '../../base/base.model';

export class ClientDto extends SimpleBaseEntity<String> {

  name?: string;
  prenom?: string;
  email?: string;
  motDePasse?: string;
  adresse?: string;
  numeroDeTelephone?: string;
  isActive?: Boolean;
  billingAddress?: string;
  packageId?: number;
  packageName?: string;
}
