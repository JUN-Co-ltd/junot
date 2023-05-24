package jp.co.jun.edi.component;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MSizmstEntity;
import jp.co.jun.edi.model.JunpcSizmstModel;
import jp.co.jun.edi.model.JunpcSizmstSearchConditionModel;
import jp.co.jun.edi.repository.MSizmstRepository;

/**
 * サイズマスタ情報のコンポーネント.
 *
 */
@Component
public class JunpcSizemstListServiceDataComponent {
    @Autowired
    private MSizmstRepository mSizmstRepository;

    /**
     * @param model {@link JunpcSizmstSearchConditionModel} instance
     * @return {@link Page} instance
     */
    public Page<MSizmstEntity> find(final JunpcSizmstSearchConditionModel model) {
        final PageRequest pageRequest = PageRequest.of(
                model.getPage(),
                model.getMaxResults());

        return mSizmstRepository.findByHscdOrderByJun(
                model.getHscd(),
                pageRequest);
    }

    /**
     * Item に変換する.
     * 発注生産システムのサイズマスタは、空文字をnullに変換し、APIのレスポンスサイズを削減する。
     *
     * @param entity {@link MSizmstEntity} instance
     * @return {@link JunpcSizmstModel} instance
     */
    public JunpcSizmstModel toItem(final MSizmstEntity entity) {
        final JunpcSizmstModel item = new JunpcSizmstModel();

        item.setId(entity.getId());
        item.setHscd(StringUtils.defaultIfEmpty(entity.getHscd(), null));
        item.setSzkg(StringUtils.defaultIfEmpty(entity.getSzkg(), null));
        item.setJun(StringUtils.defaultIfEmpty(entity.getJun(), null));

        return item;
    }
}
