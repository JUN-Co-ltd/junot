/**
 * メーカー返品商品検索APIの検索条件.
 */
export class MakerReturnProductSearchConditions {
  /** ブランドコードリスト */
  brandCodes: string[] | string;

  /** アイテムコードリスト */
  itemCodes: string[] | string;

  /** サブシーズンコードリスト */
  subSeasonCodes: string[];

  /** カラーコードリスト */
  colorCodes: string[] | string;

  /** サイズリスト */
  sizeList: string[] | string;

  /** 品番 */
  partNo: string;

  /** 商品コード */
  productCode: string;

  /** 品名 */
  productName: string;

  /** 商品コードの品番. */
  partNoOfProductCode: string;

  /** 商品コードのカラーコード. */
  colorCodeOfProductCode: string;

  /** 商品コードのサイズ. */
  sizeOfProductCode: string;

  /** 仕入先コード */
  supplierCode: string;

  /** 店舗コード */
  shpcd: string;

  /** 上代from */
  retailPriceFrom: number;

  /** 上代to */
  retailPriceTo: number;

  /** 最新発注分のみ */
  latestOrderOnly: boolean;

  /** 1つの結果ページで返されるリストの最大数. */
  maxResults: number;

  /** 戻す結果ページを指定するトークン.このパラメーターが指定された場合、他のパラメーター無視する. */
  pageToken: string;
}
