package jp.co.jun.edi.component.model;

import lombok.Data;

/**
 * 発注情報の変更状態を管理しているModel.
 */
@Data
public class OrderChangeStateModel {
    /**
     * MD承認済→未承認となる変更.
     *
     *  <pre>
     *  true : 変更あり
     *  false : 変更なし（初期値）
     *  </pre>
     */
    private boolean unapprovedTargetChanged = false;

    /**
     * 原産国変更.
     *  <pre>
     *  true : 変更あり
     *  false : 変更なし（初期値）
     *  </pre>
     */
    private boolean cooCodeChanged = false;

    /**
     * 仕入先（生産メーカー）変更.
     *  <pre>
     *  true : 変更あり
     *  false : 変更なし（初期値）
     *  </pre>
     */
    private boolean mdfMakerCodeChanged = false;
}
