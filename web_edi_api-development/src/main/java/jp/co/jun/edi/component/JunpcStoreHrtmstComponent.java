package jp.co.jun.edi.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MStoreHrtmstEntity;
import jp.co.jun.edi.model.JunpcStoreHrtmstDivisionModel;
import jp.co.jun.edi.model.JunpcStoreHrtmstModel;
import jp.co.jun.edi.repository.MStoreHrtmstRepository;

/**
 * 発注生産システムの店舗別配分率マスタ関連のコンポーネント.
 */
@Component
public class JunpcStoreHrtmstComponent extends GenericComponent {
    /** 店舗別配分率のリストの初期サイズ. */
    private static final int DEFAULT_CAPACITY = 400;

    @Autowired
    private MStoreHrtmstRepository mStoreHrtmstRepository;

    /**
     * 店舗別配分率マスタを取得する.
     *
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @param seasonCode シーズンコード
     * @return 店舗別配分率マスタ
     */
    public List<JunpcStoreHrtmstModel> findStoreHrtmst(final String brandCode, final String itemCode, final String seasonCode) {
        final List<JunpcStoreHrtmstModel> list = new ArrayList<>();

        JunpcStoreHrtmstModel model = generateKeyNullModel();

        for (final MStoreHrtmstEntity entity : mStoreHrtmstRepository
                .findByBrandCodeAndItemCodeAndSeason(brandCode, itemCode, seasonCode, generatePageRequest())) {
            if (isKeyBreak(model, entity)) {
                // キーブレイクした場合、Modelを生成して、リストに追加する（1件目は必ずキーブレイクする）
                model = toModel(entity);

                list.add(model);
            }

            // 配分率合計に加算
            model.setTotalHritu(model.getTotalHritu().add(entity.getHritu()));

            // 店舗別配分率のリストに追加
            model.getStoreHrtmstDivisions().add(toDivisionModel(entity));
        }

        return list;
    }

    /**
     * 以下のソート条件と取得件数を指定したPageRequestを生成する.
     *
     * <pre>
     * 配分率区分 昇順
     * アイテム 降順
     * シーズン 降順
     * ID 昇順
     * </pre>
     *
     * @return PageRequest
     */
    private PageRequest generatePageRequest() {
        return PageRequest.of(0, Integer.MAX_VALUE,
                Sort.by(Order.asc("hrtkbn"),
                        Order.desc("itemCode"),
                        Order.desc("season"),
                        Order.asc("id")));
    }

    /**
     * 初回はキーブレイクさせるため、以下のキー項目をnullにしたmodelを生成する.
     *
     * <pre>
     * アイテム
     * シーズン
     * 配分率区分
     * </pre>
     *
     * @return 配分率区分別の店舗別配分率マスタModel
     */
    private JunpcStoreHrtmstModel generateKeyNullModel() {
        final JunpcStoreHrtmstModel model = new JunpcStoreHrtmstModel();

        model.setItemCode(null);
        model.setSeason(null);
        model.setHrtkbn(null);

        return model;
    }

    /**
     * 以下のキー項目を比較し、キーブレイクしているか判定する.
     * ブランドは検索条件に入っているためキー項目には含めない。
     *
     * <pre>
     * アイテム
     * シーズン
     * 配分率区分
     * </pre>
     *
     * @param model 店舗別配分率マスタModel
     * @param entity 店舗別配分率マスタEntity
     * @return true: キーブレイクしている / false: キーブレイクしていない
     */
    private boolean isKeyBreak(final JunpcStoreHrtmstModel model, final MStoreHrtmstEntity entity) {
        return !StringUtils.equals(model.getItemCode(), entity.getItemCode())
                || !StringUtils.equals(model.getSeason(), entity.getSeason())
                || !StringUtils.equals(model.getHrtkbn(), entity.getHrtkbn());
    }

    /**
     * Entityから配分率区分別の店舗別配分率マスタのmodelへデータの詰め替えを行う.
     *
     * @param entity 店舗別配分率マスタEntity
     * @return 配分率区分別の店舗別配分率マスタModel
     */
    private JunpcStoreHrtmstModel toModel(final MStoreHrtmstEntity entity) {
        final JunpcStoreHrtmstModel model = new JunpcStoreHrtmstModel();

        model.setBrandCode(entity.getBrandCode());
        model.setItemCode(entity.getItemCode());
        model.setSeason(entity.getSeason());
        model.setHrtkbn(entity.getHrtkbn());
        model.setRtname(entity.getRtname());
        model.setTotalHritu(new BigDecimal(0));
        model.setStoreHrtmstDivisions(new ArrayList<>(DEFAULT_CAPACITY));

        return model;
    }

    /**
     * Entityから店舗別配分率マスタのmodelへデータの詰め替えを行う.
     *
     * @param entity 店舗別配分率マスタEntity
     * @return 店舗別配分率マスタModel
     */
    private JunpcStoreHrtmstDivisionModel toDivisionModel(final MStoreHrtmstEntity entity) {
        final JunpcStoreHrtmstDivisionModel model = new JunpcStoreHrtmstDivisionModel();

        model.setId(entity.getId());
        model.setShpcd(entity.getShpcd());
        model.setHritu(entity.getHritu());

        return model;
    }
}
