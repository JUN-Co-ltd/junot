import { LgSendType } from '../const/lg-send-type';
import { InstructorSystemType } from '../const/const';

/**
 * 在庫出荷指示検索結果モデル.
 */
export class InventoryShipmentSearchResult {

  /** 出荷日 */
  cargoAt: Date;

  /** 出荷場所. */
  cargoPlace: string;

  /** 指示元システム */
  instructorSystem: InstructorSystemType;

  /** ブランドコード */
  brandCode: string;

  /** ブランド名 */
  brandName: string;

  /** 課コード */
  divisionCode: string;

  /** 品番 */
  partNo: string;

  /** 品名 */
  productName: string;

  /** 数量(キー項目で集約した数量合計) */
  // TODO: deliveryLotは仮です。テーブル項目名が決まったら合わせてください
  deliveryLotSum: number;

  /** 上代金額 */
  retailPriceSum: number;

  /** LG送信区分 */
  lgSendType: LgSendType;
}
