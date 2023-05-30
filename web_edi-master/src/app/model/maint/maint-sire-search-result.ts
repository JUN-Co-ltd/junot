import { Authority } from 'src/app/enum/authority.enum';

/**
 * マスタメンテ 取引先検索結果用Model
 */
export class MaintSireSearchResult {
  /** 区分 */
  reckbn: string;

  /** 仕入先コード. */
  sireCode: string;

  /** 仕入先名. */
  sireName: string;

  /** 工場コード. */
  kojCode: string;

  /** 工場名. */
  kojName: string;

  /** 発注区分（生地） */
  hkiji: string;

  /** 発注区分（製品） */
  hseihin: string;

  /** 発注区分（値札） */
  hnefuda: string;

  /** 発注区分（附属） */
  hfuzoku: string;

  /** ブランドコード. */
  brandCode: string;

  /** 発注書送付先区分 */
  hsofkbn: string;

  /** 納品依頼書送付先区分 */
  nsofkbn: string;

  /** 予備受領書送付先区分 */
  ysofkbn: string;

  /** 有害物質対応区分. */
  yugaikbn: string;
}
