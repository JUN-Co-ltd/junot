/**
 *フクキタル連携 宛先情報のModel.
 */
export class FukukitaruDestination {
    /** ID */
    id: number;
    /** 住所. */
    address: string;
    /** 会社名. */
    companyName: string;
    /** FAX番号. */
    fax: string;
    /** 郵便番号. */
    postalCode: string;
    /** 電話番号. */
    tel: string;
    /** 承認需要フラグ. */
    isApprovalRequired: boolean;
}
