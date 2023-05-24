package jp.co.jun.edi.component.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

/**
 * バッチプロパティ情報のModel.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchPropertyModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** PDF生成用.一時フォルダパス. */
    private final String scheduleFopTemporaryFolder;

    /** PDF生成用.XCONFファイルパス. */
    private final String scheduleFopPathXconf;

    /** 受注確定バッチ.最大処理件数. */
    private final int orderReceiveMaxProcesses;

    /** 受注確定バッチ.受注確定書XSLファイルパス. */
    private final String orderReceivePathXsl;

    /** 受注確定バッチ.メール送信フラグ. */
    private final boolean orderReceiveSendMailSend;

    /** 納品依頼バッチ.納品依頼書XSLファイルパス. */
    private final String deliveryRequestPathXsl;

    /** 納品依頼バッチ.納品依頼書住所. */
    private final String deliveryRequestAddress;

    /** 納品依頼バッチ（即時）.最大処理件数. */
    private final int deliveryRequestMaxProcesses;

    /** 納品依頼バッチ（即時）.メール送信フラグ. */
    private final boolean deliveryRequestSendMailSend;

    /** 納品依頼バッチ（夜間）.メール送信フラグ. */
    private final boolean deliveryOfficialRequestSendMailSend;

    /** 発注承認バッチ.発注承認書XSLファイルパス. */
    private final String orderApprovalPathXsl;

    /** 発注承認バッチ（即時）.最大処理件数. */
    private final int orderApprovalMaxProcesses;

    /** 発注承認バッチ（即時）.メール送信フラグ. */
    private final boolean orderApprovalSendMailSend;

    /** 発注承認バッチ（夜間）.メール送信フラグ. */
    private final boolean orderApprovalOfficialSendMailSend;

    /** 資材発注連携エラーメール転送情報. */
    private final MaterialOrderLinkingErrorMailForwardPropertyModel materialOrderLinkingErrorMailForward;

    /** 資材発注連携情報. */
    private final MaterialOrderLinkingPropertyModel materialOrderLinking;

    /** 仕入バッチ.出荷関連プロパティ. */
    private final ShipmentPropertyModel shipmentProperty;

    /** 返品明細（伝票）バッチ.最大処理件数. */
    private final int returnItemMaxProcesses;

    /** 返品明細（伝票）バッチ.返品伝票XSLファイルパス. */
    private final String returnItemPathXsl;

    //PRD_0134 #10654 add JEF start
    /** 仕入明細（伝票）バッチ.最大処理件数. */
    private final int purchaseItemMaxProcesses;

    /** 仕入明細（伝票）バッチ.仕入伝票XSLファイルパス. */
    private final String purchaseItemPathXsl;
    //PRD_0134 #10654 add JEF end

    //PRD_0151 add JFE start
    /** 仕入実績一覧.検索結果XSLファイルパス. */
    private final String purchaseRecordPathXsl;
    //PRD_0151 add JFE end
    /** 直送（伝票）バッチ.納品出荷伝票XSLファイルパス. */
    private final String directDeliveryPathXsl;

    /** 直送（伝票）バッチ.ピッキングリストXSLファイルパス. */
    private final String pickingListPathXsl;

    /** 会計仕入確定バッチ.最大処理件数. */
    private final int accountPurchaseConfirmMaxProcesses;

    // PRD_0143 #10423 JFE add start
    /** タグデータ送信バッチ.メール情報. */
    private final TagdatPropertyModel tagdatProperty;
    // PRD_0143 #10423 JFE add end

    //PRD_0177 add JFE start
    /** 仕入伝票作成バッチ.消化委託返品XSLファイルパス. */
    private final String purchaseDigestionReturnPathXsl;
    //PRD_0177 add JFE end
}
