import { FukukitaruOrderSku } from '../model/fukukitaru-order-sku';

/** フクキタル連携 アテンションタグ・ネームの型定義 */
export interface FukukitaruAttentionByColor {
  /** 色コード. */
  colorCode: string;
  /** 色名. */
  colorName: string;
  /** アテンションのリスト. */
  attentionList: FukukitaruOrderSku[];
}
