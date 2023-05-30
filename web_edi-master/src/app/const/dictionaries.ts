import { PreEventParam } from './const';

/**
 * ディクショナリ.
 */
export class Dictionaries {
  /** Submitメッセージ */
  static readonly SUBMIT_MSG_CODE = {
    [PreEventParam.CREATE]: 'SUCSESS.ENTRY',
    [PreEventParam.UPDATE]: 'SUCSESS.UPDATE',
    [PreEventParam.DELETE]: 'SUCSESS.DELETE'
  };
}
