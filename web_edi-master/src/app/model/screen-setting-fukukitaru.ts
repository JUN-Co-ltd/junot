import { FukukitaruMasterOrderType } from '../const/const';

import { FukukitaruItem } from './fukukitaru-item';
import { Item } from './item';
import { Order } from './order';
import { FukukitaruDestination } from './fukukitaru-destination';
import { FukukitaruMaster } from './fukukitaru-master';
import { ScreenSettingFukukitaruSku } from './screen-setting-fukukitaru-sku';
import { MaterialFileInfo } from './material-file-info';
import { FukukitaruMasterAppendicesTerm } from './fukukitaru-master-appendices-term';
import { FukukitaruMaterialAttentionTag } from '../model/fukukitaru-material-attention-tag';
import { FukukitaruMaterialAttentionName } from '../model/fukukitaru-material-attention-name';
import { FukukitaruInputAssistSet } from '../model/fukukitaru-input-assist-set';

/**
 * フクキタル連携 画面用マスタ情報のModel.
 */
export class ScreenSettingFukukiatru {
  /** 品番情報. */
  item: Item;

  /** 発注情報. */
  order: Order;

  /** フクキタル品番情報. */
  fkItem: FukukitaruItem;

  /** 納品宛先リスト. */
  listDeriveryAddress: FukukitaruDestination[];

  /** 発注宛先リスト. */
  listSupplierAddress: FukukitaruDestination[];

  /** 請求宛先リスト. */
  listBillingAddress: FukukitaruDestination[];

  /** テープ巾リスト. */
  listTapeWidth: FukukitaruMaster[];

  /** テープ種類リスト. */
  listTapeType: FukukitaruMaster[];

  /** 洗濯ネーム付記用語リスト. */
  listWashNameAppendicesTerm: FukukitaruMasterAppendicesTerm[];

  /** カテゴリコードリスト. */
  listCategoryCode: FukukitaruMaster[];

  /** アテンションタグ付記用語リスト. */
  listAttentionTagAppendicesTerm: FukukitaruMasterAppendicesTerm[];

  /** アテンションシールのシール種類リスト. */
  listAttentionSealType: FukukitaruMaster[];

  /** リサイクルマークリスト. */
  listRecycle: FukukitaruMaster[];

  /** 中国内版情報製品分類リスト. */
  listCnProductCategory: FukukitaruMaster[];

  /** 中国内繁盛法製品種別リスト. */
  listCnProductType: FukukitaruMaster[];

  /** アテンションタグリスト. */
  listAttentionTag: FukukitaruMaterialAttentionTag[];

  /** アテンションネームリスト. */
  listAttentionName: FukukitaruMaterialAttentionName[];

  /** 同封副資材リスト. */
  listAuxiliaryMaterial: FukukitaruMaster[];

  /** 下札類リスト. */
  listBottomBill: FukukitaruMaster[];

  /** 洗濯マークリスト. */
  listWashPattern: FukukitaruMaster[];

  /** SKU. */
  listScreenSku: ScreenSettingFukukitaruSku[];

  /** 洗濯ネームリスト. */
  listWashName: FukukitaruMaster[];

  /** 資材ファイル情報リスト. */
  listMaterialFile: MaterialFileInfo[];

  /** アテンション下札 */
  listAttentionBottomBill: FukukitaruMaster[];

  /** 発注種別. */
  orderType: FukukitaruMasterOrderType;

  /** Nergyメリット下札. */
  listHangTagNergyMerit: FukukitaruMaster[];

  /** 入力補助セット. */
  listInputAssistSet: FukukitaruInputAssistSet[];

  /**
   * スティナブルマーク印字表示フラグ.
   * true：表示、false：非表示
   * */
  sustainableMarkDisplayFlg: boolean;
}
