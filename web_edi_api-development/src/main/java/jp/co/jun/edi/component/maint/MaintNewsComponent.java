package jp.co.jun.edi.component.maint;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.entity.TNewsEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.maint.MaintNewsModel;
import jp.co.jun.edi.repository.TNewsRepository;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * マスタメンテナンス用のお知らせ情報関連のコンポーネント.
 */
@Component
public class MaintNewsComponent extends GenericComponent {
    @Autowired
    private TNewsRepository tNewsRepository;

    /**
     * お知らせ情報を取得する.
     *
     * @param newsId お知らせID
     * @return {@link TNewsEntity} instance
     * @throws ResourceNotFoundException お知らせが存在しない場合
     */
    public TNewsEntity getTNews(final BigInteger newsId) {
        return tNewsRepository.findByIdAndDeletedAtIsNull(newsId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
    }

    /**
     * {@link MaintNewsModel} を {@link TNewsEntity} にコピーする.
     *
     * @param model {@link MaintNewsModel} instance
     * @param entity {@link TNewsEntity} instance
     */
    public void copyModelToEntity(final MaintNewsModel model, final TNewsEntity entity) {
        entity.setTitle(model.getTitle());
        entity.setContent(model.getContent());
        entity.setOpenStartAt(model.getOpenStartAt());
        entity.setOpenEndAt(model.getOpenEndAt());
        entity.setNewDisplayEndAt(model.getNewDisplayEndAt());
    }

    /**
     * {@link TNewsEntity} を {@link MaintNewsModel} にコピーする.
     *
     * @param entity {@link TNewsEntity} instance
     * @param model {@link MaintNewsModel} instance
     */
    public void copyEntityToModel(final TNewsEntity entity, final MaintNewsModel model) {
        model.setId(entity.getId());
        model.setTitle(entity.getTitle());
        model.setContent(entity.getContent());
        model.setOpenStartAt(entity.getOpenStartAt());
        model.setOpenEndAt(entity.getOpenEndAt());
        model.setNewDisplayEndAt(entity.getNewDisplayEndAt());
    }
}
