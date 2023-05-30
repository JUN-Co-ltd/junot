import { Order } from 'src/app/model/order';
import { Delivery } from 'src/app/model/delivery';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { Purchase } from './purchase';

export interface PurchaseTransactionData {
  order: Order;
  delivery: Delivery;
  purchase: Purchase;
  colors: JunpcCodmst[];
  allocations: JunpcCodmst[];
}
