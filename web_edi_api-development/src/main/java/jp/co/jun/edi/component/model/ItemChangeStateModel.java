package jp.co.jun.edi.component.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 品番情報の変更状態を管理しているModel.
 */
@Data
public class ItemChangeStateModel {

    /**
     * 品番変更.
     *  <pre>
     *  true : 変更あり
     *  false : 変更なし（初期値）
     *  </pre>
     */
    private boolean partNoChanged = false;

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

    /**
     * 登録済みの組成の変更.
     *
     *  <pre>
     *  true : 変更あり
     *  false : 変更なし（初期値）
     *  </pre>
     */
    private boolean registedCompositionChanged = false;

    /**
     * @return true:SKU追加あり
     */
    private boolean isAddSkuColor = false;

    /**
     * 発注書印字対象の組成変更.
     *
     *  <pre>
     *  true : 変更あり
     *  false : 変更なし（初期値）
     *  </pre>
     */
    private boolean printCompositionChanged = false;

    /**
     * 変更された組成のカラーコードリスト.
     * ※共通は除く.共通が変更された場合、共通を使っている色コードを変更扱いとする.
     */
    private List<String> changedCompositionsColors = new ArrayList<>();
}
