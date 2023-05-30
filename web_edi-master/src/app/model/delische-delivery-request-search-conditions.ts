/**
 * デリスケ納品依頼検索APIの検索条件
 */
export class DelischeDeliveryRequestSearchConditions {
  /** 発注ID. */
  orderId: number;
  /** 納品日from. */
  deliveryAtFrom: Date;
  /** 納品日to. */
  deliveryAtTo: Date;
  /** 納品遅れ. */
  deliveryAtLateFlg: boolean;

  /** 年度・納品週から変換した納期from. */
  deliveryAtFromByMdweek: string;
  /** 年度・納品週から変換した納期from. */
  deliveryAtToByMdweek: string;

  /** 納品週・年from. */
  mdWeekYearFrom: number;
  /** 納品週from. */
  mdWeekFrom: number;
  /** 納品週・年to. */
  mdWeekYearTo: number;
  /** 納品週to. */
  mdWeekTo: number;

  /** 検索ラジオボタン. */
  searchSelect: string;
}
