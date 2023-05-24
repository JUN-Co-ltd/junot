package jp.co.jun.edi.component;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MThresholdEntity;
import jp.co.jun.edi.model.ThresholdModel;
import jp.co.jun.edi.repository.MThresholdRepository;
import jp.co.jun.edi.repository.specification.MThresholdSpecification;

/**
 * 閾値関連のコンポーネント.
 */
@Component
public class ThresholdComponent extends GenericComponent {

    @Autowired
    private MThresholdRepository mThresholdRepository;

    @Autowired
    private MThresholdSpecification tresholdSpec;

    // 共通閾値のブランドコード
    private static final String DEFAULT_BLAND_CODE = "00";

    /**
     * 閾値リストを取得する.
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @return 閾値リスト
     */
    public List<ThresholdModel> listThreshold(final String brandCode, final String itemCode) {
        final List<ThresholdModel> thresholds = new ArrayList<>();

        // 動的に条件文を生成
        for (final MThresholdEntity thresholdEntity : mThresholdRepository.findAll(Specification
                .where(tresholdSpec.notDeleteContains())
                .and(tresholdSpec.brandCodeContains(brandCode))
                .and(tresholdSpec.itemCodeContains(itemCode)),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id"))))) {

            final ThresholdModel thresholdModel = new ThresholdModel();
            BeanUtils.copyProperties(thresholdEntity, thresholdModel);

            // レスポンスに返却する
            thresholds.add(thresholdModel);
        }

        if (thresholds.size() == 0) {
            // 取得できなかった場合は、共通閾値のブランドコードで値を取得する。
            for (final MThresholdEntity thresholdEntity : mThresholdRepository.findAll(Specification
                    .where(tresholdSpec.notDeleteContains())
                    .and(tresholdSpec.brandCodeContains(DEFAULT_BLAND_CODE))
                    .and(tresholdSpec.itemCodeContains(null)),
                    PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id"))))) {

                final ThresholdModel thresholdModel = new ThresholdModel();
                BeanUtils.copyProperties(thresholdEntity, thresholdModel);

                // レスポンスに返却する
                thresholds.add(thresholdModel);
            }
        }

        return thresholds;
    }

}
