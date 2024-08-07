export interface InvoiceDTO {
  id: number;
  userId: number;
  groupId: number;
  serviceId: string;
  timestamp: string; // or Date if you handle the conversion
  amount: number;
}
