import { Compositions } from './compositions';

/**
 * 発注情報の表示用組成のModel.
 */
export class OrderViewCompositions extends Compositions {
  /** 表示用組成リスト */
  compositions: Compositions[];
}
