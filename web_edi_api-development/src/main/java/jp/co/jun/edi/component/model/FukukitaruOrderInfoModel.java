package jp.co.jun.edi.component.model;

import java.io.Serializable;
import java.util.List;

import jp.co.jun.edi.entity.TSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTCompositionLinkingEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFAttentionAppendicesTermEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFItemLinkingEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFOrderLinkingEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFOrderSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFWashAppendicesTermEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFWashPatternEntity;
import lombok.Data;

/**
 * フクキタル発注情報格納Model.
 */
@Data
public class FukukitaruOrderInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** フクキタル発注情報取得. */
    private ExtendedTFOrderLinkingEntity linkingOrderInfoEntity;

    /** フクキタル発注SKU情報の色、サイズ取得. */
    private List<ExtendedTFOrderSkuEntity> listExtendedTFOrderSkuEntity;

    /** フクキタル品番情報取得. */
    private ExtendedTFItemLinkingEntity extendedTFItemLinkingEntity;

    /** フクキタル洗濯マーク情報取得. */
    private List<ExtendedTFWashPatternEntity> listTFWashPatternEntity;

    /** フクキタル洗濯ネーム付記用語情報取得. */
    private List<ExtendedTFWashAppendicesTermEntity> listTFWashAppendicesTermEntity;

    /** フクキタルアテンションタグ付記用語情報取得. */
    private List<ExtendedTFAttentionAppendicesTermEntity> listTFAttentionAppendicesTermEntity;

    /** 品番SKU情報の色、サイズのパターン情報取得. */
    private List<TSkuEntity> listTSkuEntity;

    /** 組成情報取得. */
    private List<ExtendedTCompositionLinkingEntity> listExtendedTCompositionEntity;
}
