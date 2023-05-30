// PRD_0008 add SIT start
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
// PRD_0008 add SIT end

/** 品番一覧検索Formの入力値 */
export class ItemListSearchFormConditions {
  constructor(
    /** キーワード区分 */
    public keywordType: number,
    /** キーワード */
    public keyword: string,
    /** 社員区分 */
    public staffType: number,
    /** 社員名 */
    public staffName: string,
    /** サブシーズンコード */
    public subSeason: string,
    /** 年度 */
    public year: number,
  ) { }
}

/** 発注一覧検索Formの入力値 */
export class OrderListSearchFormConditions {
  /** キーワード区分 */
  keywordType = 0;
  /** キーワード */
  keyword: string;
  /** 社員区分 */
  staffType = 0;
  /** 社員名 */
  staffName: string;
  /** サブシーズンコード */
  subSeason: string;
  /** 年度 */
  year: number;
  /** 製品納期年from */
  productDeliveryAtYearFrom: number;
  /** 製品納期月from */
  productDeliveryAtMonthlyFrom: number;
  /** 製品納期年to */
  productDeliveryAtYearTo: number;
  /** 製品納期月to */
  productDeliveryAtMonthlyTo: number;
}

/** 配分一覧検索Formの入力値 */
export class DeliveryListSearchFormConditions {
  /** キーワード区分 */
  keywordType = 0;
  /** キーワード */
  keyword: string;
  /** 内訳(キャリー区分) */
  carryType: number;
  /** 完納 */
  orderCompleteType: number;
  /** 仕入 */
  purchasesType: number;
  /** 承認 */
  approvaldType: number;
  /** 出荷 */
  shipmentType: number;
  // PRD_0037 mod SIT start
  ///** 要再配分 */
  //reAllocation: boolean;
  /** 配分 */
  allocationStatusType: number;
  // PRD_0037 mod SIT end
  /** 仕入先(生産メーカーコード). */
  mdfMakerCode: string;
  /** 仕入先正式名称(生産メーカー名). */
  mdfMakerName: string;
  /** 納品日(修正納期)from */
  // PRD_0008 mod SIT start
  //deliveryAtFrom: Date;
  deliveryAtFrom: Date | NgbDateStruct | string;
  // PRD_0008 mod SIT end
  /** 納品日(修正納期)to */
  // PRD_0008 mod SIT start
  //deliveryAtTo: Date;
  deliveryAtTo: Date | NgbDateStruct | string;
  // PRD_0008 mod SIT end
  /** 発注番号from */
  orderNumberFrom: number;
  /** 発注番号to */
  orderNumberTo: number;
  /** 納品番号from */
  deliveryNumberFrom: string;
  /** 納品番号to */
  deliveryNumberTo: string;
  /** 納品依頼番号from */
  deliveryRequestNumberFrom: string;
  /** 納品依頼番号to */
  deliveryRequestNumberTo: string;
  /** PRD_0011 add SIT start */
  /** 事業部 */
  departmentCode: string;
  /** PRD_0011 add SIT end */
}

/** 品番検索APIの検索条件 */
export class ItemSearchConditions {
  /** 品番 */
  partNo: string;
  /** 製品名 */
  productName: string;
  /** ブランドコード */
  brandCode: string;
  /** アイテムコード */
  itemCode: string;
  /** サブシーズンコード */
  subSeasonCode: string;
  /** 年度 */
  year: number;
  /** 企画担当者名 */
  plannerName: string;
  /** 製造担当者名 */
  mdfStaffName: string;
  /** パターンナー名 */
  patanerName: string;
  /** 生産メーカー担当者名 */
  mdfMakerStaffName: string;
  /** ID昇順/降順指定 */
  idOrderBy: number;
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;
  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;

}

/** 発注検索APIの検索条件 */
export class OrderSearchConditions {
  /** 品番ID */
  partNoId: number;
  /** 品番 */
  partNo: string;
  /** 発注ID */
  orderId: number;
  /** 発注No */
  orderNumber: number;
  /** 発注No(複数検索用) */
  orderNumberText: string;
  /** 製品名 */
  productName: string;
  /** ブランドコード */
  brandCode: string;
  /** アイテムコード */
  itemCode: string;
  /** メーカー */
  maker: string;
  /** サブシーズンコード */
  subSeasonCode: string;
  /** 年度 */
  year: number;
  /** 製品納期年from */
  productDeliveryAtYearFrom: number;
  /** 製品納期年to */
  productDeliveryAtMonthlyFrom: number;
  /** 製品納期月from */
  productDeliveryAtYearTo: number;
  /** 製品納期月to */
  productDeliveryAtMonthlyTo: number;
  /** 企画担当者名 */
  plannerName: string;
  /** 製造担当者名 */
  mdfStaffName: string;
  /** パターンナー名 */
  patanerName: string;
  /** 生産メーカー担当者名 */
  mdfMakerStaffName: string;
  /** IDソート順 */
  idOrderBy: number;
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;
  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}

/**
 * 納品依頼検索APIの検索条件
 * ※過去納品
 */
export class DeliveryRequestSearchConditions {
  /** 品番 */
  partNo: string;
  /** 発注ID */
  orderId: number;
  /** 発注No */
  orderNumber: number;
  /** 納品依頼ID */
  deliveryRequestId: number;
  /** 降順フラグ */
  idSortDesc: boolean;
}

/**
 * 納品依頼検索APIの検索条件
 * ※配分一覧
 */
export class DeliveryListSearchConditions {
  /** 品番 */
  partNo: string;
  /** ブランドコード */
  brandCode: string;
  /** 事業部 */
  departmentCode: string;
  /** 内訳(キャリー区分) */
  carryType: string;
  /** 完納フラグ */
  orderCompleteFlg: boolean;
  /** 仕入フラグ */
  purchasesFlg: boolean;
  /** 承認フラグ */
  approvaldFlg: boolean;
  /** 出荷フラグ */
  shipmentFlg: boolean;
  // PRD_0037 mod SIT start
  ///** 要再配分フラグ */
  //reAllocationFlg: boolean;
  /** 配分 */
  allocationStatusType: number;
  // PRD_0037 mod SIT end
  /** 仕入先(生産メーカー). */
  mdfMakerCode: string;
  /** 納品日(修正納期)from */
  deliveryAtFrom: Date;
  /** 納品日(修正納期)to */
  deliveryAtTo: Date;
  /** 発注番号from */
  orderNumberFrom: number;
  /** 発注番号to */
  orderNumberTo: number;
  /** 納品番号from */
  deliveryNumberFrom: string;
  /** 納品番号to */
  deliveryNumberTo: string;
  /** 納品依頼番号from */
  deliveryRequestNumberFrom: string;
  /** 納品依頼番号to */
  deliveryRequestNumberTo: string;
  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;
  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}

/** 閾値取得APIの検索条件 */
export class ThresholdSearchConditions {
  /** ブランドコード */
  brandCode: string;
  /** アイテムコード */
  itemCode: string;
}

/** 納品予定検索APIの検索条件 */
export class DeliveryPlanSearchConditions {
  /** 発注ID */
  orderId: number;
}

 // PRD_0123 #7054 add JFE start
/** 納入場所検索の検索条件 */
export class DeliveryLocationSearchConditions{
  /** 品番情報.ID */
  id: number;
}
 // PRD_0123 #7054 add JFE end
