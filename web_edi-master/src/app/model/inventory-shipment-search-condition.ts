import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { InstructorSystemType } from 'src/app/const/const';

/**
 * 在庫出荷一覧検索のモデル.
 */
export class InventoryShipmentSearchCondition {
  /** 店舗コード(ディスタ選択) */
  shpcd: string;

  /** 指示元システム */
  instructorSystem: InstructorSystemType;

  /** 課コード */
  divisionCode: string;

  /** 事業部コード */
  departmentCode: string;

  /** 出荷from */
  cargoAtFrom: Date | NgbDateStruct | string;

  /** 出荷to */
  cargoAtTo: Date | NgbDateStruct | string;

  /** ブランドコード */
  brandCode: string;

  /** アイテムコード */
  itemCode: string;

  /** 次のページのトークン */
  pageToken: string;
}
