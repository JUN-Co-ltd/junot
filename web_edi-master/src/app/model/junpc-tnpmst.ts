/**
 * 発注生産システムの店舗マスタのModel.
 */
export class JunpcTnpmst {
  /** ID */
  id: number;

  /** 店舗コード. */
  shpcd: string;

  /** 店舗名称. */
  name: string;

  /** 店舗名略称. */
  sname: string;

  /** 住所１. */
  add1: string;

  /** 住所２. */
  add2: string;

  /** 住所３. */
  add3: string;

  /** 住所４. */
  add4: string;

  /** 電話番号:建屋代表. */
  telban: string;

  /** FAX番号:建屋代表. */
  faxban: string;

  /** 開店日 .*/
  opnymd: string;

  /** 閉店日 .*/
  clsymd: string;

  /** 配分課. */
  hka: string;

  /** 配分順. */
  hjun: string;

  /** 配分区分. */
  distrikind: string;

  /** 直送可否フラグ. */
  directDeliveryFlg: boolean;

  /** 物流コード. */
  logisticsCode: string;

  /** 場所コード. */
  allocationCode: string;

  // PRD_0041 add SIT start
  /** 倉庫区分. */
  warekind: number;
  // PRD_0041 add SIT end
}
