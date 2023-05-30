import { Compositions } from './compositions';
import { ItemFileInfo } from './item-file-info';
import { MisleadingRepresentationFile } from './misleading-representation-file';
import { Sku } from './sku';
import { FukukitaruItem } from './fukukitaru-item';
import { Order } from './order';
import { OrderSupplier } from './order-supplier';

/**
 * 品番のModel.
 */
export class Item {
  /** ID */
  id: number;
  /** 品番 */
  partNo: string;
  /** 希望納品日 */
  preferredDeliveryDate: Date;
  /** 納品日 */
  deliveryDate: Date;
  /** 仮発注日 */
  proviOrderDate: Date;
  /** 投入日 */
  deploymentDate: Date;
  /** 投入週 */
  deploymentWeek: number;
  /** P終了日 */
  pendDate: Date;
  /** P終了週 */
  pendWeek: number;
  /** 品名 */
  productName: string;
  /** 品名カナ */
  productNameKana: string;
  /** 年度 */
  year: number;
  /** シーズン */
  seasonCode: string;
  /** サブシーズン */
  subSeasonCode: string;
  /** 生地メーカー */
  matlMakerCode: string;
  /** 生地メーカー名称 */
  matlMakerName: string;
  /** 発注先メーカーID(最新製品) */
  currentProductOrderSupplierId: number;
  /** 生産メーカー */
  mdfMakerCode: string;
  /** 生産メーカー名称 */
  mdfMakerName: string;
  /** 生産工場 */
  mdfMakerFactoryCode: string;
  /** 生産工場名 */
  mdfMakerFactoryName: string;
  /** 委託先工場 */
  consignmentFactory: string;
  /** 原産国 */
  cooCode: string;
  /** 原産国名 */
  cooName: string;
  /** 上代 */
  retailPrice: number;
  /** 生地原価 */
  matlCost: number;
  /** 加工賃 */
  processingCost: number;
  /** 付属品 */
  accessoriesCost: number;
  /** その他原価 */
  otherCost: number;
  /** 企画担当 */
  plannerCode: string;
  /** 企画担当名称 */
  plannerName: string;
  /** 製造担当 */
  mdfStaffCode: string;
  /** 製造担当名称 */
  mdfStaffName: string;
  /** パターンナー */
  patanerCode: string;
  /** 生産メーカー担当 */
  mdfMakerStaffId: number;
  /** パターンナー名称 */
  patanerName: string;
  /** パターンNo */
  patternNo: string;
  /** 丸井デプトブランド */
  maruiDeptBrand: string;
  /** 丸井品番 */
  maruiGarmentNo: string;
  /** Voi区分 */
  voiCode: string;
  /** 素材 */
  materialCode: string;
  /** ゾーン */
  zoneCode: string;
  /** ブランド */
  brandCode: string;
  /** サブブランド */
  subBrandCode: string;
  /** ブランドソート */
  brandSortCode: string;
  /** 部門 */
  deptCode: string;
  /** アイテム */
  itemCode: string;
  /** テイスト */
  tasteCode: string;
  /** タイプ1 */
  type1Code: string;
  /** タイプ2 */
  type2Code: string;
  /** タイプ3 */
  type3Code: string;
  /** 福袋 */
  grabBag: boolean;
  /** 在庫管理区分 */
  inventoryManagementType: boolean;
  /** 評価減区分 */
  devaluationType: boolean;
  /** 軽減税率対象フラグ */
  reducedTaxRateFlg: boolean;
  /** 消化委託区分 */
  digestionCommissionType: boolean;
  /** アウトレット区分 */
  outletCode: string;
  /** アウトレット区分名 */
  outletName: string;
  /** メーカー品番 */
  makerGarmentNo: string;
  /** メモ */
  memo: string;
  /** 商品管理メッセージフラグ */
  itemMassageDisplay: boolean;
  /** 商品管理メッセージ */
  itemMassage: string;
  /** 登録ステータス */
  registStatus: number;
  /** サンプル */
  sample: boolean;
  /** 優良誤認区分 */
  misleadingRepresentation: boolean;
  /** 優良誤認承認区分（組成） */
  qualityCompositionStatus: number;
  /** 優良誤認承認区分（国） */
  qualityCooStatus: number;
  /** 優良誤認承認区分（有害物質） */
  qualityHarmfulStatus: number;
  /** JAN区分 */
  janType: number;
  /** 停止フラグ */
  stopped: boolean;
  /** 連携ステータス */
  linkingStatus: string;
  /** 連携日時 */
  linkedAt: Date;
  /** 登録日時 */
  createdAt: string;
  /** 登録ユーザID */
  createdUserId: string;
  /** 更新日時 */
  updatedAt: Date;
  /** 登録ユーザID */
  updatedUserId: string;
  /** 発注先メーカー情報のリスト */
  orderSuppliers: OrderSupplier[];
  /** SKU情報 */
  skus: Array<Sku>;
  /** 組成情報 */
  compositions: Array<Compositions>;
  /** 品番ファイル情報リスト */
  itemFileInfos: Array<ItemFileInfo>;
  /** 優良誤認検査ファイル情報リスト */
  misleadingRepresentationFiles: MisleadingRepresentationFile[];
  /** 発注情報のリスト */
  orders: Order[];
  /** 登録ステータス変更区分 */
  changeRegistStatusType: number;
  /** バリデーション区分 */
  validationType: number;
  /** フクキタル品番情報. */
  fkItem: FukukitaruItem;
  /** 読み取り専用 */
  readOnly: boolean;
  /** 受注・発注登録済み */
  registeredOrder: boolean;
  /** 発注承認済み */
  approvedOrder: boolean;
  /** 全ての発注が完納 */
  completedAllOrder: boolean;
  /** 納品依頼承認済み */
  approvedDelivery: boolean;
  /** 優良誤認承認済み */
  approvedMisleadingRepresentation: boolean;
  /** 優良誤認（組成）承認済みのカラーのリスト */
  approvedColors: string[];
}
