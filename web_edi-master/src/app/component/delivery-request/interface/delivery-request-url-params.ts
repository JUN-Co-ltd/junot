/** 納品依頼画面のURLパラメータの型定義 */
export interface DeliveryRequestUrlParams {
  path: string;           // URLパス
  deliveryId: number;     // 当画面で処理する納品ID
  errorCode: string;      // エラーメッセージコード
  // PRD_0044 del SIT start
  //sqCancelStatus: number; // SQキャンセルAPIレスポンスステータス
  // PRD_0044 del SIT end
  preEvent: number;       // URLクエリパラメータpreEvent
}
