import { FukukitaruItemWashPattern } from './fukukitaru-item-wash-pattern';
import { FukukitaruItemWashAppendicesTerm } from './fukukitaru-item-wash-appendices-term';
import { FukukitaruItemAttentionAppendicesTerm } from './fukukitaru-item-attention-appendices-term';

/**
 * フクキタル品番情報Model.
 */
export class FukukitaruItem {
  /** ID */
  id: number;

  /** 品番ID. */
  partNoId: number;

  /** カテゴリコード（VIS：空文字固定）. */
  categoryCode: number;

  /** NERGY用メリット下札コード1（VIS：空文字固定）. */
  nergyBillCode1: string;

  /** NERGY用メリット下札コード2（VIS：空文字固定）. */
  nergyBillCode2: string;

  /** NERGY用メリット下札コード3（VIS：空文字固定）. */
  nergyBillCode3: string;

  /** NERGY用メリット下札コード4（VIS：空文字固定）. */
  nergyBillCode4: string;

  /** NERGY用メリット下札コード5（VIS：空文字固定）. */
  nergyBillCode5: string;

  /** NERGY用メリット下札コード6（VIS：空文字固定）. */
  nergyBillCode6: string;

  /** シールへの付記用語印字. */
  printAppendicesTerm: boolean;

  /** 原産国表記フラグ. */
  printCoo: boolean;

  /** シールへの品質印字. */
  printParts: boolean;

  /** QRコードの有無. */
  printQrcode: boolean;

  /** 洗濯ネームサイズ印字. */
  printSize: boolean;

  /** シールへの絵表示印字. */
  printWashPattern: boolean;

  /** サスティナブルマーク印字. */
  printSustainableMark: boolean;

  /** シールへのリサイクルマーク印字. */
  recycleMark: number;

  /** REEFUR用ブランド（VIS：空文字固定）. */
  reefurPrivateBrandCode: string;

  /** サタデーズサーフ用NY品番（VIS：空文字固定）. */
  saturdaysPrivateNyPartNo: string;

  /** アテンションシールのシール種類. */
  stickerTypeCode: number;

  /** 洗濯ネームテープ種類. */
  tapeCode: number;

  /** 洗濯ネームテープ巾. */
  tapeWidthCode: number;

  /** 絵表示. */
  listItemWashPattern: FukukitaruItemWashPattern[];

  /** 中国内版情報製品分類. */
  cnProductCategory: number;

  /** 中国内版情報製品種別. */
  cnProductType: number;

  /** 洗濯ネームテープ種類名称. */
  tapeName: string;

  /** 洗濯ネームテープ巾名称. */
  tapeWidthName: string;

  /** 絵表示名称. */
  washPatternName: string;

  /** 洗濯ネーム用付記用語. */
  listItemWashAppendicesTerm: FukukitaruItemWashAppendicesTerm[];

  /** アテンションタグ付記用語. */
  listItemAttentionAppendicesTerm: FukukitaruItemAttentionAppendicesTerm[];

  /** シールへのリサイクルマーク印字名称. */
  recycleName: string;

  /** アテンションシールのシール種類名称. */
  sealName: string;

  /** 中国内版情報製品分類名称. */
  productCategoryName: string;

  /** 中国内版情報製品種別名称. */
  productTypeName: string;
}
