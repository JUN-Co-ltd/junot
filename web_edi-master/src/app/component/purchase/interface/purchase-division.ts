import { LgSendType } from 'src/app/const/lg-send-type';

export interface PurchaseDivision {
  /** 仕入ID. */
  id: number;

  /** 仕入(入荷)数. */
  arrivalCount: number;

  /** 仕入(入荷)確定数. */
  fixArrivalCount: number;

  /** 配分課. */
  divisionCode: string;

  /** LG送信区分. */
  lgSendType: LgSendType;
}
