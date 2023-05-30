/**
 * マスタメンテ お知らせ用Model
 */
export class MaintNews {
  /** ID. */
  id: number;

  /** タイトル. */
  title: string;

  /** 本文. */
  content: string;

  /** 公開開始日時. */
  openStartAt: string | Date;

  /** 公開終了日時. */
  openEndAt: string | Date;

  /** 新着表示終了日時. */
  newDisplayEndAt: string | Date;
}
