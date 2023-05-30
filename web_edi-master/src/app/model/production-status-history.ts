import { ProductionStatus } from './production-status';
/**
 * 生産ステータス履歴Model
 */
export class ProductionStatusHistory extends ProductionStatus {
  /** 登録日 */
  createdAt: Date;
}
