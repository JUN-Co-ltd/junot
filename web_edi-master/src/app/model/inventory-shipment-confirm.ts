import { InstructorSystemType } from '../const/const';
/**
 * 在庫出荷確定のモデル.
 */
export class InventoryShipmentConfirm {
  /** チェック(画面input用) */
  check: boolean;

  /** 出荷日 */
  cargoAt: Date;

  /** 出荷場所. */
  cargoPlace: string;

  /** 指示元システム */
  instructorSystem: InstructorSystemType;

  /** 課コード */
  divisionCode: string;

  /** 品番 */
  partNo: string;
}
