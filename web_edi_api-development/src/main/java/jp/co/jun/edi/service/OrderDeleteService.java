package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderFileInfoEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.TProductionStatusEntity;
import jp.co.jun.edi.entity.TProductionStatusHistoryEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderFileInfoRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.TProductionStatusHistoryRepository;
import jp.co.jun.edi.repository.TProductionStatusRepository;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 発注、発注SKU削除処理.
 */
@Service
public class OrderDeleteService extends GenericDeleteService<DeleteServiceParameter<BigInteger>, DeleteServiceResponse> {
    @Autowired
    private TOrderRepository tOrderRepository;

    @Autowired
    private TOrderSkuRepository tOrderSkuRepository;

    @Autowired
    private TProductionStatusRepository tProductionStatusRepository;

    @Autowired
    private TProductionStatusHistoryRepository tProductionStatusHistoryRepository;

    @Autowired
    private TOrderFileInfoRepository tOrderFileInfoRepository;

    @Autowired
    private TFileRepository tFileRepository;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private ItemComponent itemComponent;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<BigInteger> serviceParameter) {
        // 発注情報を取得。取得できない(削除済み)場合は例外を投げる
        final TOrderEntity tOrderEntity = tOrderRepository.findByOrderId(serviceParameter.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_O_005)));

        // 発注情報ステータスチェック。発注確定済の場合は業務エラー
        if (orderComponent.isOrderConfirmed(tOrderEntity.getOrderApproveStatus())) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_O_002));
        }

        // 品番情報を取得し、データが存在しない場合は例外を投げる
        final TItemEntity itemEntity = itemRepository.findById(tOrderEntity.getPartNoId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 外部連携区分:JUNoT登録以外の場合、削除不可
        itemComponent.validateReadOnly(itemEntity.getExternalLinkingType());

        // 読み取り専用の場合、更新不可
        orderComponent.validateReadOnly(tOrderEntity.getExpenseItem());

        // 削除処理
        deleteReleatedOrder(tOrderEntity);

        return DeleteServiceResponse.builder().build();
    }

    /**
     * 発注に紐づく情報を削除する.
     * ・発注情報
     * ・発注SKU
     * ・生産ステータス
     * ・生産ステータス履歴
     * ・発注ファイル情報
     * ・ファイル情報
     * @param tOrderEntity 削除対象の発注情報
     */
    private void deleteReleatedOrder(final TOrderEntity tOrderEntity) {
        // 削除日付
        final Date deleteAt = new Date();

        // 発注と発注SKUの削除日更新：
        deleteOrderAndOrderSku(tOrderEntity, deleteAt);

        // 生産ステータスと生産ステータス履歴の削除日更新：
        deleteProductionStatusAndProductionStatusHistory(tOrderEntity, deleteAt);

        // 発注ファイル情報とファイル情報の削除日更新：
        deleteOrderFileInfoAndFile(tOrderEntity, deleteAt);
    }

    /**
     * 発注と発注SKUを削除する.
     * @param tOrderEntity 削除対象の発注情報
     * @param deleteAt 削除日
     */
    private void deleteOrderAndOrderSku(final TOrderEntity tOrderEntity, final Date deleteAt) {
        // 発注情報の削除日更新：
        // 削除日をセット
        tOrderEntity.setDeletedAt(deleteAt);
        // 発注情報の削除日を更新
        tOrderRepository.save(tOrderEntity);

        // 発注SKU情報の削除日更新：
        // データ取得
        final List<TOrderSkuEntity> orderSkuList = tOrderSkuRepository.findByOrderId(tOrderEntity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        // 削除日をセット
        for (final TOrderSkuEntity orderSkuEntity : orderSkuList) {
            orderSkuEntity.setDeletedAt(deleteAt);
        }
        // 発注SKUの削除日を更新
        tOrderSkuRepository.saveAll(orderSkuList);
    }

    /**
     * 生産ステータスと生産ステータス履歴を削除する.
     * @param tOrderEntity 削除対象の発注情報
     * @param deleteAt 削除日
     */
    private void deleteProductionStatusAndProductionStatusHistory(final TOrderEntity tOrderEntity, final Date deleteAt) {
        // 生産ステータスの削除日更新：
        // データ取得
        final TProductionStatusEntity productionStatus = tProductionStatusRepository.findByOrderId(tOrderEntity.getId()).orElse(null);

        if (Objects.nonNull(productionStatus)) {
            // 削除日をセット
            productionStatus.setDeletedAt(deleteAt);
            // 発注SKUの削除日を更新
            tProductionStatusRepository.save(productionStatus);
        }

        // 生産ステータス履歴の削除日更新：
        // データ取得
        final List<TProductionStatusHistoryEntity> productionStatusHistoryList = tProductionStatusHistoryRepository.findByOrderId(tOrderEntity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        // 削除日をセット
        for (final TProductionStatusHistoryEntity tProductionStatusHistoryEntity : productionStatusHistoryList) {
            tProductionStatusHistoryEntity.setDeletedAt(deleteAt);
        }
        // 生産ステータス履歴の削除日を更新
        tProductionStatusHistoryRepository.saveAll(productionStatusHistoryList);
    }

    /**
     * 発注ファイル情報とファイル情報を削除する.
     * @param tOrderEntity 削除対象の発注情報
     * @param deleteAt 削除日
     */
    private void deleteOrderFileInfoAndFile(final TOrderEntity tOrderEntity, final Date deleteAt) {
        // 削除対象のファイルID
        final Set<BigInteger> fileIds = new HashSet<>();

        // 発注ファイル情報の削除日更新：
        // データ取得
        final List<TOrderFileInfoEntity> orderFileInfoList = tOrderFileInfoRepository.findByOrderId(tOrderEntity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        // 削除日をセット
        for (final TOrderFileInfoEntity tOrderFileInfoEntity : orderFileInfoList) {
            tOrderFileInfoEntity.setDeletedAt(deleteAt);
            fileIds.add(tOrderFileInfoEntity.getFileNoId());
        }
        // 発注ファイル情報の削除日を更新
        tOrderFileInfoRepository.saveAll(orderFileInfoList);

        // ファイル情報の削除日更新：
        if (fileIds.size() != 0) {
            // 削除対象のファイル情報を取得
            List<TFileEntity> fileList = tFileRepository.findByFileIds(fileIds);

            // 削除日をセット
            for (TFileEntity fileEntity : fileList) {
                fileEntity.setDeletedAt(deleteAt);
            }
            // 削除日を更新
            tFileRepository.saveAll(fileList);
        }
    }
}
