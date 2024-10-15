export interface InvoiceDTO {
  id: number;
  userId: number;
  groupId: number;
  serviceId: string;
  serviceName: string;  // Include service name
  timestamp: string; // or Date if you handle the conversion
  amount: number;
}
