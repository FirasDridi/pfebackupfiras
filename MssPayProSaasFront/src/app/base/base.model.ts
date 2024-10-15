export class BaseEntity<U> {
  createdBy?: U;
  createdDate?: Date;
  lastModifiedBy?: U;
  lastModifiedDate?: Date;
}
export class SimpleBaseEntity<U> extends BaseEntity<U> {
  id?: string;
}

export enum CrudType {
  CREATE,
  DELETE,
  READ,
  UPDATE,
}
