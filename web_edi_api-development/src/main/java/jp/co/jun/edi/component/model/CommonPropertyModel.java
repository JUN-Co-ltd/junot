package jp.co.jun.edi.component.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

/**
 * 共通プロパティ情報のModel.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonPropertyModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** JUNoT.URL. */
    private final String junotUrl;

    /** JUN.企業コード. */
    private final String junCompany;

    /** 送信メール.送信元メールアドレス（複数指定の場合は、カンマ区切り）. */
    private final String sendMailFrom;

    /** 送信メール.送信先メールアドレス（複数指定の場合は、カンマ区切り）. */
    private final String sendMailTo;

    /** 送信メール.CCメールアドレス（複数指定の場合は、カンマ区切り）. */
    private final String sendMailCc;

    /** 送信メール.BCCメールアドレス（複数指定の場合は、カンマ区切り）. */
    private final String sendMailBcc;

    /** 送信メール.ログインユーザメール送信フラグ. */
    private final boolean sendMailLoginUserSend;

    /** 送信メール.メール送信フラグ. */
    private final boolean sendMailSend;

    /** 送信メール.テンプレート.埋め込み文字.件名接頭辞. */
    private final String sendMailTemplateEmbeddedCharacterSubjectPrefix;

    /** S3.プレフィックス.pdf. */
    private final String s3PrefixPdf;

    /** 管理者ユーザ.会社コード. */
    private final String adminUserCompany;

    /** 管理者ユーザ.アカウント名. */
    private final String adminUserAccountName;

    /** 送信メール.署名. */
    private final String sendMailSignature;

    /** 品番・商品一括登録Excel. */
    private final BulkRegistItemPropertyModel bulkRegistItem;
}
