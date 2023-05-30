import { JunpcCodmstIf } from './junpc-codmst-if';

/** 配分課リストの型定義 */
export interface Allocations {
  deliveryAllocationList: JunpcCodmstIf[];
  noDeliveryAllocationList: JunpcCodmstIf[];
}
