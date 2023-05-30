import { ItemFileInfo } from './item-file-info';

/**
 * 登録する品番ファイル情報のモデル
 * ファイルの実態もここに格納する
 */
export class ItemFileInfoRequest extends ItemFileInfo {
  /** 登録モード */
  mode: number;
  /** ファイル */
  file: any;
}
