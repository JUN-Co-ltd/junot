package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.extended.ExtendedItemMisleadingRepresentationEntity;
import jp.co.jun.edi.entity.extended.ExtendedTMisleadingRepresentationFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.CompositionModel;
import jp.co.jun.edi.model.FileModel;
import jp.co.jun.edi.model.ItemFileInfoModel;
import jp.co.jun.edi.model.ItemMisleadingRepresentationModel;
import jp.co.jun.edi.model.MisleadingRepresentationFileModel;
import jp.co.jun.edi.model.MisleadingRepresentationModel;
import jp.co.jun.edi.model.SkuModel;
import jp.co.jun.edi.repository.TFileInfoSelectRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.extended.ExtendedItemMisleadingRepresentationRepository;
import jp.co.jun.edi.repository.extended.ExtendedTCompositionRepository;
import jp.co.jun.edi.repository.extended.ExtendedTMisleadingRepresentationFileRepository;
import jp.co.jun.edi.repository.extended.ExtendedTMisleadingRepresentationRepository;
import jp.co.jun.edi.repository.extended.ExtendedTSkuRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.FileInfoCategory;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OrderApprovalType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 品番・優良誤認情報取得処理.
 */
@Service
public class ItemMisleadingRepresentationGetService extends GenericGetService<GetServiceParameter<BigInteger>,
GetServiceResponse<ItemMisleadingRepresentationModel>> {

    @Autowired
    private ExtendedItemMisleadingRepresentationRepository exItemMRRepository;

    @Autowired
    private ExtendedTSkuRepository exSkuRepository;

    @Autowired
    private ExtendedTCompositionRepository exCompositionRepository;

    @Autowired
    private ExtendedTMisleadingRepresentationRepository exMisleadingRepresentationRepository;

    @Autowired
    private TFileInfoSelectRepository fileInfoSelectRepository;

    @Autowired
    private ExtendedTMisleadingRepresentationFileRepository exTMisleadingRepresentationFileRepository;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TItemRepository itemRepository;

    @Autowired
    private ItemComponent itemComponent;

    @Override
    protected GetServiceResponse<ItemMisleadingRepresentationModel> execute(
           final GetServiceParameter<BigInteger> serviceParameter) {

        // 優良誤認用品番情報取得　取得できない場合はエラー
        final ExtendedItemMisleadingRepresentationEntity exItemMREntity = exItemMRRepository.findById(serviceParameter.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 優良誤認用品番情報
        final ItemMisleadingRepresentationModel model = createItemMisleadingRepresentationModel(exItemMREntity);

        // 品番情報を取得し、データが存在しない場合は例外を投げる
        final TItemEntity itemEntity = itemRepository.findByPartNo(exItemMREntity.getPartNo()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        model.setReadOnly(itemComponent.isReadOnly(itemEntity.getExternalLinkingType()));

        // SKU情報
        model.setSkus(findSku(serviceParameter.getId()));

        // 組成情報
        model.setCompositions(findComposition(serviceParameter.getId()));

        // 品番ファイル情報(タンザク)
        model.setTanzakuItemFileInfo(findTanzakuFileInfo(serviceParameter.getId()));

        // 優良誤認検査ファイル情報
        model.setMisleadingRepresentationFiles(findMisleadingRepresentationFileInfo(serviceParameter.getId()));

        // 優良誤認情報
        model.setMisleadingRepresentations(findMisleadingRepresentation(serviceParameter.getId()));

        // 発注情報取得
        final String[] searchApplovalTypeList =
            {OrderApprovalType.APPROVED.getValue(), OrderApprovalType.CONFIRM.getValue(), OrderApprovalType.REJECT.getValue()};

        final List<TOrderEntity> orderEntityList = orderRepository.findByPartNoIdAndOrderApproveStatus(
                exItemMREntity.getId(),
                searchApplovalTypeList,
                PageRequest.of(0, 1, Sort.by(Order.desc("orderNumber"))))
                .getContent();

        if (CollectionUtils.isNotEmpty(orderEntityList)) {
            model.setOrderNumber(orderEntityList.get(0).getOrderNumber());
            model.setQuantity(orderEntityList.get(0).getQuantity());
        }

        return GetServiceResponse.<ItemMisleadingRepresentationModel>builder().item(model).build();
    }

    /**
     * SKU情報取得.
     *
     * @param partNoId 品番ID
     * @return SKUのリスト
     */
    private List<SkuModel> findSku(final BigInteger partNoId) {
        final List<SkuModel> list =
                exSkuRepository.findByPartNoId(partNoId, PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(entity -> {
                    final SkuModel model = new SkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                })
                .collect(Collectors.toList());

        return list;
    }

    /**
     * 組成情報取得.
     * @param partNoId 品番ID
     * @return 組成情報リスト
     */
    private List<CompositionModel> findComposition(final BigInteger partNoId) {
        final List<CompositionModel> list =
                exCompositionRepository.findByPartNoId(partNoId, PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(entity -> {
                    final CompositionModel model = new CompositionModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                })
                .collect(Collectors.toList());

        return list;
    }

    /**
     * 優良誤認承認情報取得.
     * @param partNoId 品番ID
     * @return 組成情報リスト
     */
    private List<MisleadingRepresentationModel> findMisleadingRepresentation(final BigInteger partNoId) {
        final List<MisleadingRepresentationModel> list =
                exMisleadingRepresentationRepository.findByPartNoId(partNoId, PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(entity -> {
                    final MisleadingRepresentationModel model = new MisleadingRepresentationModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                })
                                                           .collect(Collectors.toList());


        return list;
    }

    /**
     * 優良誤認用品番情報Model作成.
     * @param exItemEntity 優良誤認用品番情報Entity
     * @return 優良誤認用品番情報Model
     */
    private ItemMisleadingRepresentationModel createItemMisleadingRepresentationModel(final ExtendedItemMisleadingRepresentationEntity exItemEntity) {

        final ItemMisleadingRepresentationModel model = new ItemMisleadingRepresentationModel();

        BeanUtils.copyProperties(exItemEntity, model);

        // 有害物質対応日付をDateに変換
        if (StringUtils.isNotEmpty(exItemEntity.getHazardousSubstanceResponseAt())
                && !StringUtils.equals(exItemEntity.getHazardousSubstanceResponseAt(), "00000000")) {
            model.setHazardousSubstanceResponseAt(DateUtils.stringToDate(exItemEntity.getHazardousSubstanceResponseAt()));
        }

        return model;

    }

    /**
     * 品番ファイル情報(タンザク)取得.
     *
     * @param partNoId 品番ID
     * @return 品番ファイル情報
     */
    private ItemFileInfoModel findTanzakuFileInfo(final BigInteger partNoId) {
        final ItemFileInfoModel model =
                fileInfoSelectRepository.findByPartNoIdAndFileCategory(partNoId, FileInfoCategory.TANZAKU.getValue(), PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .findFirst()
                .map(entity -> {
                    final ItemFileInfoModel fileInfoModel = new ItemFileInfoModel();
                    BeanUtils.copyProperties(entity, fileInfoModel);
                    return fileInfoModel;
                })
                .orElse(null);

        return model;
    }

    /**
     * 優良誤認ファイル情報取得.
     *
     * @param partNoId 品番ID
     * @return 優良誤認ファイル情報リスト
     */
    private List<MisleadingRepresentationFileModel> findMisleadingRepresentationFileInfo(final BigInteger partNoId) {
        final List<MisleadingRepresentationFileModel> list =
              exTMisleadingRepresentationFileRepository.findByPartNoId(partNoId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).stream()
                .map(this::createMisleadingRepresentationFileModel)
                .collect(Collectors.toList());

        return list;
    }

    /**
     * 優良誤認検査ファイル情報を作成.
     * @param entity 拡張優良誤認検査ファイル情報Entity
     * @return 優良誤認承認ファイル情報Model
     */
    private MisleadingRepresentationFileModel createMisleadingRepresentationFileModel(final ExtendedTMisleadingRepresentationFileEntity entity) {

        final MisleadingRepresentationFileModel misleadingRepresentationFile = new MisleadingRepresentationFileModel();
        final FileModel file = new FileModel();

        // 優良誤認検査ファイル情報のコピー
        BeanUtils.copyProperties(entity, misleadingRepresentationFile);
        BeanUtils.copyProperties(entity, file);

        // ファイル情報のidはfile_no_idをセット
        file.setId(entity.getFileNoId());
        misleadingRepresentationFile.setFile(file);

        return misleadingRepresentationFile;

    }
}
