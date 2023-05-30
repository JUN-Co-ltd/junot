/**
 * デリスケ発注検索APIの検索条件
 */
export class DelischeOrderSearchConditions {
  /** 事業部コード. */
  divisionCode: string;
  /** ブランドコード. */
  brandCode: string;
  /** アイテムコード. */
  itemCode: string;
  /** 品番. */
  partNo: string;
  /** シーズン. */
  season: string[];
  /** メーカー. */
  mdfMaker: string;
  /** 製造担当. */
  mdfStaff: string;
  /** 発注納期from. */
  productDeliveryAtFrom: Date;
  /** 発注納期to. */
  productDeliveryAtTo: Date;
  /** 納品日from. */
  deliveryAtFrom: Date;
  /** 納品日to. */
  deliveryAtTo: Date;

  /** 年度・月度から変換した生産納期from. */
  productDeliveryAtFromByMonthly: string;
  /** 年度・月度から変換した生産納期to. */
  productDeliveryAtToByMonthly: string;
  /** 年度・納品週から変換した納期from. */
  deliveryAtFromByMdweek: string;
  /** 年度・納品週から変換した納期from. */
  deliveryAtToByMdweek: string;

  /** 年度from. */
  productDeliveryAtMonthlyYearFrom: number;
  /** 月度from. */
  productDeliveryAtMonthlyFrom: number;
  /** 年度to. */
  productDeliveryAtMonthlyYearTo: number;
  /** 月度to. */
  productDeliveryAtMonthlyTo: number;

  /** 納品週・年from. */
  mdWeekYearFrom: number;
  /** 納品週from. */
  mdWeekFrom: number;
  /** 納品週・年to. */
  mdWeekYearTo: number;
  /** 納品週to. */
  mdWeekTo: number;
  // PRD_0146 #10776 add JFE start
  /** 費目. */
  expenseItem: string;
  // PRD_0146 #10776 add JFE end

  /** 検索ラジオボタン. */
  searchSelect: string;

  /** 納品遅れ. */
  deliveryAtLateFlg: boolean;
  /** 完納は対象外. */
  excludeCompleteOrder: boolean;
  /** 発注残あり. */
  existsOrderRemaining: boolean;
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;
  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}
