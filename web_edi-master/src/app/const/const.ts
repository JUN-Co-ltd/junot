import { StringUtils } from '../util/string-utils';

/** 定数 */
export class Const {
  /** newの期限 */
  public static readonly NEW_PERIOD = 7;
  /** デフォルト納品予定明細件数 */
  public static readonly DELIVERY_PLAN_DETAILS_DEFAULT_CNT = 4;
  /** 納品予定明細最大件数 */
  public static readonly DELIVERY_PLAN_DETAILS_MAX_CNT = 50;
  /** 納期変更理由リスト */
  public static readonly DELIVERY_CHANGE_REASON_LIST: { id: number, content: string }[] = [
    { id: 1, content: 'JUN理由' },
    { id: 2, content: 'メーカー理由' },
    { id: 9, content: 'その他' },
  ];
  /** 色・サイズFormのデフォルト行数 */
  public static readonly SKU_FORM_DEFAULT_COUNT = 5;
  /** 色・サイズFormの1回の追加行数 */
  public static readonly SKU_FORM_ADD_COUNT = 5;
  /** 色・サイズFormの最大行数 */
  public static readonly SKU_FORM_MAX_COUNT = 50;

  /** コードマスタ 初期値フラグ */
  public static readonly M_CODMST_INITIAL_FLAG = '1';

  /** 添付できるファイル数の上限_1 */
  public static readonly MAX_FILES_ONE = 1;

  /** 添付できるファイルサイズの上限 */
  public static readonly MAX_FILE_SIZE = 10000000;

  /** ファイルサイズの上限(メッセージ表示用) */
  public static readonly MAX_FILE_SIZE_VIEW = '10MB';

  /** 率手入力の値 */
  public static readonly MANUAL_INPUT_DISTRIBUTION = 'A3';

  /** 靴用シール */
  public static readonly SHOE_SEALS_ARRAY =
    ['PA-T116', 'PA-T117', 'PA-T123', 'SPA-T116', 'SPA-T117', 'SPA-T123', 'SPA-T126', 'VIS-T116', 'VIS-T117', 'VIS-T118',
      'SVIS-T116', 'SVIS-T117', 'SVIS-T118', 'RE-T4', 'RE-T5', 'RE-T97', 'ADL-T5', 'ADL-T6', 'AR-T17', 'AR-T18', 'AR-T21',
      'AR-T22', 'ADR-T3', 'ADR-T4', 'ADR-T216', 'ADR-T217', 'ADR-T224', 'ADR-T225', 'ADR-T226', 'NE-T4', 'RF-T70', 'RT-T6',
      'RF-T7', 'MRM-T6', 'SAR-RT10', 'SPA-RT116'
    ];
  public static readonly NOT_SHOE_SEALS_VIS_ARRAY = ['SVIS-T102', 'SVIS-T107'];
  public static readonly SHOE_SEALS_VIS_ARRAY = ['SVIS-T111', 'SVIS-T112'];

  /** 返品のディスタコードプレフィクス */
  public static readonly MAKER_RETURN_SHPCD_PREFIX = '5550';

  /** 仕入のディスタコードプレフィクス */
  public static readonly PURCHASE_SHPCD_PREFIX = '5551';
}

export class APIUrlConst {
  // 素材取得
  public static readonly MST_MATERIAL: string = '/junpc/codmst/material';
  // ゾーン取得
  public static readonly MST_ZONE: string = '/junpc/codmst/zone';
  /** 品番情報登録 */
  public static readonly ITEM_POST: string = '/items';
  /** 品番情報更新*/
  public static readonly ITEM_PUT: string = '/items';
  /** 品番情報削除 */
  public static readonly ITEM_DELTE: string = '/items';
  /** 品番情報取得 */
  public static readonly ITEM_GET_PARTID: string = '/items';
  /** 品番の一覧情報 */
  public static readonly ITEM_LIST: string = '/items';
}

/**
 * HTTPステータスコード.
 */
export class HttpStatusCode {
  /** OK:200 */
  public static readonly OK: number = 200;
  /** BadRequest:400 */
  public static readonly BAD_REQUEST: number = 400;
  /** Unauthorized:401 */
  public static readonly UNAUTHORIZED: number = 401;
  /** NotFound:404 */
  public static readonly NOT_FOUND: number = 404;
  /** InternalServerError:500 */
  public static readonly INTERNAL_SERVER_ERROR: number = 500;
}

export class APIErrorCode {
  /** 処理成功 */
  public static readonly SUCCSES: number = 0;
  /** 処理失敗 */
  public static readonly ERROR: number = 1;

  /** 品番重複エラー(JUNoT) */
  public static readonly PART_NO_DUP_JUNOT: string = '400_I_01';
  /** 品番重複エラー(発注生産) */
  public static readonly PART_NO_DUP_EXISTING: string = '400_I_02';
  /** 品番昇格エラー */
  public static readonly NOT_CHANGE_TO_PART: string = '400_I_04';
  /** 品番昇格エラー */
  public static readonly REGIST_STATUS_UNMATCH: string = '400_I_05';
  /** 発注中エラー */
  public static readonly ACTIVE_ORDER_REGISTERED: string = '400_I_06';
  /** 品番削除不可エラー */
  public static readonly NO_DELITED: string = '400_I_07';
  /** 原産国コードエラー */
  public static readonly NO_COO_CODE_EXISTING: string = '400_I_08';
  /** 原産国必須エラー */
  public static readonly COO_CODE_REQUIRED: string = '400_I_12';
  /** 仕入先コードエラー */
  public static readonly MAKER_EXISTENCE: string = '400_I_13';
  /** 変更不可項目エラー */
  public static readonly NO_CHANGE_ITEM: string = '400_I_14';
  /** 丸井品番必須エラー */
  public static readonly MARUI_REQUIRED: string = '400_I_15';
  /** 丸井品番存在エラー */
  public static readonly MARUI_EXISTENCE: string = '400_I_16';
  /** 受注登録済 品番削除エラー */
  public static readonly REGISTERED_ORDER_NO_DELITED: string = '400_I_17';

  /** 項目検証エラー */
  public static readonly INVALID: string = '400_04';
}

/** URLパラメータに渡すAPIエラーの種類 */
export class APIErrorTypeParam {
  /** 完納エラー */
  public static readonly ORDER_COMPLETE = 1;
  /** 納品依頼登録済みエラー */
  public static readonly DELIVERY_REGISTED = 2;
}

export class AuthType {
  /** 取引先 */
  public static readonly AUTH_SUPPLIERS: boolean = false;
  /** 社内 */
  public static readonly AUTH_INTERNAL: boolean = true;
  /** ログイン情報 */
  public static readonly LOGIN_INFO = 'loginInfo';
}

/** ファイルのDB操作モード */
export class FileMode {
  /** 新規 */
  public static readonly NEW_FILE = 0;
  /** 削除 */
  public static readonly DELETED_FILE = 1;
  /** 更新 */
  public static readonly UPDATED_FILE = 2;
}

export class FileCategory {
  /** タンザク */
  public static readonly TYPE_TANZAKU = 0;
  /** 見積 */
  public static readonly TYPE_ESTIMATES = 1;
}

/** 画面遷移前の処理 */
export class PreEventParam {
  /** 登録 */
  public static readonly CREATE = 1;
  /** 更新 */
  public static readonly UPDATE = 2;
  /** 削除 */
  public static readonly DELETE = 3;
  /** 承認 */
  public static readonly APPROVE = 4;
  /** 承認エラー */
  public static readonly APPROVE_ERROR = 5;
  /** 発注確定 */
  public static readonly CONFIRM = 6;
  /** 発注確定エラー */
  public static readonly CONFIRM_ERROR = 7;
  /** 更新エラー */
  public static readonly UPDATE_ERROR = 8;
  /** Submitエラー */
  public static readonly SUBMIT_ERROR = 9;
  /** SQロック取得 */
  public static readonly SQLOCK = 10;
  /** 承認済納品依頼訂正 */
  public static readonly CORRECT = 11;
  /** 読み取り専用品番エラー */
  public static readonly ITEM_READONLY_ERROR = 12;
  /** 直送確定 */
  public static readonly DIRECT_CONFIRM = 13;
  // PRD_0044 add SIT start
  /** 承認済納品依頼一時訂正 */
  public static readonly SAVE_CORRECT = 14;
  // PRD_0044 add SIT end
}

/** 発注承認ステータス */
export class OrderApprovalStatus {
  /** 受注未確定かつMD未承認 */
  public static readonly UNAPPROVED = '0';
  /** 発注承認済かつ受注確定済 */
  public static readonly ACCEPT = '1';
  /** 発注承認差し戻し、受注確定済 */
  public static readonly REJECT = '2';
  /** 受注確定済、MD未承認 */
  public static readonly CONFIRM = '3';
  /** 受注差し戻し、MD未承認 */
  public static readonly CONFIRM_REJECT = '4';
}

/** 優良誤認承認ステータス */
export class QualityApprovalStatus {
  /** 非 */
  public static readonly NON_TARGET = 0;
  /** 対象 */
  public static readonly TARGET = 1;
  /** 一部 */
  public static readonly PART = 5;
  /** 承認 */
  public static readonly ACCEPT = 9;
  /** コード対応 */
  public static readonly CODE_MAP = {
    '0': '非',
    '1': '対象',
    '5': '一部',
    '9': '済'
  };
}

/** 納品承認ステータス */
export class DeliveryApprovalStatus {
  /** 未承認 */
  public static readonly UNAPPROVED = '0';
  /** 承認 */
  public static readonly ACCEPT = '1';
  /** 差し戻し */
  public static readonly REJECT = '2';
}

/** 完納区分 */
export class CompleteStatus {
  /** 未完納 */
  public static readonly INCOMPLETE = '0';
  /** 自動完納 */
  public static readonly AUTO_COMPLETE = '5';
  /** 完納 */
  public static readonly COMPLETE = '6';
}

/** 発注情報 済区分 */
export class AllCompleteStatus {
  /** 未 */
  public static readonly INCOMPLETE = '9';
  /** 済 */
  public static readonly COMPLETE = '0';
}

/** 担当区分 */
export class StaffType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 企画担当 */
  public static readonly PLANNING = 1;
  /** 製造担当 */
  public static readonly PRODUCTION = 2;
  /** パターンナー */
  public static readonly PATANER = 3;
  /** メーカー担当 */
  public static readonly MAKER = 4;
}

/** キーワード区分 */
export class KeywordType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 品番 */
  public static readonly ITEM_NO = 1;
  /** 品名 */
  public static readonly ITEM_NAME = 2;
  /** ブランドコード */
  public static readonly BRAND = 3;
  /** アイテムコード */
  public static readonly ITEM_CODE = 4;
}

export class OrderKeywordType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 品番 */
  public static readonly ITEM_NO = 1;
  /** 品名 */
  public static readonly ITEM_NAME = 2;
  /** ブランドコード */
  public static readonly BRAND = 3;
  /** アイテムコード */
  public static readonly ITEM_CODE = 4;
  /** メーカー */
  public static readonly MAKER = 5;
  /** 発注No */
  public static readonly ORDER_NO = 6;
}

/** 登録ステータス */
export class RegistStatus {
  /** 商品登録 */
  public static readonly ITEM = 0;
  /** 品番登録 */
  public static readonly PART = 1;
}

/** 登録ステータス変更区分 */
export class ChangeRegistStatusType {
  /** 商品へ変更 */
  public static readonly ITEM = 0;
  /** 品番へ変更 */
  public static readonly PART = 1;
}

// 画面表示モード
export class ViewMode {
  /** 商品登録 */
  public static readonly ITEM_NEW = 0;
  /** 商品更新 */
  public static readonly ITEM_EDIT = 1;
  /** 品番更新 */
  public static readonly PART_EDIT = 3;
}

/** 仕入先区分 */
export class SupplierType {
  /** 製品 */
  public static readonly MDF_MAKER = '10';
  /** 生地・原糸 */
  public static readonly MALT_MAKER = '30';
}

/** Voi区分 */
export class VoiType {
  /** 未設定 */
  public static readonly UNSET = '0';
  /** オンライン */
  public static readonly ONLINE = '1';
  /** カタログ */
  public static readonly CATALOG = '2';
}

/** Path */
export class Path {
  /** 登録 */
  public static readonly NEW = 'new';
  /** 一覧 */
  public static readonly VIEW = 'view';
  /** 更新 */
  public static readonly EDIT = 'edit';
  /** 訂正 */
  public static readonly CORRECT = 'correct';
  /** 削除 */
  public static readonly DELETE = 'delete';
  /** 承認 */
  public static readonly APPROVAL = 'approval';
}

/** 場所コード */
export class AllocationCode {
  /** 本社撮影 */
  public static readonly PHOTO = '17';
  /** 縫製検品 */
  public static readonly SEWING = '18';
}

/** 最終納品ステータス */
export class LastDeliveryStatus {
  /** 通常納品 */
  public static readonly NORMAL = '0';
  /** 最終納品 */
  public static readonly LAST = '1';
}

/** 生産ステータス */
export class ProductionStatusType {
  /** サンプル */
  public static readonly SAMPLE = 0;
  /** 品質表示 */
  public static readonly QUALITY_DISPLAY = 1;
  /** 下げ札出荷 */
  public static readonly TAG_SHIPMENT = 2;
  /** 仕様確定 */
  public static readonly SPECIFICATION_FIX = 3;
  /** 生地出荷 */
  public static readonly TEXTURE_SHIPMENT = 4;
  /** 生地入荷 */
  public static readonly TEXTURE_ARRIVAL = 5;
  /** 付属入荷 */
  public static readonly ATTACHMENT_ARRIVAL = 6;
  /** 縫製中 */
  public static readonly SEWING_IN = 7;
  /** 縫製検品 */
  public static readonly SEW_INSPECTION = 8;
  /** 検品 */
  public static readonly INSPECTION = 9;
  /** SHIP(9). */
  public static readonly SHIP = 10;
  /** DISTA入荷日 */
  public static readonly DISTA_ARRIVAL = 11;
  /** DISTA仕入日 */
  public static readonly DISTA_PURCHASE = 12;
}

/** 一覧画面のタブ名 */
export class ItemListPageTabName {
  /** 品番一覧 */
  public static readonly ITEM = 'item';
  /** 発注一覧 */
  public static readonly ORDER = 'order';
}

/** 検索区分 */
export class SearchTextType {
  /** コード / 名称 部分一致（デフォルト） */
  public static readonly CODE_NAME_PARTIAL_MATCH = '0';
  /** コード 部分一致 */
  public static readonly CODE_PARTIAL_MATCH = '1';
  /** 名称 部分一致 */
  public static readonly NAME_PARTIAL_MATCH = '2';
  /** コード 完全一致 */
  public static readonly CODE_PERFECT_MATCH = '3';
}

/** 連携ステータス */
export class LinkingStatus {
  /** 連携対象 */
  public static readonly TARGET = '0';
  /** 連携中 */
  public static readonly LINKING = '1';
  /** 連携済 */
  public static readonly LINK_COMPLETE = '2';
  /** 連携対象外 */
  public static readonly NON_TARGET = '9';
}

/** 組成(混率)の共通 */
export class CompositionsCommon {
  /** 組成(混率)の共通の色コード */
  public static readonly COLOR_CODE = '00';
  /** 組成(混率)の共通の色名 */
  public static readonly COLOR_NAME = '共通';
}

/** 組成(混率)の必須区分 */
export class CompositionsRequiredType {
  /** 組成と率が任意 */
  public static readonly ANY = '00';
  /** 組成は必須 */
  public static readonly REQUIRED = '10';
  /** 組成と率が必須 */
  public static readonly BOTH_REQUIRED = '11';
}

/** ストレージキー */
export class StorageKey {
  /** デリスケ検索条件 */
  public static readonly DELISCHE_SEARCH_CONDITIONS = 'delischeSearchFormConditions';
  /** デリスケ検索条件開閉 */
  public static readonly DELISCHE_SEARCH_COLLAPSED = 'delischeSearchFormCollapsed';
  /** 優良誤認一覧検索条件 */
  public static readonly STORAGE_KEY_MR_LIST_SEARCH_CONDITIONS = 'MRListSearchFormConditions';
  /** メーカー返品一覧検索条件 */
  public static readonly MAKER_RETURN_LIST_SEARCH_CONDITIONS = 'makerReturnListSearchFormConditions';
  /** 配分出荷一覧検索条件 */
  public static readonly DISTRIBUTION_SHIPMENT_LIST_SEARCH_CONDITIONS = 'distributionShipmentListSearchFormConditions';
  /** 在庫出荷一覧検索条件 */
  public static readonly INVESTORY_SHIPMENT_LIST_SEARCH_CONDITIONS = 'inventoryShipmentListSearchFormConditions';
  // PRD_0141 #10656 add JFE start
  /** 取引先一覧検索条件 */
  public static readonly MAINT_SIRE_LIST_SEARCH_CONDITIONS = 'maintSireListSearchFormConditions';
  // PRD_0141 #10656 add JFE end
}

/** セッションストレージキー */
export class SessionStorageKey {
  /** 選択タブ名 */
  public static readonly LIST_SELECTED_TAB_NAME = 'listSelectedTabName';
}

/** 選択タブ名 */
export class SelectTabName {
  /** 品番一覧 */
  public static readonly ITEM_LIST = '品番一覧';
  /** 発注一覧 */
  public static readonly ORDER_LIST = '発注一覧';
}

/** 選択タブのURL */
export class SelectTabUrl {
  /** 品番一覧 */
  public static readonly ITEM_LIST = '/items';
  /** 発注一覧 */
  public static readonly ORDER_LIST = '/orders';
}

/** パンくずラベル */
export class BreadcrumbLabel {
  /** トップ */
  public static readonly TOP: string = 'トップ';
}

/** お知らせソート対象カラム名 */
export class NewsSortColumnName {
  /** カラム名 id */
  public static readonly COLUMN_NAME_ID = 'id';
  /** カラム名 タイトル */
  public static readonly COLUMN_NAME_TITLE = 'title';
  /** カラム名 本文 */
  public static readonly COLUMN_NAME_CONTENT = 'content';
  /** カラム名 公開日 */
  public static readonly COLUMN_NAME_OPEN_START_AT = 'openStartAt';
  /** カラム名 公開終了日 */
  public static readonly COLUMN_NAME_OPEN_END_AT = 'openEndAt';
}

/** ソート昇順、降順 */
export class OrderByType {
  /** 昇順 */
  public static readonly ASC = 'ASC';
  /** 降順 */
  public static readonly DESC = 'DESC';
}

/** お知らせタグ区分 */
export class NewsTagType {
  /** 重要 */
  public static readonly IMPORTANT = '1';

  /** コード対応 */
  public static readonly CODE_MAP = {
    '1': '重要',
  };
}

/** 送信ボタンの種類 */
export class SubmitType {
  public static readonly ENTRY = 'ENTRY';                       // 登録
  public static readonly UPDATE = 'UPDATE';                     // 更新
  public static readonly CONTINUE_TO_PART = 'CONTINUE_TO_PART'; // 続けて品番も登録
  public static readonly UPDATE_TO_PART = 'UPDATE_TO_PART';     // 品番として登録
  public static readonly CONFIRM = 'CONFIRM';                   // 確定
  public static readonly CORRECT = 'CORRECT';                   // 訂正
  public static readonly APPROVE = 'APPROVE';                   // 承認
  public static readonly DELETE = 'DELETE';                     // 削除
  public static readonly DIRECT_CONFIRM = 'DIRECT_CONFIRM';     // 直送確定
  // PRD_0044 add SIT start
  public static readonly SAVE_CORRECT = 'SAVE_CORRECT';         // 一時保存
  // PRD_0044 add SIT end
}

/** 登録済ステータス */
export class EntryStatus {
  /** 未登録 */
  public static readonly UNREGISTERED = 0;
  /** 登録済 */
  public static readonly REGISTERED = 1;
}

/** バリデーション区分(商品・品番画面) */
export class ItemValidationType {
  /** 商品バリデーション */
  public static readonly ITEM = 0;
  /** 品番バリデーション */
  public static readonly PART = 1;
}

/** バリデーションパターン（正規表現） */
export class ValidatorsPattern {
  /** 1以上の正数のみ */
  public static readonly POSITIVE_INTEGER = '^([1-9][0-9]*)$';

  /** 整数2桁まで小数1桁まで、負数または先頭0不可※0のみは可 空文字の場合、正規表現はチェックしない. */
  public static readonly INTEGER_SECOND_DECIMAL_FIRST = /^$|^([1-9][0-9]{1}|[0-9])(\.[0-9]{1})?$/;

  /** 整数2桁まで小数1桁まで、または100。負数または先頭0不可※0のみは可 空文字の場合、正規表現はチェックしない. */
  public static readonly INTEGER_SECOND_DECIMAL_FIRST_OR_100 = /^$|^(100|([1-9][0-9]{1}|[0-9])(\.[0-9]{1})?)$/;

  /** 整数2桁まで小数2桁まで、負数または先頭0不可※0のみは可 空文字の場合、正規表現はチェックしない. */
  public static readonly INTEGER_SECOND_DECIMAL_SECOND = /^$|^([1-9][0-9]{1}|[0-9])(\.[0-9]{1,2})?$/;

  /** 1以上の正数のみ. 空文字の場合、正規表現はチェックしない. */
  public static readonly POSITIVE_INTEGER_ = /^$|^([1-9][0-9]*)$/;

  /** 0以上の正数のみ(0のみは可能、先頭0の2桁以上不可) */
  public static readonly NON_NEGATIVE_INTEGER = '^([0]|[1-9][0-9]*)$';

  /** 数値のみ */
  public static readonly NUMERIC = '^[0-9,]*$';

  /** 半角英字のみ(大文字小文字可能) */
  public static readonly HALF_WIDTH_ALPHABET = '^[a-zA-Z]+$';

  /** 半角英数字のみ(大文字小文字可能) */
  public static readonly HALF_WIDTH_ALPHANUMERIC = '^[a-zA-Z0-9-?]*$';

  /** 半角英数字のみ、マイナス不可(大文字小文字可能) */
  public static readonly NO_MINUS_HALF_WIDTH_ALPHANUMERIC = /^[a-zA-Z0-9]*$/;

  /** HH:mm形式. 空文字の場合、正規表現はチェックしない. */
  public static readonly TIME_HH_MM = /^$|^([0-1][0-9]|2[0-3]):[0-5][0-9]$/;

  /** 0から9の半角数字. 空文字の場合、正規表現はチェックしない. */
  public static readonly NUMERIC_0_9 = /^$|^[0-9]+$/;

  /** 他社JAN：0から9の半角数字8桁または13桁. 空文字の場合、正規表現はチェックしない. */
  public static readonly NUMERIC_0_9_OTHER_JAN = /^$|^[0-9]{8}$|^[0-9]{13}$/;

  /** 他社UPC：0から9の半角数字12桁. 空文字の場合、正規表現はチェックしない. */
  public static readonly NUMERIC_0_9_OTHER_UPC = /^$|^[0-9]{12}$/;

  /**
   * メールアドレス形式. 空文字の場合、正規表現はチェックしない.
   *
   * HTML5の `input[type=email]` と同様の定義。
   * ただし、この要件は電子メールの構文を定義するRFC5322に対して 意図的に違反している。
   *
   * https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address
   */
  // tslint:disable-next-line:max-line-length
  public static readonly EMAIL = /^$|^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

  /**
   * メールアドレス形式（カンマ区切り）. 空文字の場合、正規表現はチェックしない.
   *
   * HTML5の `input[type=email]` と同様の定義。
   * ただし、この要件は電子メールの構文を定義するRFC5322に対して 意図的に違反している。
   *
   * https://html.spec.whatwg.org/multipage/input.html#valid-e-mail-address
   */
  // tslint:disable-next-line:max-line-length
  public static readonly EMAIL_COMMA_DELIMITED = /^$|^(?:[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*)(?:,[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*)*$/;

  /**
   * 組成のパーセント.
   * 0～100
   */
  public static readonly PERCENT = /^([0-9]|[1-9][0-9]|100)$/;

  /**
   * 品名カナ. 空文字の場合、正規表現はチェックしない.
   */
  public static readonly PRODUCT_NAME_KANA = new RegExp('^$|^[a-zA-Z0-9 !#\\$%&\\(\\)\\*\\+\\-\\./:;<=>\\?@\\[\\]\\^_`\\{\\|\\}~｡｢｣､･ｰ'
    + 'ａ-ｚＡ-Ｚ０-９ァ-ロワヲン・ー　！＃＄％＆（）＊＋－．／：；＜＝＞？＠［］＾＿｀｛｜｝、。「」]{1,200}$');

  /**
   * 品名に㎜は、禁則文字とする
   *   Added 2021/06/07 (Mon) by JFE
   */

   // public static readonly PRODUCT_NAME = new RegExp('^[^㎜]+$');
   // public static readonly PRODUCT_NAME = new RegExp('^[^ヮヰヱヴヵヶヷヸヹヺヽヾ"\', ￥＂＇，＼～”’‘\\\\‐−‒–—―㎜\t]+$'); '';
   // BugFix 2021/07/28 (Wed) 不要な半角スペースが含まれている
  // PRD_0057 mod JFE start
  public static readonly PRODUCT_NAME = new RegExp('^[^ヮヰヱヴヵヶヷヸヹヺヽヾ“"\',￥＂＇，＼～”’‘\\\\‐−‒–—―㎜\t]+$'); '';
  // PRD_0057 mod JFE end
}

/** 遅延区分 */
export class LateType {
  /** 遅延なし */
  public static readonly NO_LATE = '0';
  /** 遅延あり */
  public static readonly LATE = '1';
}

/** デリスケ生産工程区分 */
export class DelischeProductionStatusType {
  /** 遅延なし */
  public static readonly NO_LATE = '0';
  /** 遅延あり */
  public static readonly LATE = '1';
  /** データなし */
  public static readonly NO_DATA = '2';
}

/** デリスケソートカラム区分 */
export class DelischeSortColumnType {
  /** 納品月度. */
  public static readonly DELIVERY_AT_MONTHLY = 'deliveryAtMonthly';
  /** MD週 */
  public static readonly MD_WEEK = 'mdWeek';
  /** 品番 */
  public static readonly PART_NO = 'partNo';
  /** 品名 */
  public static readonly PRODUCT_NAME = 'productName';
  /** シーズン */
  public static readonly SEASON = 'season';
  // PRD_0146 #10776 add JFE start
  /** 費目 */
  public static readonly EXPENSE_ITEM = 'expenseItem';
  /** 発注No */
  public static readonly ORDER_NUMBER = 'orderNumber';
  /** 関連No */
  public static readonly RELATION_NUMBER= 'relationNumber';
  // PRD_0146 #10776 add JFE end
  /** メーカー */
  public static readonly MAKER = 'mdfMakerName';
  /** 発注日 */
  public static readonly PRODUCT_ORDER_AT = 'productOrderAt';
  /** 発注納期 */
  public static readonly PRODUCT_DELIVERY_AT = 'productDeliveryAt';
  /** 発注数 */
  public static readonly QUANTITY = 'quantity';
  /** 納品依頼数 */
  public static readonly DELIVERY_LOT = 'deliveryLotSum';
  /** 仕入実数 */
  public static readonly ARRIVAL_LOT = 'arrivalLotSum';
  /** 発注残 */
  public static readonly ORDER_REMAINING_LOT = 'orderRemainingLot';
  /** (純)売上数 */
  public static readonly NET_SALES_QUANTITY = 'netSalesQuantity';
  /** 在庫残 */
  public static readonly STOCK_QUANTITY = 'stockQuantity';
}

/** デリスケレコード区分 */
export class DelischeRecordType {
  /** 発注. */
  public static readonly ORDER = '1';
  /** 納品依頼 */
  public static readonly DERIVERY_REQUEST = '2';
  /** 納品SKU */
  public static readonly DERIVERY_SKU = '3';
}

/** デリスケファイルステータス */
export class DelischeFileStatus {
  /** 作成中 */
  public static readonly CREATING = 0;
  /** 作成済 */
  public static readonly COMPLETE_CREATE = 1;
  /** 件数超過エラー */
  public static readonly CNT_ERROR = 2;
  /** その他エラー */
  public static readonly OTHER_ERROR = 9;
}

/** 検索キーワード上限 */
export class KeywordConditionsLimit {
  /** 品番一覧 */
  public static readonly ITEM_LIST = 10;
  /** 発注一覧 */
  public static readonly ORDER_LIST = 10;
  /** 配分一覧 */
  public static readonly DELIVERY_SEARCH_LIST = 10;
}

/** 昇順/降順指定 */
export class OrderBy {
  /** 昇順 */
  public static readonly ASC = 0;
  /** 降順 */
  public static readonly DESC = 1;
}

/** フクキタル連携マスタタイプ */
export class FukukitaruMasterType {
  /** テープ巾 */
  public static readonly TAPE_WIDE = 1;
  /** テープ種類 */
  public static readonly TAPE_TYPE = 2;
  /** 洗濯ネーム付記用語 */
  public static readonly WASH_NAME_APPENDICES_TERM = 3;
  /** アテンションタグ付記用語 */
  public static readonly ATTENTION_TAG_APPENDICES_TERM = 4;
  /** アテンションシールのシール種類 */
  public static readonly ATTENTION_SEAL_TYPE = 5;
  /** リサイクルマーク */
  public static readonly RECYCLE = 6;
  /** 中国内販情報製品分類 */
  public static readonly CN_PRODUCT_CATEGORY = 7;
  /** 中国内販情報製品種別 */
  public static readonly CN_PRODUCT_TYPE = 8;
  /** アテンションタグ */
  public static readonly ATTENTION_TAG = 9;
  /** アテンションネーム */
  public static readonly ATTENTION_NAME = 10;
  /** 同封副資材 */
  public static readonly AUXILIARY_MATERIAL = 11;
  /** 下札類 */
  public static readonly BOTTOM_BILL = 12;
  /** 洗濯マーク */
  public static readonly WASH_PATTERN = 13;
  /** 請求先 */
  public static readonly BILLING_ADDRESS = 14;
  /** 納品先 */
  public static readonly DERIVERY_ADDRESS = 15;
  /** 発注先 */
  public static readonly SUPPLIER_ADDRESS = 16;
  /** SKU */
  public static readonly SKU = 17;
  /** 品番情報 */
  public static readonly ITEM = 18;
  /** 発注情報 */
  public static readonly ORDER = 19;
  /** フクキタル品番情報 */
  public static readonly FUKUKITARU_ITEM = 20;
  /** 発注種別 */
  public static readonly ORDER_TYPE = 21;
  /** 資材ファイル情報 */
  public static readonly MATERIAL_FILE_INFO = 22;
  /** 洗濯ネーム(23). */
  public static readonly WASH_NAME = 23;
  /** アテンション下札 */
  public static readonly ATTENTION_BOTTOM_BILL = 24;
  /** カテゴリコード */
  public static readonly CATEGORY_CODE = 25;
  /** NERGYメリット下札 */
  public static readonly HANG_TAG_NERGY_MERIT = 26;
  /** 入力補助セット */
  public static readonly INPUT_ASSIST_SET = 27;
  /** サスティナブルマーク */
  public static readonly SUSTAINABLE_MARK = 28;
}

/** フクキタルデリバリ種別 */
export class FukukitaruMasterDeliveryType {
  /** 国内. */
  public static readonly DOMESTIC = 1;
  /** 海外. */
  public static readonly OVERSEES = 2;
}

/** デリスケ検索区分 */
export class DelischeSearchType {
  /** 発注検索. */
  public static readonly ORDER = 'order';
  /** 納品検索 */
  public static readonly DERIVERY = 'delivery';
}

/**
 * フクキタル確定ステータスの分類.
 */
export class FukukitaruMasterConfirmStatusType {
  /** 未確定(0). */
  public static readonly ORDER_NOT_CONFIRMED = 0;
  /** 確定(1). */
  public static readonly ORDER_CONFIRMED = 1;
  /** 未承認(2). */
  public static readonly ORDER_UNAPPROVED = 2;
}

/**
 * フクキタル確定ステータスの分類.
 */
export class FukukitaruMasterConfirmStatusTypeName {
  /** タイプ対応 */
  public static readonly ORDER_CONFIRM_MAP = {
    0: '未確定',
    1: '確定',
    2: '未承認',
  };
}

/** フクキタル連携ステータスの定義. */
export class FukukitaruMasterLinkingStatusType {
  /** 連携対象 */
  public static readonly TARGET = 0;
  /** 連携中 */
  public static readonly LINKING = 1;
  /** 連携済 */
  public static readonly LINK_COMPLETE = 2;
  /** 連携対象外(エラー) */
  public static readonly NON_TARGET = 9;
}

/** フクキタルステータス表示名 */
export class MaterialOrderStatusName {
  /** 資材発注確定待ち */
  public static readonly WAITING_CONFIRM = '資材発注確定待ち';
  /** 資材発注確定 */
  public static readonly CONFIRM = '資材発注確定';
  /** 資材発注承認待ち */
  public static readonly APPROVAL_PENDING = '資材発注承認待ち';
  /** 資材発注連携中 */
  public static readonly LINKING = '資材発注連携中';
  /** 資材発注連携済み */
  public static readonly LINKED = '資材発注連携済み';
  /** 資材発注連携エラー */
  public static readonly LINKAGE_ERROR = '資材発注連携エラー';
}

/**
 * フクキタル用発注種別の定義.
 */
export class FukukitaruMasterOrderType {
  /** 洗濯ネーム(1). */
  public static readonly WASH_NAME = 1;
  /** 下札(2). */
  public static readonly BOTTOM_BILL = 2;
  /** 洗濯ネーム小物(3). */
  public static readonly WASH_NAME_KOMONO = 3;
  /** 下札小物(4). */
  public static readonly BOTTOM_BILL_KOMONO = 4;
}

/**
 * フクキタル用発注種別の定義.(タイプ対応)
 */
export class FukukitaruMasterOrderTypeName {
  /** タイプ対応 */
  public static readonly ORDER_TYPE_MAP = {
    1: '洗濯ネーム',
    2: '下札',
    3: '洗濯ネーム小物',
    4: '下札小物',
  };
}

/**
 * フクキタル用資材種別の定義.
 */
export class FukukitaruMasterMaterialType {
  /** 洗濯ネーム(1). */
  public static readonly WASH_NAME = 1;
  /** アテンションネーム(2). */
  public static readonly ATTENTION_NAME = 2;
  /** 洗濯同封副資材(3). */
  public static readonly WASH_AUXILIARY_MATERIAL = 3;
  /** 下札(4). */
  public static readonly BOTTOM_BILL = 4;
  /** アテンションタグ(5). */
  public static readonly ATTENTION_TAG = 5;
  /** アテンション下札(6). */
  public static readonly BOTTOM_BILL_ATTENTION = 6;
  /** NERGY用メリット下札(7). */
  public static readonly BOTTOM_BILL_NERGY_MERIT = 7;
  /** 下札同封副資材(8). */
  public static readonly BOTTOM_BILL_AUXILIARY_MATERIAL = 8;
}

/**
 * フクキタル用資材種別の定義.(FukukitaruMasterMaterialType 対応)
 */
export class FukukitaruMasterMaterialTypeName {
  /** FukukitaruMasterMaterialType 対応 */
  public static readonly MASTER_MATERIAL_MAP = {
    4: '下札',
    8: '同封副資材'
  };
}

/** フクキタル連携項目の共通 */
export class FukukitaruColorCommon {
  /** フクキタル連携項目の共通の色コード */
  public static readonly COLOR_CODE = '00';
  /** フクキタル連携項目の共通の色名 */
  public static readonly COLOR_NAME = '共通';
}

/** 店別配分キャンセルAPIレスポンスステータス区分 */
export class SpecialtyQubeCancelStatusType {
  /** キャンセルOK. */
  public static readonly CANCEL_OK = 0;
  /** キャンセルNG. */
  public static readonly CANCEL_NG = 1;
  /** データ無し. */
  public static readonly NO_DATA = 2;
  /** その他エラー. */
  public static readonly OTHER_ERROR = 9;
  /** 別ユーザーSQロック中. */
  public static readonly LOCKED_BY_OTHER_USER = 99;
}

/** 全店配分削除APIレスポンスステータス区分 */
export class SpecialtyQubeDeleteStatusType {
  /** 削除OK. */
  public static readonly DELETE_OK = 0;
  /** 削除NG. */
  public static readonly DELETE_NG = 1;
  /** データ無し. */
  public static readonly NO_DATA = 2;
  /** 削除済. */
  public static readonly ALREADY_DELETED = 3;
  /** その他エラー. */
  public static readonly OTHER_ERROR = 9;
}

/** フクキタル連携表示切り替え用ブランドコード */
export class FukukitaruBrandCode {
  /** VIS */
  public static readonly VIS = 'BV';
  /** ROPE PICNIC */
  public static readonly ROPE_PICNIC = 'GD';
  /** Rope Picnic Passage */
  public static readonly ROPE_PICNIC_PASSAGE = 'GI';
  /** ロペピクニックキッズ */
  public static readonly ROPE_PICNIC_KIDS = 'GR';
  /** アダム・エ・ロペ レディース */
  public static readonly ADAM_ET_ROPE_LADIES = 'GA';
  /** アダム・エ・ロペ メンズ */
  public static readonly ADAM_ET_ROPE_MENS = 'GM';
}

/** 納品依頼合計配分指定種別 */
export class DeliveryDistributionSpecificationType {
  /** 配分率(％)指定. */
  public static readonly DISTRIBUTION_RATE = 1;
  /** 配分数指定. */
  public static readonly DISTRIBUTION_NUMBER = 2;
}

/** 有害物質対応区分. */
export class MSirmstYugaikbnType {
  /** 対象外. */
  public static readonly NON_TARGET = '0';
  /** 提出済み. */
  public static readonly SUBMITTED = '1';
  /** 未提出. */
  public static readonly UNSUBMITTED = '9';
}

/** 有害物質対応区分. */
export const YugaiKbnDictionary = {
  [MSirmstYugaikbnType.NON_TARGET]: '対象外',
  [MSirmstYugaikbnType.SUBMITTED]: '提出済み',
  [MSirmstYugaikbnType.UNSUBMITTED]: '未提出'
};

/** 優良誤認検査対象区分. */
export class MisleadingRepresentationType {
  /** 原産国. */
  public static readonly COO = 1;
  /** 組成. */
  public static readonly COMPOSITION = 2;
  /** 有害物質. */
  public static readonly HARMFUL = 3;
}

/** シーズン. */
export class SeasonType {
  /** 春夏. */
  public static readonly SPRING_AND_SUMMER = 'A';
  /** 秋冬. */
  public static readonly AUTOMN_AND_WINTER = 'B';
}

/** シーズン. */
export const SeasonDictionary = {
  [SeasonType.SPRING_AND_SUMMER]: '春夏',
  [SeasonType.AUTOMN_AND_WINTER]: '秋冬'
};

/** 全体優良誤認承認ステータス */
export class EntireQualityApprovalType {
  /** 全て対象外. */
  public static readonly ENTIRE_NON_TARGET = 0;
  /** 全て未検査. */
  public static readonly ENTIRE_NON_INSPECTED = 1;
  /** 一部検査済. */
  public static readonly PART_INSPECTED = 2;
  /** 全て検査済. */
  public static readonly ENTIRE_INSPECTED = 3;
}

/** 全体優良誤認承認ステータス. */
export const EntireQualityApprovalDictionary = {
  // 全て対象外は含めないでください
  [EntireQualityApprovalType.ENTIRE_NON_INSPECTED]: '未検査',
  [EntireQualityApprovalType.PART_INSPECTED]: '一部検査',
  [EntireQualityApprovalType.ENTIRE_INSPECTED]: '検査済'
};

/** JAN区分 */
export class JanType {
  /** 自社JAN. */
  public static readonly IN_HOUSE_JAN = 0;
  /** 他社JAN. */
  public static readonly OTHER_JAN = 1;
  /** 他社UPC. */
  public static readonly OTHER_UPC = 2;
}

/** キャリー区分 */
export class CarryType {
  /** 通常 */
  public static readonly NORMAL = '0';
  /** 直送 */
  public static readonly DIRECT = '9';
  // PRD_0104#7055 add JFE start
  /** TC. */
  public static readonly TC = '1';
  // PRD_0104#7055 add JFE end
}

/** 配分一覧キーワード区分 */
export class DeliveryKeywordType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 品番 */
  public static readonly ITEM_NO = 1;
  /** ブランドコード */
  public static readonly BRAND = 2;
  // PRD_0011 del SIT start
  ///** 事業部 */
  //public static readonly DEPARTMENT_CODE = 3;
  // PRD_0011 del SIT end
}

/** 配分一覧検索・内訳区分(キャリー区分) */
export class DeliveryListCarryType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 通常 */
  public static readonly NORMAL = 1;
  /** 直送 */
  public static readonly DIRECT = 2;
  // PRD_0104#7055 add JFE start
  /** TC. */
  public static readonly TC = 3;
  // PRD_0104#7055 add JFE end
}

/** 配分一覧検索・完納区分 */
export class DeliveryListOrderCompleteType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 完納 */
  public static readonly COMPLETE = 1;
  /** 未完納 */
  public static readonly INCOMPLETE = 2;
}

// PRD_0037 add SIT start
/** 配分一覧検索・配分状態 */
export class DeliveryListAllocationStatusType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 未配分 */
  public static readonly UNALLOCATED = 1;
  /** 配分済 */
  public static readonly ALLOCATED = 2;
  /** 再配分 */
  public static readonly REALLOCATION = 3;
  /** 入荷済未配分 */
  public static readonly PURCHASEUNALLOCATED = 4;
}
// PRD_0037 add SIT end

/** 仕入一覧検索・仕入区分 */
export class DeliveryListPurchasesType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 仕入済 */
  public static readonly PURCHASE = 1;
  /** 未仕入 */
  public static readonly UNPURCHASE = 2;
}

/** 承認一覧検索・承認区分 */
export class DeliveryListApprovaldType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 承認済 */
  public static readonly APPROVALD = 1;
  /** 未承認 */
  public static readonly UNAPPROVALD = 2;
}

/** 出荷一覧検索・出荷区分 */
export class DeliveryListShipmentType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 出荷済 */
  public static readonly SHIPMENT = 1;
  /** 未出荷 */
  public static readonly UNSHIPMENT = 2;
}

/** 要再配分検索・要再配分区分 */
export class DeliveryListReallocationType {
  /** 未選択 */
  public static readonly NO_SELECT = 0;
  /** 要再配分 */
  public static readonly REALLOCATION = 1;
}

/** 配分一覧・状態 */
export class DeliveryListAllocationStatus {
  /** 納品情報.承認ステータス = 0:未承認  または　2:差し戻し */
  public static readonly UNAPPROVED = '未承認';
  /** 納品情報.承認ステータス = 1:承認    かつ 配分先の店舗数 = 1 */
  public static readonly APPROVED = '承認済';
  /** 納品情報.承認ステータス = 1:承認    かつ 配分先の店舗数 > 1 */
  public static readonly ALLOCATED = '配分済';
  /** 仕入れ確定数 < 店舗配分の合計 */
  public static readonly REALLOCATION = '要再配分';
  /** 入荷フラグ = true:入荷済  かつ　配分先の店舗数 > 1 */
  public static readonly PURCHASE = '仕入済';
  // PRD_0032 add SIT start
  /** 入荷フラグ = true:入荷済  かつ　配分先の店舗数 > 1 */
  public static readonly UNALLOCATED = '仕入済未配分';
  // PRD_0032 add SIT end
  /** 納品依頼に紐付く出荷確定の受信が完了している */
  public static readonly SHIPMENT = '出荷済';
  // PRD_0087 add SIT start
  /** 納品依頼に紐づく出荷指示の送信が完了している */
  public static readonly SHIPMENT_INSTRUCTION = '指示済未出荷';
  // PRD_0087 add SIT end
}

/**
 * 納品依頼画面表示用マスタの定義.
 */
export class ScreenSettingDeliveryMasterType {
  /** 閾値 */
  public static readonly THRESHOLD = 1;
  /** 店舗マスタ */
  public static readonly TNPMST = 2;
  /** 店舗別配分率. */
  public static readonly STORE_HRTMST = 3;
  // PRD_0031 add SIT start
  /** 在庫数 */
  public static readonly STORE_STOCK = 4;
  // PRD_0031 add SIT end
  // PRD_0033 add SIT start
  /** 売上数 */
  public static readonly POS_SALES_QUANTITY = 5;
  // PRD_0033 add SIT end
  // PRD_0123 #7054 add JFE start
  /** 納入場所 */
  public static readonly DELIVERY_LOCATION = 6;
  // PRD_0123 #7054 add JFE end
}

/** 職種区分の定義. */
export class OccupationType {
  /** DB. */
  public static readonly DB = '20';
  /** 製造. */
  public static readonly PRODUCTION = '21';
  /** バイヤー. */
  public static readonly BUYER = '22';
  /** MD. */
  public static readonly MD = '25';
  /** デザイナー. */
  public static readonly DESIGNER = '30';
  /** 開発. */
  public static readonly DEVELOPER = '41';
  /** 品質管理. */
  public static readonly QUALITY = '99';
}

/** APIバリデーションエラーの対象のリソースの定義. */
export class ResourceType {
  /** 組成. */
  public static readonly COMPOSITION = 'composition';
  /** 資材発注(印字物有り資材). */
  public static readonly MATERIAL_ORDER_MATERIAL_WITH_PRINTED = 'materialOrderMaterialWithPrinted';
  /** 資材発注(発注日). */
  public static readonly MATERIAL_ORDER_ORDER_AT = 'materialOrderOrderAt';
  /** 資材発注(希望出荷日). */
  public static readonly MATERIAL_ORDER_PREFERRED_SHIPPING_AT = 'materialOrderPreferredShippingAt';
  /** 資材発注(アテンション下札). */
  public static readonly MATERIAL_ORDER_BOTTOM_BILLATENTION = 'materialOrderBottomBillAtention';
}

/** カテゴリコードの種類. */
export class CategoryCodeType {
  /** カテゴリコードなし. */
  public static readonly NO_CATEGORY_CODE = 1;
}

/**
 * 在庫出荷 指示元の定義.
 */
export class InstructorSystemType {
  /** Jadore. */
  public static readonly JADORE: number = 0;
  /** SCS. */
  public static readonly SCS: number = 1;
  /** ZOZO. */
  public static readonly ZOZO: number = 2;
  /** StyleVoice(SV). */
  public static readonly SV: number = 3;
  /** USP/ARO. */
  public static readonly ARO: number = 4;

}

/**
 * 納品伝票分類の定義.
 */
export class DeliveryVoucherCategoryType {
  /** 配分出荷伝票. */
  public static readonly SHIPPING_DISTRIBUTION_VOUCHER: number = 1;
  /** ピッキングリスト. */
  public static readonly PICKING_LIST: number = 2;
}

// PRD_0141 #10656 JFE add start
/** 取引先一覧画面表示用レコード区分の定義 */
export class recKbnType {
  /** 仕入先. */
  public static readonly SIRE = '1';
  /** 工場. */
  public static readonly KOJO = '2';
  /** SPOT. */
  public static readonly SPOT = '3';
}

/** 取引先一覧画面表示用レコード区分. */
export const RecKbnDictionary = {
  // 全て対象は含めないでください
  [recKbnType.SIRE]: '仕入先',
  [recKbnType.KOJO]: '工場',
  [recKbnType.SPOT]: 'SPOT'
};

export class KbnMaster {
  // 仕入区分の値を定義する
  public static readonly sirkbn: { kbn: string, value: string }[] = [
    { kbn: '00', value: '社内振替' },
    { kbn: '10', value: '製品' },
    { kbn: '30', value: '生地原糸' },
    { kbn: '40', value: '工賃' },
    // PRD_0167 add JFE start
    { kbn: '50', value: 'その他仕入' },
    // PRD_0167 add JFE end
    { kbn: '60', value: '附属' },
    { kbn: '70', value: '経理' },
    { kbn: '90', value: 'ゴルフ場' }
  ];
  // 有害物質対応区分の値を定義する
  public static readonly yugaikbn: { kbn: string, value: string }[] = [
    { kbn: '0', value: '対象外' },
    { kbn: '1', value: '提出済み' },
    { kbn: '9', value: '未提出' }
  ];
  // 送付先区分の値を定義する
  public static readonly sofkbn: { kbn: string, value: string }[] = [
    { kbn: '0', value: '無し' },
    { kbn: '2', value: 'PDF/DL' },
    { kbn: '3', value: 'PDFメール' }
  ];
};
// PRD_0141 #10656 JFE add end
