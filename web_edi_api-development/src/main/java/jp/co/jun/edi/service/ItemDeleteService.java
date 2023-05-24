package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.entity.TCompositionEntity;
import jp.co.jun.edi.entity.TExternalSkuEntity;
import jp.co.jun.edi.entity.TFItemEntity;
import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.entity.TFileInfoEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderSupplierEntity;
import jp.co.jun.edi.entity.TSkuEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TCompositionRepository;
import jp.co.jun.edi.repository.TExternalSkuRepository;
import jp.co.jun.edi.repository.TFItemRepository;
import jp.co.jun.edi.repository.TFileInfoRepository;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSupplierRepository;
import jp.co.jun.edi.repository.TSkuRepository;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.RegistStatusType;

/**
 * 品番・SKU・組成削除処理.
 */
@Service
public class ItemDeleteService extends GenericDeleteService<DeleteServiceParameter<BigInteger>, DeleteServiceResponse> {
    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private TCompositionRepository compositionRepository;

    @Autowired
    private TSkuRepository skuRepository;

    @Autowired
    private TExternalSkuRepository tExternalSkuRepository;

    @Autowired
    private TFileRepository fileRepository;

    @Autowired
    private TFileInfoRepository fileInfoRepository;

    @Autowired
    private TFItemRepository tfItemRepository;

    @Autowired
    private FukukitaruItemComponent fukukitaruComponent;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSupplierRepository orderSupplierRepository;

    @Autowired
    private ItemComponent itemComponent;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<BigInteger> serviceParameter) {

        // 品番情報を取得し、データが存在しない場合は例外を投げる
        final TItemEntity tItem = itemRepository.findById(serviceParameter.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 外部連携区分:JUNoT登録以外の場合、削除不可
        itemComponent.validateReadOnly(tItem.getExternalLinkingType());

        // 品番情報の登録ステータスが品番の場合は、削除不可のため例外を投げる
        if (tItem.getRegistStatus() == RegistStatusType.PART.getValue()) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_07));
        }

        // 品番情報の登録ステータスが商品かつ受注(発注)が登録されている場合は削除不可のため例外を投げる
        final int orderCount = orderRepository.findByPartNoId(serviceParameter.getId()).size();
        if (orderCount > 0) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_17));
        }

        deleted(tItem);

        return DeleteServiceResponse.builder().build();
    }

    /**
     * 削除処理.
     * @param tItem 削除対象の品番情報
     */
    private void deleted(final TItemEntity tItem) {

        // 削除日付
        final Date deleteAt = new Date();

        // 削除対象のファイルID
        final Set<BigInteger> fileIds = new HashSet<>();

        //----------------------------------
        // 品番番報の削除日更新
        //----------------------------------
        // 削除日をセット
        tItem.setDeletedAt(deleteAt);

        // 品番の削除日を更新
        itemRepository.save(tItem);

        // 発注先メーカー情報の削除
        final List<TOrderSupplierEntity> orderSupplierList =
                orderSupplierRepository.findByPartNoId(tItem.getId(),
                                                       PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        // 削除日をセット
        orderSupplierList.stream().forEach(orderSupplierEntity -> orderSupplierEntity.setDeletedAt(deleteAt));

        // 削除日を更新
        orderSupplierRepository.saveAll(orderSupplierList);

        //----------------------------------
        // SKU情報の削除日更新
        //----------------------------------

        //  データ取得
        final List<TSkuEntity> skuList = skuRepository.findByPartNoId(tItem.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        // 削除日をセット
        for (TSkuEntity skuEntity : skuList) {
            skuEntity.setDeletedAt(deleteAt);
        }

        // 削除日を更新
        skuRepository.saveAll(skuList);

        // 外部SKUの削除日更新
        deleteExternalSku(tItem.getId(), deleteAt);

        //----------------------------------
        // 組成情報の削除日更新
        //----------------------------------

        // データ取得
        final List<TCompositionEntity> compositionList = compositionRepository.findByPartNoId(tItem.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        // 削除日をセット
        for (TCompositionEntity compositionEntity : compositionList) {
            compositionEntity.setDeletedAt(deleteAt);
        }

        // 削除日を更新
        compositionRepository.saveAll(compositionList);

        //----------------------------------
        // ファイル情報の削除日更新
        //----------------------------------

        // ファイル情報を取得
        final List<TFileInfoEntity> fileInfoList = fileInfoRepository.findByPartNoId(tItem.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        // 削除日をセット
        for (TFileInfoEntity fileInfoEntity : fileInfoList) {
            fileInfoEntity.setDeletedAt(deleteAt);
            fileIds.add(fileInfoEntity.getFileNoId());
        }

        // 削除日を更新
        fileInfoRepository.saveAll(fileInfoList);

        //----------------------------------
        // ファイルの削除日更新
        //----------------------------------
        if (fileIds.size() != 0) {
            // ファイル情報を取得
            List<TFileEntity> fileList = fileRepository.findByFileIds(fileIds);

            // 削除日をセット
            for (TFileEntity fileEntity : fileList) {
                fileEntity.setDeletedAt(deleteAt);
            }

            // 削除日を更新
            fileRepository.saveAll(fileList);
        }

        // フクキタル品番情報の削除日更新
        deleteFKItem(tItem.getId());
    }

    /**
     * 品番IDから外部SKUを削除.
     * @param partNoId 品番ID
     * @param deleteAt 削除日
     */
    private void deleteExternalSku(final BigInteger partNoId, final Date deleteAt) {
        //  データ取得
        final List<TExternalSkuEntity> list = tExternalSkuRepository.findByPartNoId(partNoId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        if (CollectionUtils.isEmpty(list)) {
            // レコードが存在しない場合、処理を抜ける
            return;
        }

        // 削除日をセット
        list.forEach(entity -> {
            entity.setDeletedAt(deleteAt);
        });

        // 削除日を更新
        tExternalSkuRepository.saveAll(list);
    }

    /**
     * 品番IDからフクキタル品番情報を削除.
     * @param partNoId 品番情報
     */
    private void deleteFKItem(final BigInteger partNoId) {
        // フクキタル品番情報を取得。
        final Optional<TFItemEntity> optionalTFItemEntity = tfItemRepository.findByPartNoId(partNoId);

        // 取得できない(削除済み)場合は、何もしない
        if (!optionalTFItemEntity.isPresent()) {
            return;
        }

        // 品番情報に紐づく、フクキタル関連テーブルを全て削除
        fukukitaruComponent.delete(optionalTFItemEntity.get());

    }
}
